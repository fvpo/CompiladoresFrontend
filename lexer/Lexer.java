package lexer;

import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';
    private final Hashtable<String, Word> words = new Hashtable<>();

    // 🔹 Registra uma palavra reservada ou símbolo
    void reserve(Word w) {
        words.put(w.lexeme, w);
    }

    // 🔹 Construtor — reserva todas as palavras e operadores conhecidos
    public Lexer() {
        // Palavras-chave
        reserve(Word.ifWord);
        reserve(Word.elseWord);
        reserve(Word.whileWord);
        reserve(Word.forWord);
        reserve(Word.breakWord);
        reserve(Word.continueWord);
        reserve(Word.returnWord);

        // Blocos de execução
        reserve(Word.seqWord);
        reserve(Word.parWord);

        // Tipos de dados
        reserve(Word.stringWord);
        reserve(Word.cchannelWord);

        // Booleanos
        reserve(Word.trueWord);
        reserve(Word.falseWord);

        // Operadores lógicos
        reserve(Word.andWord);
        reserve(Word.orWord);
        reserve(Word.notWord);

        // Operadores relacionais
        reserve(Word.eqWord);
        reserve(Word.neWord);
        reserve(Word.ltWord);
        reserve(Word.leWord);
        reserve(Word.gtWord);
        reserve(Word.geWord);

        // Operadores aritméticos
        reserve(Word.plusWord);
        reserve(Word.minusWord);
        reserve(Word.multWord);
        reserve(Word.divWord);
        reserve(Word.modWord);

        // Atribuições
        reserve(Word.assignWord);
        reserve(Word.plusAssignWord);
        reserve(Word.minusAssignWord);
        reserve(Word.multAssignWord);
        reserve(Word.divAssignWord);

        // Incremento/decremento
        reserve(Word.incWord);
        reserve(Word.decWord);

        // Símbolos
        reserve(Word.lparenWord);
        reserve(Word.rparenWord);
        reserve(Word.lbraceWord);
        reserve(Word.rbraceWord);
        reserve(Word.lbracketWord);
        reserve(Word.rbracketWord);

        // Comentários
        reserve(Word.commentWord);
    }

    // 🔹 Lê um caractere
    void readch() throws IOException {
        int c = System.in.read();
        if (c == -1)
            peek = (char) -1; // EOF
        else
            peek = (char) c;
    }

    // 🔹 Lê e compara um caractere seguinte
    boolean readch(char c) throws IOException {
        readch();
        if (peek != c)
            return false;
        peek = ' ';
        return true;
    }

    // 🔹 Método principal: reconhece o próximo token
    public Token scan() throws IOException {
        // Ignora espaços e tabulações
        for (;; readch()) {
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n') line++;
            else break;
        }

        // 🔹 Fim de arquivo
        if (peek == (char) -1)
            return new Token(Tag.EOF);

        // 🔹 Comentários iniciando com #
        if (peek == '#') {
            do {
                readch();
            } while (peek != '\n' && peek != (char) -1);
            return scan(); // ignora o comentário
        }

        // 🔹 Operadores compostos e simples
        switch (peek) {
            case '=':
                if (readch('=')) return Word.eqWord;
                else { peek = ' '; return Word.assignWord; }

            case '<':
                if (readch('=')) return Word.leWord;
                else { peek = ' '; return Word.ltWord; }

            case '>':
                if (readch('=')) return Word.geWord;
                else { peek = ' '; return Word.gtWord; }

            case '!':
                if (readch('=')) return Word.neWord;
                else { peek = ' '; return Word.notWord; }

            case '+':
                if (readch('+')) return Word.incWord;
                if (readch('=')) return Word.plusAssignWord;
                else { peek = ' '; return Word.plusWord; }

            case '-':
                if (readch('-')) return Word.decWord;
                if (readch('=')) return Word.minusAssignWord;
                else { peek = ' '; return Word.minusWord; }

            case '*':
                if (readch('=')) return Word.multAssignWord;
                else { peek = ' '; return Word.multWord; }

            case '/':
                if (readch('=')) return Word.divAssignWord;
                else { peek = ' '; return Word.divWord; }

            case '%':
                peek = ' ';
                return Word.modWord;

            case '(':
                peek = ' ';
                return Word.lparenWord;

            case ')':
                peek = ' ';
                return Word.rparenWord;

            case '{':
                peek = ' ';
                return Word.lbraceWord;

            case '}':
                peek = ' ';
                return Word.rbraceWord;

            case '[':
                peek = ' ';
                return Word.lbracketWord;

            case ']':
                peek = ' ';
                return Word.rbracketWord;
        }

        // 🔹 Números (inteiros e reais)
        if (Character.isDigit(peek)) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                readch();
            } while (Character.isDigit(peek));

            if (peek != '.') return new Num(v);

            float x = v;
            float d = 10;
            for (;;) {
                readch();
                if (!Character.isDigit(peek)) break;
                x = x + Character.digit(peek, 10) / d;
                d = d * 10;
            }
            return new Real(x);
        }

        // 🔹 Strings (entre aspas)
        if (peek == '"') {
            StringBuilder sb = new StringBuilder();
            readch();
            while (peek != '"' && peek != (char) -1) {
                sb.append(peek);
                readch();
            }
            readch(); // consome o fechamento "
            return new Word(sb.toString(), Tag.STRING);
        }

        // 🔹 Identificadores e palavras reservadas
        if (Character.isLetter(peek)) {
            StringBuilder b = new StringBuilder();
            do {
                b.append(peek);
                readch();
            } while (Character.isLetterOrDigit(peek));

            String s = b.toString();
            Word w = words.get(s);
            if (w != null) return w;

            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        // 🔹 Qualquer outro caractere é token desconhecido
        Token tok = new Token(Tag.UNKNOWN);
        peek = ' ';
        return tok;
    }
}
