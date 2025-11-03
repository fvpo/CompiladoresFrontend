package main;

import lexer.*;
import parser.*;
import inter.*;
import symbols.Env;

import java.io.*;

class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Diretório atual: " + System.getProperty("user.dir"));

        boolean printAst = false;
        String fileArg;

        if (args.length < 1) {
            System.err.println("Uso: java main.Main [--ast] <arquivo-fonte>");
            return;
        }

        if (args.length >= 2 && args[0].equals("--ast")) {
            printAst = true;
            fileArg = args[1];
        } else {
            fileArg = args[0];
        }

        try (Reader reader = new FileReader(fileArg)) {
            Lexer lex = new Lexer(reader);
            Parser parser = new Parser(lex);

            // Cria a AST
            Stmt programa = parser.parse();

            if (printAst) {
                System.out.println("--- AST ---");
                ASTPrinter.print(programa);
                System.out.println("--- END AST ---");
            }

            // Cria o ambiente
            Env env = new Env(null);
            // bind this env as the current thread-local environment so static lookups work
            env.bindCurrent();
            try {
                // Executa o programa
                programa.exec(env);
            } finally {
                env.unbindCurrent();
            }
        }
    }
}
