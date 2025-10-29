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
        if (look.tag == t) move();
        else error("Esperado token " + t + ", encontrado " + look);
    }

    private boolean isWord(String s) {
        return (look instanceof Word) && ((Word) look).lexeme.equals(s);
    }

    private void matchWord(String s) throws IOException {
        if (isWord(s)) move();
        else error("Esperado palavra '" + s + "'");
    }

    // =====================================================
    //  INÍCIO DO PARSEADOR
    // =====================================================

    public Stmt programa() throws IOException {
        // Gera o corpo principal do programa (funções ou main)
        List<Stmt> stmts = new ArrayList<>();

        // Lê instruções e declarações na ordem em que aparecem, até o EOF
        while (look.tag != Tag.EOF) {
            if (isWord("def")) {
                stmts.add(decFuncao());
            } else {
                stmts.add(comando());
            }
        }

        return new Seq(stmts); // programa = sequência de declarações
    }

    private Stmt decFuncao() throws IOException {
        matchWord("def");
        String nome = nomeMetodo();
        match(Tag.LPAREN);
        listaParametros();
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        Stmt corpo = corpoMetodo();
        match(Tag.RBRACE);
        return corpo; // poderia ser FunctionDecl se você quiser modelar funções
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

    private void listaParametros() throws IOException {
        if (look.tag == Tag.RPAREN) return;
        parametro();
        while (look.tag == Tag.COMMA) {
            match(Tag.COMMA);
            parametro();
        }
    }

    private void parametro() throws IOException {
        tipo();
        if (look instanceof Word) move();
        else error("Esperado identificador após tipo do parâmetro");
    }

    private void tipo() throws IOException {
        if (isWord("void") || isWord("int") || isWord("float") || isWord("String")) {
            move();
        } else if (look instanceof Word) {
            move();
        } else {
            error("Esperado tipo (void,int,float,String ou identificador)");
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

    private Stmt comando() throws IOException {
        // Ignora tokens de indentação no topo
        while (look.tag == Tag.INDENT || look.tag == Tag.DEDENT || look.tag == Tag.NEWLINE) {
            move();
        }

        if (look.tag == Tag.EOF) return null;

        if (isWord("print")) {
            return printComando();
        } else if (isWord("if")) {
            return ifComando();
        } else if (look instanceof Word) {
            // atribuição ou chamada
            Word idword = (Word) look;
            move();
            if (look.tag == Tag.ASSIGN) {
                match(Tag.ASSIGN);
                Expr e = expr();
                Id id = new Id(idword, Type.intWord, 0);
                if (look.tag == Tag.SEMICOLON) match(Tag.SEMICOLON);
                return new Assign(id, e);
            } else {
                error("Comando inválido após identificador '" + idword.lexeme + "'");
                return null;
            }
        } else {
            error("Comando inválido: " + look);
            return null;
        }
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
        if (isWord("else")) {
            matchWord("else");
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
            case NUM:
                x = new Constant(look, Type.intWord);
                move();
                return x;
            case REAL:
                x = new Constant(look, Type.floatWord);
                move();
                return x;
            case TEXT:
                x = new Text(((Word) look).lexeme);
                move();
                return x;
            case LPAREN:
                match(Tag.LPAREN);
                x = expr();
                match(Tag.RPAREN);
                return x;
            default:
                if (look instanceof Word) {
                    x = new Id((Word) look, Type.intWord, 0);
                    move();
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

    public Stmt parse() throws IOException {
        Stmt prog = programa();
        if (look.tag != Tag.EOF) {
            error("Tokens extras após fim do programa");
        }
        System.out.println("Análise sintática concluída com sucesso.");
        return prog; // retorna a AST raiz
    }
}
