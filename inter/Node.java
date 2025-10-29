package inter;

import lexer.*;

public class Node {
    protected int lexline = 0;

    public Node() {
        lexline = Lexer.line;
    }

    protected void error(String s) {
        throw new RuntimeException("Erro na linha " + lexline + ": " + s);
    }
}
