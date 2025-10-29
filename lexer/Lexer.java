package lexer;

import symbols.Type;
import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1;

    private char peek = ' ';
    private final Hashtable<String, Word> words = new Hashtable<>();
    private final Stack<Integer> indentStack = new Stack<>();
    private final Queue<Token> pendingTokens = new LinkedList<>();

    private final Reader reader;
    private boolean atLineStart = true;

    public Lexer(Reader reader) {
        this.reader = reader;

        // Indentação inicial (nível base 0)
        indentStack.push(0);

        // Palavras-chave
        reserve(Word.ifWord);
        reserve(Word.elseWord);
        reserve(Word.whileWord);
        reserve(Word.forWord);
        reserve(Word.breakWord);
        reserve(Word.continueWord);
        reserve(Word.returnWord);
        reserve(Word.printWord);

        // Blocos
        reserve(Word.seqWord);
        reserve(Word.parWord);

        // Tipos
        reserve(Type.stringWord);
        reserve(Type.cchannelWord);
        reserve(Type.voidWord);
        reserve(Type.boolWord);
        reserve(Type.intWord);
        reserve(Type.floatWord);
        reserve(Type.charWord);

        // Booleanos
        reserve(Word.trueWord);
        reserve(Word.falseWord);

        // Operadores lógicos
        reserve(Word.andWord);
        reserve(Word.orWord);
        reserve(Word.notWord);

        // Relacionais
        reserve(Word.eqWord);
        reserve(Word.neWord);
        reserve(Word.ltWord);
        reserve(Word.leWord);
        reserve(Word.gtWord);
        reserve(Word.geWord);

        // Aritméticos
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

    /** Registra palavra reservada na tabela. */
    private void reserve(Word w) {
        words.put(w.lexeme, w);
    }

    /** Lê o próximo caractere. */
    private void readch() throws IOException {
        int c = reader.read();
        peek = (c == -1) ? (char) -1 : (char) c;
    }

    /** Lê e verifica o próximo caractere sem perder o atual. */
    private boolean readch(char c) throws IOException {
        readch();
        if (peek == c) {
            peek = ' ';
            return true;
        }
        return false;
    }

    /** Lê o próximo token. */
    public Token scan() throws IOException {
        // Retorna tokens pendentes (INDENT/DEDENT)
        if (!pendingTokens.isEmpty()) return pendingTokens.poll();

        // Detecta indentação no início da linha
        if (atLineStart) {
            int indentCount = 0;

            while (peek == ' ' || peek == '\t') {
                indentCount += (peek == '\t') ? 4 : 1;
                readch();
            }

            if (peek == '\n') { // linha vazia → ignora
                readch();
                line++;
                return scan();
            }

            int prevIndent = indentStack.peek();
            if (indentCount > prevIndent) {
                indentStack.push(indentCount);
                pendingTokens.add(new Token(Tag.INDENT));
            } else if (indentCount < prevIndent) {
                while (!indentStack.isEmpty() && indentStack.peek() > indentCount) {
                    indentStack.pop();
                    pendingTokens.add(new Token(Tag.DEDENT));
                }
            }

            atLineStart = false;
            if (!pendingTokens.isEmpty()) return pendingTokens.poll();
        }

        // Ignora espaços intermediários
        for (; ; readch()) {
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n') {
                line++;
                atLineStart = true;
                readch();
                return new Token(Tag.NEWLINE);
            } else break;
        }

        // Fim de arquivo
        if (peek == (char) -1) {
            while (indentStack.size() > 1) {
                indentStack.pop();
                pendingTokens.add(new Token(Tag.DEDENT));
            }
            if (!pendingTokens.isEmpty()) return pendingTokens.poll();
            return new Token(Tag.EOF);
        }

        // Comentário #
        if (peek == '#') {
            do readch(); while (peek != '\n' && peek != (char) -1);
            return scan();
        }

        // Operadores
        switch (peek) {
            case '=': readch(); if (peek == '=') { peek = ' '; return Word.eqWord; } return Word.assignWord;
            case '<': readch(); if (peek == '=') { peek = ' '; return Word.leWord; } return Word.ltWord;
            case '>': readch(); if (peek == '=') { peek = ' '; return Word.geWord; } return Word.gtWord;
            case '!': readch(); if (peek == '=') { peek = ' '; return Word.neWord; } return Word.notWord;
            case '+':
                readch();
                if (peek == '+') { peek = ' '; return Word.incWord; }
                if (peek == '=') { peek = ' '; return Word.plusAssignWord; }
                return Word.plusWord;
            case '-':
                readch();
                if (peek == '-') { peek = ' '; return Word.decWord; }
                if (peek == '=') { peek = ' '; return Word.minusAssignWord; }
                return Word.minusWord;
            case '*': readch(); if (peek == '=') { peek = ' '; return Word.multAssignWord; } return Word.multWord;
            case '/': readch(); if (peek == '=') { peek = ' '; return Word.divAssignWord; } return Word.divWord;
            case '%': peek = ' '; return Word.modWord;
            case '(': peek = ' '; return Word.lparenWord;
            case ')': peek = ' '; return Word.rparenWord;
            case '{': peek = ' '; return Word.lbraceWord;
            case '}': peek = ' '; return Word.rbraceWord;
            case '[': peek = ' '; return Word.lbracketWord;
            case ']': peek = ' '; return Word.rbracketWord;
        }

        // Números
        if (Character.isDigit(peek)) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                readch();
            } while (Character.isDigit(peek));

            if (peek != '.') return new Num(v);

            float x = v, d = 10;
            for (;;) {
                readch();
                if (!Character.isDigit(peek)) break;
                x += Character.digit(peek, 10) / d;
                d *= 10;
            }
            return new Real(x);
        }

        // Strings
        if (peek == '"') {
            StringBuilder sb = new StringBuilder();
            readch();
            while (peek != '"' && peek != (char) -1) {
                sb.append(peek);
                readch();
            }
            readch(); // consome o fechamento
            return new Word(sb.toString(), Tag.TEXT);
        }

        // Identificadores e palavras reservadas
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

        // Qualquer outro caractere → token desconhecido
        Token tok = new Token(Tag.UNKNOWN);
        peek = ' ';
        return tok;
    }
}