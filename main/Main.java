package main;

import lexer.*;
import parser.*;
import java.io.*;

class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java main.Main <arquivo-fonte>");
            return;
        }

        try (Reader reader = new FileReader(args[0])) {
            Lexer lex = new Lexer(reader);
            Parser parser = new Parser(lex);
            parser.programa();
            System.out.write('\n');
        }
    }
}