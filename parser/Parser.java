package parser;

import lexer.*;

import java.io.IOException;

public class Parser {
    private Lexer lexer;
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

    public void programa() throws IOException {
        while (look.tag != Tag.EOF) {
            if (isWord("def")) {
                decFuncao();
            } else if (isWord("main")) {
                mainDecl();
                break;
            } else {
                error("Esperado 'def' ou 'main' no início do programa");
            }
        }
    }

    private void decFuncao() throws IOException {
        matchWord("def");
        String nome = nomeMetodo();
        match(Tag.LPAREN);
        listaParametros();
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        corpoMetodo();
        match(Tag.RBRACE);
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
        if (look instanceof Word) {
            move();
        } else {
            error("Esperado identificador após tipo do parâmetro");
        }
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

    private void corpoMetodo() throws IOException {
        bloco();
    }

    private void bloco() throws IOException {
        while (true) {
            if (look instanceof Word) {
                String lexeme = ((Word) look).lexeme;
                if (isWord("print") || isWord("if") || isWord("for")) {
                    comando();
                } else {
                    comando();
                }
            } else if (look.tag == Tag.SEMICOLON) {
                match(Tag.SEMICOLON);
            } else if (look.tag == Tag.RBRACE || look.tag == Tag.EOF) {
                break;
            } else {
                if (isWord("print") || isWord("if") || isWord("for")) comando();
                else break;
            }
        }
    }

    private void comando() throws IOException {
        if (isWord("print")) {
            printComando();
        } else if (isWord("if")) {
            ifComando();
        } else if (isWord("for")) {
            forComando();
        } else if (look instanceof Word) {
            String id = ((Word) look).lexeme;
            move();
            if (look.tag == Tag.ASSIGN) {
                match(Tag.ASSIGN);
                if (isWord("new")) {
                    expressaoObjeto();
                } else {
                    expr();
                }
                if (look.tag == Tag.SEMICOLON) match(Tag.SEMICOLON);
            } else if (look.tag == Tag.DOT) {
                match(Tag.DOT);
                if (look instanceof Word) {
                    String m = ((Word) look).lexeme;
                    move();
                    match(Tag.LPAREN);
                    listaArgumentos();
                    match(Tag.RPAREN);
                    if (look.tag == Tag.SEMICOLON) match(Tag.SEMICOLON);
                } else {
                    error("Esperado nome de método após '.'");
                }
            } else {
                error("Comando não reconhecido após identificador '" + id + "'");
            }
        } else {
            error("Comando inválido: " + look);
        }
    }

    private void instanciacao() throws IOException {
        if (!(look instanceof Word)) error("Esperado identificador para instanciar");
        String nome = ((Word) look).lexeme; move();
        match(Tag.ASSIGN);
        expressaoObjeto();
        if (look.tag == Tag.SEMICOLON) match(Tag.SEMICOLON);
    }

    private void expressaoObjeto() throws IOException {
        matchWord("new");
        if (look instanceof Word) move(); else error("Esperado nome de classe após 'new'");
        match(Tag.LPAREN);
        listaArgumentos();
        match(Tag.RPAREN);
    }

    private void listaArgumentos() throws IOException {
        if (look.tag == Tag.RPAREN) return;
        argumento();
        while (look.tag == Tag.COMMA) {
            match(Tag.COMMA);
            argumento();
        }
    }

    private void argumento() throws IOException {
        expr();
    }

    private void expr() throws IOException {
        if (look.tag == Tag.NUM) {
            move();
        } else if (look.tag == Tag.REAL) {
            move();
        } else if (look instanceof Word) {
            move();
        } else if (look.tag == Tag.LPAREN) {
            match(Tag.LPAREN);
            expr();
            match(Tag.RPAREN);
        } else {
            error("Expressão esperada, encontrado: " + look);
        }
        while (look.tag == Tag.PLUS || look.tag == Tag.MINUS || look.tag == Tag.MULT || look.tag == Tag.DIV) {
            move();
            expr();
        }
    }

    private void printComando() throws IOException {
        matchWord("print");
        match(Tag.LPAREN);
        texto();
        match(Tag.RPAREN);
        match(Tag.SEMICOLON);
    }



    private void texto() throws IOException {
        if (look instanceof Word) {
            move();
        } else {
            error("Esperado string/texto dentro de print()");
        }
    }

    private void ifComando() throws IOException {
        matchWord("if");
        match(Tag.LPAREN);
        expr();
        match(Tag.RPAREN);
        if (look.tag == Tag.LBRACE) {
            match(Tag.LBRACE);
            bloco();
            match(Tag.RBRACE);
        } else {
            comando();
        }
        if (isWord("else")) {
            matchWord("else");
            if (look.tag == Tag.LBRACE) {
                match(Tag.LBRACE); bloco(); match(Tag.RBRACE);
            } else comando();
        }
    }

    private void forComando() throws IOException {
        matchWord("for");
        match(Tag.LPAREN);
        if (!(look.tag == Tag.SEMICOLON)) {
            expr();
        }
        match(Tag.SEMICOLON);
        if (!(look.tag == Tag.SEMICOLON)) expr();
        match(Tag.SEMICOLON);
        if (!(look.tag == Tag.RPAREN)) expr();
        match(Tag.RPAREN);
        if (look.tag == Tag.LBRACE) {
            match(Tag.LBRACE); bloco(); match(Tag.RBRACE);
        } else comando();
    }

    private void mainDecl() throws IOException {
        matchWord("main");
        match(Tag.LPAREN);
        match(Tag.RPAREN);
        match(Tag.LBRACE);
        corpoMetodo();
        match(Tag.RBRACE);
    }

    public void parse() throws IOException {
        programa();
        if (look.tag != Tag.EOF) {
            error("Tokens extras após fim do programa");
        }
        System.out.println("Análise sintática concluída com sucesso.");
    }
}
