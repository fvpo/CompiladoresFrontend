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

    private int currentIndent = 0;
    private boolean atLineStart = true;

    public Lexer() {
        // 🔹 Inicializa com indentação base 0
        indentStack.push(0);

        // 🔹 Palavras-chave
        reserve(Word.ifWord);
        reserve(Word.elseWord);
        reserve(Word.whileWord);
        reserve(Word.forWord);
        reserve(Word.breakWord);
        reserve(Word.continueWord);
        reserve(Word.returnWord);
        reserve(Word.printWord);

        // 🔹 Blocos
        reserve(Word.seqWord);
        reserve(Word.parWord);

        // 🔹 Tipos
        reserve(Type.stringWord);
        reserve(Type.cchannelWord);
        reserve(Type.voidWord);
        reserve(Type.boolWord);
        reserve(Type.intWord);
        reserve(Type.floatWord);
        reserve(Type.charWord);

        // 🔹 Booleanos
        reserve(Word.trueWord);
        reserve(Word.falseWord);

        // 🔹 Operadores
        reserve(Word.andWord);
        reserve(Word.orWord);
        reserve(Word.notWord);

        // 🔹 Relacionais
        reserve(Word.eqWord);
        reserve(Word.neWord);
        reserve(Word.ltWord);
        reserve(Word.leWord);
        reserve(Word.gtWord);
        reserve(Word.geWord);

        // 🔹 Aritméticos
        reserve(Word.plusWord);
        reserve(Word.minusWord);
        reserve(Word.multWord);
        reserve(Word.divWord);
        reserve(Word.modWord);

        // 🔹 Atribuições
        reserve(Word.assignWord);
        reserve(Word.plusAssignWord);
        reserve(Word.minusAssignWord);
        reserve(Word.multAssignWord);
        reserve(Word.divAssignWord);

        // 🔹 Incremento/decremento
        reserve(Word.incWord);
        reserve(Word.decWord);

        // 🔹 Símbolos
        reserve(Word.lparenWord);
        reserve(Word.rparenWord);
        reserve(Word.lbraceWord);
        reserve(Word.rbraceWord);
        reserve(Word.lbracketWord);
        reserve(Word.rbracketWord);

        // 🔹 Comentário
        reserve(Word.commentWord);
        reserve(Word.extendsWord);
    }

    void reserve(Word w) {
        words.put(w.lexeme, w);
    }

    void readch() throws IOException {
        int c = System.in.read();
        peek = (c == -1) ? (char) -1 : (char) c;
    }

    boolean readch(char c) throws IOException {
        readch();
        if (peek != c) return false;
        peek = ' ';
        return true;
    }

    public Token scan() throws IOException {
        // 🔹 Se houver tokens pendentes (como INDENT/DEDENT), devolve primeiro
        if (!pendingTokens.isEmpty())
            return pendingTokens.poll();

        // 🔹 Se início de linha, mede a indentação
        if (atLineStart) {
            int indentCount = 0;
            while (peek == ' ' || peek == '\t') {
                indentCount += (peek == '\t') ? 4 : 1;
                readch();
            }

            // Se a linha for vazia, ignora
            if (peek == '\n') {
                line++;
                readch();
                return scan();
            }

            int prevIndent = indentStack.peek();

            // Gera tokens de indentação apenas quando muda o nível
            if (indentCount > prevIndent) {
                indentStack.push(indentCount);
                pendingTokens.add(new Token(Tag.INDENT));
            } else if (indentCount < prevIndent) {
                while (!indentStack.isEmpty() && indentStack.peek() > indentCount) {
                    indentStack.pop();
                    pendingTokens.add(new Token(Tag.DEDENT));
                }
            }
            currentIndent = indentCount;
            atLineStart = false;

            if (!pendingTokens.isEmpty())
                return pendingTokens.poll();
        }

        // 🔹 Ignora espaços entre tokens
        for (;; readch()) {
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n') {
                line++;
                atLineStart = true;
                readch();
                return new Token(Tag.NEWLINE);
            } else break;
        }

        // 🔹 Fim de arquivo
        if (peek == (char) -1) {
            // Antes de finalizar, gera DEDENTs restantes
            while (indentStack.size() > 1) {
                indentStack.pop();
                pendingTokens.add(new Token(Tag.DEDENT));
            }
            if (!pendingTokens.isEmpty())
                return pendingTokens.poll();
            return new Token(Tag.EOF);
        }

        // 🔹 Comentário #
        if (peek == '#') {
            do { readch(); } while (peek != '\n' && peek != (char) -1);
            return scan();
        }

        // 🔹 Operadores
        switch (peek) {
            case '=': if (readch('=')) return Word.eqWord; peek=' '; return Word.assignWord;
            case '<': if (readch('=')) return Word.leWord; peek=' '; return Word.ltWord;
            case '>': if (readch('=')) return Word.geWord; peek=' '; return Word.gtWord;
            case '!': if (readch('=')) return Word.neWord; peek=' '; return Word.notWord;
            case '+': if (readch('+')) return Word.incWord; if (readch('=')) return Word.plusAssignWord; peek=' '; return Word.plusWord;
            case '-': if (readch('-')) return Word.decWord; if (readch('=')) return Word.minusAssignWord; peek=' '; return Word.minusWord;
            case '*': if (readch('=')) return Word.multAssignWord; peek=' '; return Word.multWord;
            case '/': if (readch('=')) return Word.divAssignWord; peek=' '; return Word.divWord;
            case '%': peek=' '; return Word.modWord;
            case '(': peek=' '; return Word.lparenWord;
            case ')': peek=' '; return Word.rparenWord;
            case '{': peek=' '; return Word.lbraceWord;
            case '}': peek=' '; return Word.rbraceWord;
            case '[': peek=' '; return Word.lbracketWord;
            case ']': peek=' '; return Word.rbracketWord;
        }

        // 🔹 Números
        if (Character.isDigit(peek)) {
            int v = 0;
            do { v = 10 * v + Character.digit(peek, 10); readch(); }
            while (Character.isDigit(peek));

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

        // 🔹 Strings
        if (peek == '"') {
            StringBuilder sb = new StringBuilder();
            readch();
            while (peek != '"' && peek != (char) -1) {
                sb.append(peek);
                readch();
            }
            readch();
            return new Word(sb.toString(), Tag.TEXT);
        }

        if (Character.isLetter(peek)) {
            StringBuilder b = new StringBuilder();
            do { b.append(peek); readch(); }
            while (Character.isLetterOrDigit(peek));

            String s = b.toString();
            Word w = words.get(s);
            if (w != null) return w;
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        // 🔹 Qualquer outro caractere
        Token tok = new Token(Tag.UNKNOWN);
        peek = ' ';
        return tok;
    }
}
