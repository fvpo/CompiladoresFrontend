package main;

import lexer.*;
import parser.*;
import inter.*;
import symbols.Env;

import java.io.*;

class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Diretório atual: " + System.getProperty("user.dir"));

        if (args.length < 1) {
            System.err.println("Uso: java main.Main <arquivo-fonte>");
            return;
        }

        try (Reader reader = new FileReader(args[0])) {
            Lexer lex = new Lexer(reader);
            Parser parser = new Parser(lex);

            // Cria a AST
            Stmt programa = parser.parse();

            // Cria o ambiente
            Env env = new Env(null);

            // Executa o programa
            programa.exec(env);
        }
    }
}