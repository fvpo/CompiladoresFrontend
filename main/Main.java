package main;

import lexer.*; import parser.*;

import java.io.IOException;

class Main{
    public static void main(String[] args) throws IOException {
        Lexer lex = new Lexer();
        Parser parser = new Parser(lex);
        parser.programa();
        System.out.write('\n');
    }
}