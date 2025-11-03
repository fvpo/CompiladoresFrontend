package inter;

import java.util.Scanner;

import lexer.Word;
import symbols.Env;
import symbols.Type;

public class Input extends Expr {
    private static final Scanner scanner = new Scanner(System.in);

    public Input() {
        super(Word.inputWord, Type.stringWord); // tipo string
    }

    @Override
    public Object eval() {
        System.out.print("> "); // opcional: mostra prompt
        String line = scanner.nextLine();
        return line; // sempre retorna string
    }
}