package main;

import java.io.*;
import lexer.*;

public class TokenDump {
    public static void main(String[] args) throws Exception {
        Reader r = new FileReader("main/read.txt");
        Lexer lx = new Lexer(r);
        Token t;
        while ((t = lx.scan()) != null) {
            System.out.println(t + " -> " + t.tag);
            if (t.tag == Tag.EOF) break;
        }
    }
}
