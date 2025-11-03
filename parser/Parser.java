package parser;

import lexer.*;
import inter.*;
import symbols.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final Lexer lexer;
    private Token look;
    // Scoped symbol table for declared variable types
    private final SymbolTable symtab = new SymbolTable();

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        move();
    }

    private void move() throws IOException {
        look = lexer.scan();
    }

    private void error(String msg) {
        throw new RuntimeException("Erro sintático: " + msg + " — token atual: " + look);
    }

    private void match(Tag t) throws IOException {
        if (look.tag == t) {
            move();
        } else {
            error("Esperado token " + t + ", encontrado " + look);
        }
    }

    private boolean isWord(String s) {
        return (look instanceof Word) && ((Word) look).lexeme.equals(s);
    }

    private void matchWord(String s) throws IOException {
        if (isWord(s)) {
            move();
        } else {
            error("Esperado palavra '" + s + "'");
        }
    }

    // =====================================================
    // INÍCIO DO PARSEADOR
    // =====================================================
    public Stmt programa() throws IOException {
        List<Stmt> stmts = new ArrayList<>();

        // Lê instruções e declarações na ordem em que aparecem, até o EOF
        while (look.tag != Tag.EOF) {
            if (isWord("def")) {
                stmts.add(decFuncao());
            } else if (isWord("class")) {
                stmts.add(declClasse());
            } else {
                stmts.add(comando());
            }
        }

        return new Seq(stmts); // programa = sequência de comandos top-level
    }

    private Stmt decFuncao() throws IOException {
        matchWord("def");
        String nome = nomeMetodo();
        match(Tag.LPAREN);
        // collect parameter names
        java.util.List<String> params = listaParametros();
        match(Tag.RPAREN);
        // optional return type after parameter list (e.g., def foo(int x) int { ... })
        symbols.Type returnType = symbols.Type.voidWord;
        if (isTipo(look) || (look instanceof Word && ((Word) look).tag == Tag.ID)) {
            // consume a type token or identifier (user class) and resolve it
            String typeName = ((Word) look).lexeme;
            move();
            returnType = resolveType(typeName);
        }
        // allow optional newlines/indentation between return type and method body
        while (look.tag == Tag.NEWLINE || look.tag == Tag.INDENT || look.tag == Tag.DEDENT) {
            move();
        }
        match(Tag.LBRACE);
        Stmt corpo = corpoMetodo();
        match(Tag.RBRACE);
        // return a MethodDecl so class members retain the method name, body and return type
        return new inter.MethodDecl(nome, params, corpo, returnType);
    }

    private String nomeMetodo() throws IOException {
        if (look instanceof Word) {
            String s = ((Word) look).lexeme;
            move();
            return s;
        } else {
            error("Esperado identificador (nome do método)");
            return null;
        }
    }

    // Note: declaration parsing of the form "Type id;" is handled inline in
    // comando()

    private java.util.List<String> listaParametros() throws IOException {
        java.util.List<String> params = new java.util.ArrayList<>();
        if (look.tag == Tag.RPAREN) {
            return params;
        }
        params.add(parametro());
        while (look.tag == Tag.COMMA) {
            match(Tag.COMMA);
            params.add(parametro());
        }
        return params;
    }

    private String parametro() throws IOException {
        tipo();
        if (look instanceof Word) {
            String name = ((Word) look).lexeme;
            move();
            return name;
        } else {
            error("Esperado identificador após tipo do parâmetro");
            return null;
        }
    }

    private void tipo() throws IOException {
        if (isWord("void") || isWord("int") || isWord("float") || isWord("String") || isWord("c_channel")) {
            move();
        } else if (look instanceof Word) {
            move();
        } else {
            error("Esperado tipo (void,int,float,String,c_channel ou identificador)");
        }
    }

    private Stmt corpoMetodo() throws IOException {
        return bloco();
    }

    private Stmt bloco() throws IOException {
        List<Stmt> stmts = new ArrayList<>();
        while (look.tag != Tag.RBRACE && look.tag != Tag.EOF) {
            stmts.add(comando());
        }
        return new Seq(stmts);
    }

    private Stmt declClasse() throws IOException {
        matchWord("class"); // consome 'class'
        String nome = nomeClasse(); // lê o identificador da classe

        String superNome = null;
        if (isWord("extends")) { // consome 'extends' se houver
            matchWord("extends");
            superNome = nomeClasse();
        }

        match(Tag.LBRACE); // abre corpo da classe
        List<Stmt> membros = new ArrayList<>();
        while (look.tag != Tag.RBRACE && look.tag != Tag.EOF) {
            if (isWord("def")) {
                membros.add(decFuncao());
            } else {
                membros.add(comando());
            }
        }
        match(Tag.RBRACE); // fecha corpo da classe

        return new ClassDecl(nome, superNome, membros); // ClassDecl deve existir em inter
    }

    private String nomeClasse() throws IOException {
        if (look instanceof Word) {
            String s = ((Word) look).lexeme;
            move();
            return s;
        } else {
            error("Esperado identificador (nome da classe)");
            return null;
        }
    }

    // =====================================================
    // SEQ / PAR (blocos por indentação)
    // =====================================================
    private Stmt blocoSeqPar() throws IOException {
        boolean isSeq = isWord("seq");
        move(); // consome 'seq' ou 'par'

        // Espera abrir bloco com '{'
        if (look.tag != Tag.LBRACE) {
            error("Esperado '{' após '" + (isSeq ? "seq" : "par") + "'");
        }
        match(Tag.LBRACE);

        // Lê comandos até '}'
        List<Stmt> stmts = new ArrayList<>();
        while (look.tag != Tag.RBRACE && look.tag != Tag.EOF) {
            Stmt s = comando();
            if (s != null) stmts.add(s);
        }

        match(Tag.RBRACE);

        return isSeq ? new Seq(stmts) : new Par(stmts);
    }

    private Stmt whileComando() throws IOException {
        matchWord("while");
        match(Tag.LPAREN);
        Expr cond = expr();
        match(Tag.RPAREN);

        // Corpo do while
        Stmt corpo;
        if (look.tag == Tag.LBRACE) {
            match(Tag.LBRACE);
            corpo = bloco(); // bloco() já consome NEWLINEs e chama comando() repetidamente
            match(Tag.RBRACE);
        } else {
            corpo = comando();
        }

        return new inter.While(cond, corpo);
    }

    private Stmt forComando() throws IOException {
        matchWord("for");
        match(Tag.LPAREN);

        // ========================
        // Inicialização (init)
        // ========================
        Stmt init = null;
        if (look.tag != Tag.SEMICOLON) {
            // Aqui tratamos declaração do tipo "int i = 0" ou apenas atribuição "i = 0"
            if (isTipo(look)) { // método que verifica se look é 'int', 'float', etc.
                Type t = resolveType(((Word) look).lexeme);
                move(); // consome tipo
                if (!(look instanceof Word)) {
                    error("Esperado identificador após tipo");
                }
                Word idWord = (Word) look;
                move(); // consome identificador
                Expr initExpr = null;
                if (look.tag == Tag.ASSIGN) {
                    match(Tag.ASSIGN);
                    initExpr = expr();
                }
                init = new Decl(new Id(idWord, t, 0), initExpr);
            } else if (look instanceof Word) {
                // Caso de atribuição simples
                Word idWord = (Word) look;
                move();
                if (look.tag != Tag.ASSIGN) {
                    error("Esperado '=' após identificador no init do for");
                }
                match(Tag.ASSIGN);
                Expr initExpr = expr();
                init = new inter.Assign(new Id(idWord, Type.intWord, 0), initExpr);
            } else {
                error("Inicialização inválida no for");
            }
        }
        match(Tag.SEMICOLON);

        // ========================
        // Condição
        // ========================
        Expr cond = null;
        if (look.tag != Tag.SEMICOLON) {
            cond = expr();
        }
        match(Tag.SEMICOLON);

        // ========================
        // Atualização (update)
        // ========================
        Stmt update = null;
        if (look.tag != Tag.RPAREN) {
            if (look instanceof Word w) {
                move();
                if (look.tag == Tag.ASSIGN) {
                    match(Tag.ASSIGN);
                    Expr rhs = expr();
                    update = new inter.Assign(new Id(w, Type.intWord, 0), rhs);
                } else if (look.tag == Tag.INC) {
                    match(Tag.INC);
                    update = new inter.Assign(new Id(w, Type.intWord, 0),
                            new Arith(new Token(Tag.PLUS), new Id(w, Type.intWord, 0), Constant.fromInt(1)));
                } else if (look.tag == Tag.DEC) {
                    match(Tag.DEC);
                    update = new inter.Assign(new Id(w, Type.intWord, 0),
                            new Arith(new Token(Tag.MINUS), new Id(w, Type.intWord, 0), Constant.fromInt(1)));
                } else {
                    error("Atualização inválida no for: esperado =, ++ ou --");
                }
            } else {
                error("Atualização inválida no for: esperado identificador");
            }
        }
        match(Tag.RPAREN);

        // ========================
        // Corpo do for
        // ========================
        Stmt corpo;
        if (look.tag == Tag.LBRACE) {
            match(Tag.LBRACE);
            corpo = bloco();
            match(Tag.RBRACE);
        } else {
            corpo = comando();
        }

        return new inter.For(init, cond, update, corpo);
    }

    // Método auxiliar para verificar tipos básicos
    private boolean isTipo(Token t) {
        return t instanceof Word w && (w.lexeme.equals("int") || w.lexeme.equals("float")
                || w.lexeme.equals("String") || w.lexeme.equals("c_channel"));
    }

    // ========================
    // Método auxiliar para inicialização e update
    // ========================


    // =====================================================
    // COMANDOS
    // =====================================================
    private Stmt comando() throws IOException {
        // Ignora tokens de indentação e NEWLINE
        while (look.tag == Tag.INDENT || look.tag == Tag.DEDENT || look.tag == Tag.NEWLINE) {
            move();
        }

    // (debug prints removed)

        if (look.tag == Tag.EOF) {
            return null;
        }

        // Se encontrou '}', significa fim de bloco
        if (look.tag == Tag.RBRACE) {
            return null; // bloco() vai tratar o match(RBRACE)
        }

        // ===============================
        // PALAVRAS RESERVADAS
        // ===============================
        if (isWord("def")) {
            return decFuncao();
        } else if (isWord("class")) {
            return declClasse();
        } else if (isWord("print")) {
            matchWord("print");
            match(Tag.LPAREN);
            Expr x = expr();
            match(Tag.RPAREN);
            match(Tag.SEMICOLON);
            return new Print(x);
        } else if (isWord("send")) {
            matchWord("send");
            match(Tag.LPAREN);
            if (!(look instanceof Word)) error("Esperado identificador do canal");
            Word chWord = (Word) look;
            move();
            match(Tag.COMMA);
            Expr value = expr();
            match(Tag.RPAREN);
            match(Tag.SEMICOLON);
            Type chType = symtab.lookup(chWord.lexeme);
            if (chType == null) chType = Type.stringWord;
            return new inter.Send(new Id(chWord, chType, 0), value);
        } else if (isWord("receive")) {
            matchWord("receive");
            match(Tag.LPAREN);
            if (!(look instanceof Word)) error("Esperado identificador do canal");
            Word chWord = (Word) look;
            move();
            match(Tag.COMMA);
            if (!(look instanceof Word)) error("Esperado identificador alvo");
            Word targetWord = (Word) look;
            move();
            match(Tag.RPAREN);
            match(Tag.SEMICOLON);
            Type chType = symtab.lookup(chWord.lexeme);
            if (chType == null) chType = Type.stringWord;
            Type targetType = symtab.lookup(targetWord.lexeme);
            if (targetType == null) targetType = Type.intWord;
            return new inter.Receive(new Id(chWord, chType, 0), new Id(targetWord, targetType, 0));
        } else if (isWord("return")) {
            // return [expr] ;
            matchWord("return");
            Expr e = null;
            if (look.tag != Tag.SEMICOLON) {
                e = expr();
            }
            match(Tag.SEMICOLON);
            return new inter.Return(e);
        } else if (isWord("if")) {
            return ifComando();
        } else if (isWord("seq") || isWord("par")) {
            return blocoSeqPar();
        } else if (isWord("while")) {
            return whileComando();
        } else if (isWord("for")) {
            return forComando();
        } else if (isWord("break")) {
            move();
            if (look.tag == Tag.SEMICOLON)
                match(Tag.SEMICOLON);
            return new inter.Break();
        } else if (isWord("continue")) {
            move();
            if (look.tag == Tag.SEMICOLON)
                match(Tag.SEMICOLON);
            return new inter.Continue();
        }

        // ===============================
        // DECLARAÇÕES E ATRIBUIÇÕES
        // ===============================
        if (look instanceof Word first) {
            move(); // consume first word (could be a type or an identifier)

            // <Type> <id> ... -> declaration
            // Ensure the next token is an identifier (Tag.ID), not a punctuation Word like '['
            if (look instanceof Word idWord && idWord.tag == Tag.ID) {
                move(); // consume identifier
                Type declaredType = resolveType(first.lexeme);
                
                // Create and register ID
                Id id = new Id(idWord, declaredType, 0);
                symtab.declare(idWord.lexeme, declaredType);
                
                // Handle initialization based on type
                Expr init = null;
                
                if (declaredType == Type.cchannelWord) {
                    // Channel must be initialized
                    match(Tag.ASSIGN);
                    matchWord("new");
                    matchWord("c_channel");
                    match(Tag.SEMICOLON);
                    init = new New("c_channel", Type.cchannelWord);
                } else if (look.tag == Tag.LBRACKET) {
                    // Array declaration
                    match(Tag.LBRACKET);
                    if (look.tag != Tag.NUM) error("Esperado tamanho do array");
                    Num sizeTok = (Num) look;
                    move();
                    match(Tag.RBRACKET);
                    
                    // Update to array type
                    id = new Id(idWord, new symbols.Array(declaredType, sizeTok.value), 0);
                    symtab.declare(idWord.lexeme, id.type);
                    
                    // Optional array initialization
                    if (look.tag == Tag.ASSIGN) {
                        match(Tag.ASSIGN);
                        init = arrExpr();
                    }
                    match(Tag.SEMICOLON);
                } else {
                    // Regular variable
                    if (look.tag == Tag.ASSIGN) {
                        match(Tag.ASSIGN);
                        init = expr();
                    }
                    match(Tag.SEMICOLON);
                }
                
                return new Decl(id, init);
            }
            
            // Create base expression (identifier or array access)
            Expr x;
            if (first.lexeme.equals("this")) {
                x = new inter.This();
            } else {
                // Use declared type if available, otherwise default to int
                Type known = symtab.lookup(first.lexeme);
                if (known == null) known = Type.intWord;
                x = new Id(first, known, 0);
                
                // Check for array access before assignment
                if (look.tag == Tag.LBRACKET) {
                    match(Tag.LBRACKET);
                    Expr index = expr();
                    match(Tag.RBRACKET);
                    x = new inter.Index(x, index);
                }
            }

            // <id> = ... -> assignment
            if (look.tag == Tag.ASSIGN) {
                match(Tag.ASSIGN);
                Expr e;
                if (look.tag == Tag.LBRACE) {
                    e = arrExpr();
                } else {
                    e = expr();
                }
                match(Tag.SEMICOLON);
                return new Assign(x, e);
            }

            

            // parse trailers: .name(...) ou .name
            while (look.tag == Tag.DOT) {
                match(Tag.DOT);
                if (!(look instanceof Word)) {
                    error("Esperado identificador após '.'");
                }
                String memberName = ((Word) look).lexeme;
                move();

                if (look.tag == Tag.LPAREN) {
                    match(Tag.LPAREN);
                    List<Expr> callArgs = new java.util.ArrayList<>();
                    if (look.tag != Tag.RPAREN) {
                        callArgs.add(expr());
                        while (look.tag == Tag.COMMA) {
                            match(Tag.COMMA);
                            callArgs.add(expr());
                        }
                    }
                    match(Tag.RPAREN);
                    x = new inter.Call(x, memberName, callArgs);
                } else {
                    x = new inter.Access(x, memberName);
                }
            }

            // Assignment to field: e.g. this.field = expr
            if (look.tag == Tag.ASSIGN) {
                match(Tag.ASSIGN);
                Expr rhs;
                rhs = expr();

                if (look.tag == Tag.SEMICOLON) {
                    match(Tag.SEMICOLON);
                }
                return new Assign(x, rhs);
            }

            // Assignment with compound operators on fields: this.field += expr
            if (look.tag == Tag.PLUS_ASSIGN || look.tag == Tag.MINUS_ASSIGN || look.tag == Tag.MULT_ASSIGN || look.tag == Tag.DIV_ASSIGN) {
                Token opTok = look;
                move();
                Expr rhs = expr();
                if (look.tag == Tag.SEMICOLON) match(Tag.SEMICOLON);

                Token binOp;
                switch (opTok.tag) {
                    case PLUS_ASSIGN: binOp = Word.plusWord; break;
                    case MINUS_ASSIGN: binOp = Word.minusWord; break;
                    case MULT_ASSIGN: binOp = Word.multWord; break;
                    case DIV_ASSIGN: binOp = Word.divWord; break;
                    default: binOp = Word.plusWord; break;
                }
                Expr ar = new inter.Arith(binOp, x, rhs);
                return new Assign(x, ar);
            }

            // Standalone call/access statement
            if (look.tag == Tag.SEMICOLON) {
                match(Tag.SEMICOLON);
                return new ExprStmt(x);
            }

            error("Comando inválido após identificador '" + first.lexeme + "'");
            return null;
        }

        error("Comando inválido: " + look);
        return null;
    }

    private Stmt ifComando() throws IOException {
        matchWord("if");
        match(Tag.LPAREN);
        Expr cond = expr();
        match(Tag.RPAREN);

        Stmt thenStmt;
        if (look.tag == Tag.LBRACE) {
            match(Tag.LBRACE);
            thenStmt = bloco();
            match(Tag.RBRACE);
        } else {
            thenStmt = comando();
        }

        Stmt elseStmt = null;

        while (look.tag == Tag.NEWLINE || look.tag == Tag.INDENT || look.tag == Tag.DEDENT) {
            move();
        }

        if (isWord("else")) {
            matchWord("else"); // consome o 'else'
            if (look.tag == Tag.LBRACE) {
                match(Tag.LBRACE);
                elseStmt = bloco();
                match(Tag.RBRACE);
            } else {
                elseStmt = comando();
            }
        }

        return new If(cond, thenStmt, elseStmt);
    }

    // =====================================================
    // EXPRESSÕES
    // =====================================================
    private Expr expr() throws IOException {
        return rel();
    }

    private Expr arrExpr() throws IOException {
        match(Tag.LBRACE);
        List<Expr> elements = new ArrayList<>();
        if (look.tag != Tag.RBRACE) {
            elements.add(expr());
            while (look.tag == Tag.COMMA) {
                match(Tag.COMMA);
                elements.add(expr());
            }
        }
        match(Tag.RBRACE);
        return new inter.ArrayLiteral(elements);
    }

    // =====================================================
    // EXPRESSÕES RELACIONAIS
    // =====================================================
    private Expr rel() throws IOException {
        Expr left = arith();

        while (look.tag == Tag.LT || look.tag == Tag.GT
                || look.tag == Tag.LE || look.tag == Tag.GE
                || look.tag == Tag.EQ || look.tag == Tag.NE) {
            Token op = look;
            move();
            Expr right = arith();
            left = new Rel(op, left, right); // nova classe que você cria (semelhante à Arith)
        }

        return left;
    }

    // =====================================================
    // EXPRESSÕES ARITMÉTICAS
    // =====================================================
    private Expr arith() throws IOException {
        Expr left = term();
        while (look.tag == Tag.PLUS || look.tag == Tag.MINUS) {
            Token op = look;
            move();
            Expr right = term();
            left = new Arith(op, left, right);
        }
        return left;
    }

    private Expr term() throws IOException {
        Expr left = factor();
        while (look.tag == Tag.MULT || look.tag == Tag.DIV) {
            Token op = look;
            move();
            Expr right = factor();
            left = new Arith(op, left, right);
        }
        return left;
    }

    private Expr factor() throws IOException {
        Expr x;

        switch (look.tag) {
            case NUM: {
                Num numTok = (Num) look;
                x = Constant.fromInt(numTok.value);
                move();
                return x;
            }
            case REAL: {
                Real realTok = (Real) look;
                x = Constant.fromDouble(realTok.value);
                move();
                return x;
            }
            case TEXT: {
                String s = ((Word) look).lexeme;
                x = Constant.fromString(s);
                move();
                return x;
            }
            case LPAREN: {
                match(Tag.LPAREN);
                x = expr();
                match(Tag.RPAREN);
                return x;
            }
            case LBRACE: { // <<< ADIÇÃO: array literal como fator
                return arrExpr();
            }
            default:
                // NEW, THIS, INPUT ou identificadores
                if (isWord("new")) {
                    move(); // consome 'new'
                    if (!(look instanceof Word)) {
                        error("Esperado nome da classe após 'new'");
                    }
                    String className = ((Word) look).lexeme;
                    move();

                    match(Tag.LPAREN);
                    List<Expr> args = new ArrayList<>();
                    if (look.tag != Tag.RPAREN) {
                        args.add(expr());
                        while (look.tag == Tag.COMMA) {
                            match(Tag.COMMA);
                            args.add(expr());
                        }
                    }
                    match(Tag.RPAREN);

                    return new New(className, args);
                }

                if (isWord("input")) {
                    move(); // consome 'input'
                    match(Tag.LPAREN);
                    match(Tag.RPAREN);
                    return new inter.Input();
                }

                if (look instanceof Word) {
                    Word idWord = (Word) look;
                    if (idWord.lexeme.equals("this")) {
                        x = new inter.This();
                        move();
                    } else {
                        Type known2 = symtab.lookup(((Word) idWord).lexeme);
                        if (known2 == null) known2 = Type.intWord;
                        x = new Id(idWord, known2, 0);
                        move();

                        // Handle array indexing
                        if (look.tag == Tag.LBRACKET) {
                            match(Tag.LBRACKET);
                            Expr index = expr(); // Parse the index expression
                            match(Tag.RBRACKET);
                            x = new inter.Index(x, index); // Create new Index node
                        }
                    }

                    // parse trailers: .name(...) ou .name
                    while (look.tag == Tag.DOT) {
                        match(Tag.DOT);
                        if (!(look instanceof Word)) {
                            error("Esperado identificador após '.'");
                        }
                        String memberName = ((Word) look).lexeme;
                        move();

                        if (look.tag == Tag.LPAREN) {
                            // method call
                            match(Tag.LPAREN);
                            List<Expr> callArgs = new ArrayList<>();
                            if (look.tag != Tag.RPAREN) {
                                callArgs.add(expr());
                                while (look.tag == Tag.COMMA) {
                                    match(Tag.COMMA);
                                    callArgs.add(expr());
                                }
                            }
                            match(Tag.RPAREN);
                            x = new inter.Call(x, memberName, callArgs);
                        } else {
                            // field access
                            x = new inter.Access(x, memberName);
                        }
                    }

                    return x;
                }

                error("Fator inválido: " + look);
                return null;
        }
    }

    // =====================================================
    // EXECUÇÃO
    // =====================================================

    /**
     * Resolve a textual type name to a Type object. Built-ins map to existing
     * Type constants; unknown names are treated as user-declared class types.
     */
    private Type resolveType(String name) {
        return switch (name) {
            case "int" -> Type.intWord;
            case "float" -> Type.floatWord; 
            case "string" -> Type.stringWord;
            case "void" -> Type.voidWord;
            case "c_channel" -> Type.cchannelWord;
            default -> new symbols.ClassType(name);
        };
    }

    public Stmt parse() throws IOException {
        Stmt prog = programa();
        if (look.tag != Tag.EOF) {
            error("Tokens extras após fim do programa");
        }
        System.out.println("Análise sintática concluída com sucesso.");
        return prog;
    }
}
