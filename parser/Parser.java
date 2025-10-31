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
    // simple scoped symbol table for syntax checks (variable/function/class names)
    private final java.util.Deque<java.util.Set<String>> scopes = new java.util.ArrayDeque<>();

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        move();
        // start global scope
        enterScope();
    }

    private void enterScope() {
        scopes.push(new java.util.HashSet<>());
    }

    private void exitScope() {
        scopes.pop();
    }

    private void declare(String name) {
        if (scopes.isEmpty()) enterScope();
        scopes.peek().add(name);
    }

    private boolean isDeclared(String name) {
        for (java.util.Set<String> s : scopes) {
            if (s.contains(name)) return true;
        }
        return false;
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
        }else {
            error("Esperado token " + t + ", encontrado " + look);
        }
    }

    private boolean isWord(String s) {
        return (look instanceof Word) && ((Word) look).lexeme.equals(s);
    }

    private void matchWord(String s) throws IOException {
        if (isWord(s)) {
            move(); 
        }else {
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

    private Stmt decMetodo() throws IOException {
        // read method name 
        String nome = nomeMetodo();
        match(Tag.LPAREN);
        // collect parameter names 
        java.util.List<String> params = listaParametros();
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        // new scope for method body
        enterScope();
        // declare parameters in method scope
        for (String p : params) declare(p);
        // implicitly allow 'this' inside method bodies since they belong to a class
        declare("this");
        Stmt corpo = corpoMetodo();
        exitScope();
        match(Tag.RBRACE);
        // return a MethodDecl for the class to store
        return new inter.MethodDecl(nome, params, corpo);
    }

    private Stmt decFuncao() throws IOException {
        matchWord("def");
        String nome = nomeMetodo();
        // register function name in current (global) scope so recursion/uses are allowed 
        declare(nome);
        match(Tag.LPAREN);
        // collect parameter names
        java.util.List<String> params = listaParametros();
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        // new scope for function body
        enterScope();
        // declare parameters in function scope
        for (String p : params) declare(p);
        Stmt corpo = corpoMetodo();
        exitScope();
        match(Tag.RBRACE);
        // return a MethodDecl to represent the function (reuse class method node)
        return new inter.MethodDecl(nome, params, corpo);
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

    // Note: declaration parsing of the form "Type id;" is handled inline in comando()

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
        // enter a new scope for the block
        enterScope();
        List<Stmt> stmts = new ArrayList<>();
        while (look.tag != Tag.RBRACE && look.tag != Tag.EOF) {
            stmts.add(comando());
        }
        // exit block scope
        exitScope();
        return new Seq(stmts);
    }

    /** Recursively validate expressions for undeclared identifier usages. */
    private void validateExpr(Expr e) {
        if (e == null) return;
        if (e instanceof Id) {
            String name = ((Id) e).getName();
            if (!isDeclared(name)) {
                error("Uso de identificador não declarado na expressão: " + name);
            }
            return;
        }
        if (e instanceof Arith) {
            Arith a = (Arith) e;
            validateExpr(a.expr1);
            validateExpr(a.expr2);
            return;
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            validateExpr(u.expr);
            return;
        }
        if (e instanceof Rel) {
            Rel r = (Rel) e;
            validateExpr(r.expr1);
            validateExpr(r.expr2);
            return;
        }
        if (e instanceof Call) {
            Call c = (Call) e;
            // For function calls, ensure the function name is declared
            if (c.target != null) {
                validateExpr(c.target);
            } else {
                String funcName = c.methodName;
                if (!isDeclared(funcName)) {
                    error("Uso de função não declarada: " + funcName);
                }
            }
            // Validate all function arguments
            if (c.args != null) {
                for (Expr arg : c.args) validateExpr(arg);
            }
            return;
        }
        if (e instanceof Access) {
            Access ac = (Access) e;
            validateExpr(ac.target);
            return;
        }
        if (e instanceof New) {
            New n = (New) e;
            if (n.args != null) for (Expr arg : n.args) validateExpr(arg);
            return;
        }
        // Constant and other expression types are safe
    }

    private Stmt declClasse() throws IOException {
        matchWord("class");               // consome 'class'
        String nome = nomeClasse();       // lê o identificador da classe
        // register class name in global scope
        declare(nome);

        String superNome = null;
        if (isWord("extends")) {          // consome 'extends' se houver
            matchWord("extends");
            superNome = nomeClasse();
        }

        match(Tag.LBRACE);                // abre corpo da classe
        List<Stmt> membros = new ArrayList<>();
        while (look.tag != Tag.RBRACE && look.tag != Tag.EOF) {
            if (isWord("def")) {
                matchWord("def"); // consume 'def' keyword
                membros.add(decMetodo());
            } else {
                membros.add(comando());
            }
        }
        match(Tag.RBRACE);                // fecha corpo da classe

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
    // =====================================================
// SEQ / PAR (blocos por indentação)
// =====================================================
    private Stmt blocoSeqPar() throws IOException {
        boolean isSeq = isWord("seq");
        move(); // consome 'seq' ou 'par'

        // consumir possíveis NEWLINEs que terminam a linha onde veio 'seq'/'par'
        while (look.tag == Tag.NEWLINE) {
            move();
        }

        // agora esperamos o INDENT obrigatoriamente (pois seq/par só por indentação)
        if (look.tag != Tag.INDENT) {
            error("Esperado INDENT após 'seq'/'par' (bloco indentado).");
        }
        match(Tag.INDENT);

        // enter block scope
        enterScope();

        List<Stmt> stmts = new ArrayList<>();

        // lê comandos até DEDENT
        while (look.tag != Tag.DEDENT && look.tag != Tag.EOF) {
            Stmt s = comando();
            if (s != null) {
                stmts.add(s);
            }
        }

// Ignora possíveis NEWLINEs entre o fim do bloco e o DEDENT
        while (look.tag == Tag.NEWLINE) {
            move();
        }

        // exit block scope
        exitScope();

        match(Tag.DEDENT);

        return isSeq ? new Seq(stmts) : new Par(stmts);
    }

    // =====================================================
    // COMANDOS
    // =====================================================
    private Stmt comando() throws IOException {
        // Ignora tokens de indentação e NEWLINE
        while (look.tag == Tag.INDENT || look.tag == Tag.DEDENT || look.tag == Tag.NEWLINE) {
            move();
        }

        if (look.tag == Tag.EOF) {
            return null;
        }

        // Se encontrou '}', significa fim de bloco
        if (look.tag == Tag.RBRACE) {
            return null; // bloco() vai tratar o match(RBRACE)
        }

        // Reserved keywords first
        if (isWord("def")) {
            return decFuncao();
        } else if (isWord("class")) {
            return declClasse();
        } else if (isWord("print")) {
            return printComando();
        } else if (isWord("return")) {
            matchWord("return");
            Expr e = null;
            if (look.tag != Tag.SEMICOLON) {
                e = expr();
                // static check: recursively validate the expression for undeclared identifiers
                validateExpr(e);
            }
            match(Tag.SEMICOLON);
            return new inter.Return(e);
        } else if (isWord("if")) {
            return ifComando();
        } else if (isWord("seq") || isWord("par")) {
            return blocoSeqPar();
        }

        // Handle declarations (Type id ...) and assignments (id = ...)
        if (look instanceof Word) {
            Word first = (Word) look;
            move(); // consume first word (could be a type or an identifier)

            // Pattern: <Type> <id> ...  -> declaration
                if (look instanceof Word) {
                    Word idWord = (Word) look;
                    move(); // consume identifier

                    Expr init = null;
                    if (look.tag == Tag.ASSIGN) {
                        match(Tag.ASSIGN);
                        init = expr();
                    }

                    if (look.tag == Tag.SEMICOLON) {
                        match(Tag.SEMICOLON);
                    }

                    // resolve the declared type (first.lexeme)
                    Type declaredType = resolveType(first.lexeme);
                    // register variable in current scope for syntax checks
                    declare(idWord.lexeme);
                    Id id = new Id(idWord, declaredType, 0);
                    return new Decl(id, init);
                }

            // Pattern: <id> = ...  -> assignment
            if (look.tag == Tag.ASSIGN) {
                match(Tag.ASSIGN);
                Expr e = expr();
                Id id = new Id(first, Type.intWord, 0);
                if (look.tag == Tag.SEMICOLON) {
                    match(Tag.SEMICOLON);
                }
                return new Assign(id, e);
            }

            // Otherwise treat as an expression statement: parse possible trailers (.name or .name(args))
            Expr x = new Id(first, Type.intWord, 0);
            // direct function call: id(...)
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
                x = new inter.Call(null, first.lexeme, callArgs);
            }

            // parse trailers: .member or .member(args...)
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

            if (look.tag == Tag.SEMICOLON) {
                match(Tag.SEMICOLON);
            } else {
                error("Esperado ';' após expressão");
            }

            return new inter.ExprStmt(x);
        }

        error("Comando inválido: " + look);
        return null;
    }

    private Stmt printComando() throws IOException {
        matchWord("print");
        match(Tag.LPAREN);
        Expr e = expr();
        match(Tag.RPAREN);
        match(Tag.SEMICOLON);
        return new Print(e);
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
                // look é um Num token (classe Num extends Token { public final int value; })
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
                // Se você criou token Word com Tag.TEXT e lexeme contendo o texto
                String s = ((Word) look).lexeme;
                x = Constant.fromString(s);
                move();
                return x;
            }
            case LPAREN:
                match(Tag.LPAREN);
                x = expr();
                match(Tag.RPAREN);
                return x;
            default:
                // Aqui tratamos o NEW e c_channel
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

                if (look instanceof Word) {
                    // simple identifier; may be followed by (args) or .member(...) chains
                    Word idWord = (Word) look;
                    x = new Id(idWord, Type.intWord, 0);
                    move();

                    // direct call: id(...) -> Call(target=null, name=id)
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
                        x = new inter.Call(null, idWord.lexeme, callArgs);
                    }

                    // parse trailers: .name(...) or .name
                    while (look.tag == Tag.DOT) {
                        match(Tag.DOT);
                        if (!(look instanceof Word)) {
                            error("Esperado identificador após '.'");
                        }
                        String memberName = ((Word) look).lexeme;
                        move();

                        if (look.tag == Tag.LPAREN) {
                            // method call on expression
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
    // MAIN
    // =====================================================
    private Stmt mainDecl() throws IOException {
        matchWord("main");
        match(Tag.LPAREN);
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        Stmt corpo = corpoMetodo();
        match(Tag.RBRACE);
        return corpo;
    }

    // =====================================================
    // EXECUÇÃO
    // =====================================================

    /** Resolve a textual type name to a Type object. Built-ins map to existing
     * Type constants; unknown names are treated as user-declared class types.
     */
    private Type resolveType(String name) {
        switch (name) {
            case "int": return Type.intWord;
            case "float": return Type.floatWord;
            case "string": return Type.stringWord;
            case "void": return Type.voidWord;
            case "c_channel":
                // if you have a specific Type for channels, map it here; fallback to string
                return Type.stringWord;
            default:
                return new symbols.ClassType(name);
        }
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
