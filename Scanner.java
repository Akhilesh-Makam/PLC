package edu.ufl.cise.plcsp23;
import java.util.Arrays;
import java.util.HashMap;


public class Scanner implements IScanner {

    final String input;
    //array containing input chars, terminated with extra char 0 at end
    final char[] inputChars;

    //invariant ch == inputChars[pos]
    int pos;
    char ch;

    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        pos = 0;
        ch = inputChars[pos];
    }

    @Override
    public IToken next() throws LexicalException {
        if (pos == inputChars.length) {
            return new Token(IToken.Kind.EOF, 0, 0, inputChars);
        }
        else {
            return scanToken();
        }
    }

    public void nextChar() {
        pos++;
        ch = inputChars[pos];
    }

    private enum State {
        START,
        IN_IDENT,
        IN_NUM_LIT,
        IN_STRING_LIT,
        IN_COMMENT,     //so that we can read until \n and throw away
        HAVE_EQ,
        HAVE_GT,        //>, for >=
        HAVE_LT,        //<, for <= and <->
        HAVE_AND,       //&, for &&
        HAVE_OR,        //|, for ||
        HAVE_STAR,       //*
    }

    private static HashMap<String, IToken.Kind> reservedWords;
    static {
        reservedWords = new HashMap<String, IToken.Kind> ();
        reservedWords.put("image", IToken.Kind.RES_image);
        reservedWords.put("pixel", IToken.Kind.RES_pixel);
        reservedWords.put("int", IToken.Kind.RES_int);
        reservedWords.put("string", IToken.Kind.RES_string);
        reservedWords.put("void", IToken.Kind.RES_void);
        reservedWords.put("nil", IToken.Kind.RES_nil);
        reservedWords.put("load", IToken.Kind.RES_load);
        reservedWords.put("display", IToken.Kind.RES_display);
        reservedWords.put("write", IToken.Kind.RES_write);
        reservedWords.put("x", IToken.Kind.RES_x);
        reservedWords.put("y", IToken.Kind.RES_y);
        reservedWords.put("a", IToken.Kind.RES_a);
        reservedWords.put("r", IToken.Kind.RES_r);
        reservedWords.put("X", IToken.Kind.RES_X);
        reservedWords.put("Y", IToken.Kind.RES_Y);
        reservedWords.put("Z", IToken.Kind.RES_Z);
        reservedWords.put("x_cart", IToken.Kind.RES_x_cart);
        reservedWords.put("y_cart", IToken.Kind.RES_y_cart);
        reservedWords.put("a_polar", IToken.Kind.RES_a_polar);
        reservedWords.put("r_polar", IToken.Kind.RES_r_polar);
        reservedWords.put("rand", IToken.Kind.RES_rand);
        reservedWords.put("sin", IToken.Kind.RES_sin);
        reservedWords.put("cos", IToken.Kind.RES_cos);
        reservedWords.put("atan", IToken.Kind.RES_atan);
        reservedWords.put("if", IToken.Kind.RES_if);
        reservedWords.put("while", IToken.Kind.RES_while);
        reservedWords.put("red", IToken.Kind.RES_red);
        reservedWords.put("grn", IToken.Kind.RES_grn);
        reservedWords.put("blu", IToken.Kind.RES_blu);
    }

    private boolean isDigit(int ch) {
        return 48 <= ch && ch <= 57;
    }
    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }
    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '_');
    }
    private void error(String message) throws LexicalException {
        throw new LexicalException("Error at pos " + pos + ": " + message);
    }


    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1; //position of first character
        while(true) {
            switch(state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        case 0 -> {
                            return new Token(IToken.Kind.EOF, tokenStart, 0, inputChars);
                        }
                        //whitespace
                        case 32, 13, 10, 9, 12 -> {    // 32 space, 13 CR \r, 10 LF \n, 9 TAB \t, 12 FF \f
                            nextChar();
                        }
                        //single-character tokens
                        case '0' -> {
                            nextChar();
                            return new NumLitToken(IToken.Kind.NUM_LIT, tokenStart, 1, inputChars);
                        }
                        case '.' -> {
                            nextChar();
                            return new Token(IToken.Kind.DOT, tokenStart, 1, inputChars);
                        }
                        case ',' -> {
                            nextChar();
                            return new Token(IToken.Kind.COMMA, tokenStart, 1, inputChars);
                        }
                        case '?' -> {
                            nextChar();
                            return new Token(IToken.Kind.QUESTION, tokenStart, 1, inputChars);
                        }
                        case ':' -> {
                            nextChar();
                            return new Token(IToken.Kind.COLON, tokenStart, 1, inputChars);
                        }
                        case '(' -> {
                            nextChar();
                            return new Token(IToken.Kind.LPAREN, tokenStart, 1, inputChars);
                        }
                        case ')' -> {
                            nextChar();
                            return new Token(IToken.Kind.RPAREN, tokenStart, 1, inputChars);
                        }
                        case '[' -> {
                            nextChar();
                            return new Token(IToken.Kind.LSQUARE, tokenStart, 1, inputChars);
                        }
                        case ']' -> {
                            nextChar();
                            return new Token(IToken.Kind.RSQUARE, tokenStart, 1, inputChars);
                        }
                        case '{' -> {
                            nextChar();
                            return new Token(IToken.Kind.LCURLY, tokenStart, 1, inputChars);
                        }
                        case '}' -> {
                            nextChar();
                            return new Token(IToken.Kind.RCURLY, tokenStart, 1, inputChars);
                        }
                        case '!' -> {
                            nextChar();
                            return new Token(IToken.Kind.BANG, tokenStart, 1, inputChars);
                        }
                        case '+' -> {
                            nextChar();
                            return new Token(IToken.Kind.PLUS, tokenStart, 1, inputChars);
                        }
                        case '-' -> {
                            nextChar();
                            return new Token(IToken.Kind.MINUS, tokenStart, 1, inputChars);
                        }
                        case '/' -> {
                            nextChar();
                            return new Token(IToken.Kind.DIV, tokenStart, 1, inputChars);
                        }
                        case '%' -> {
                            nextChar();
                            return new Token(IToken.Kind.MOD, tokenStart, 1, inputChars);
                        }
                        case '=' -> {
                            state = State.HAVE_EQ;
                            nextChar();
                        }
                        case '>' -> {
                            state = State.HAVE_GT;
                            nextChar();
                        }
                        case '<' -> {
                            state = State.HAVE_LT;
                            nextChar();
                        }
                        case '&' -> {
                            state = State.HAVE_AND;
                            nextChar();
                        }
                        case '|' -> {
                            state = State.HAVE_OR;
                            nextChar();
                        }
                        case '*' -> {
                            state = State.HAVE_STAR;
                            nextChar();
                        }
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.IN_NUM_LIT;
                            nextChar();
                        }
                        case '"' ->  {
                            state = State.IN_STRING_LIT;
                            nextChar();
                        }
                        case '~' -> {
                            state = State.IN_COMMENT;
                            nextChar();
                        }
                        default -> {
                            if (isIdentStart(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                            }
                            else {
                                error ("illegal char with ascii value: " + (int)ch);
                            }
                        }
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        nextChar();
                    }
                    else {
                        int length = pos-tokenStart;
                        String text = input.substring(tokenStart, tokenStart + length);
                        IToken.Kind kind =  reservedWords.get(text);
                        if (kind == null) {
                            kind = IToken.Kind.IDENT;
                        }
                        return new Token(kind, tokenStart, length, inputChars);
                    }
                }
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {
                        nextChar();
                    }
                    else {
                        int length = pos-tokenStart;
                        return new NumLitToken(IToken.Kind.NUM_LIT, tokenStart, length, inputChars);
                    }
                }
                case IN_STRING_LIT -> {
                    if (ch == '"' && inputChars[pos-1] != '\\') {
                        nextChar();
                        int length = pos-tokenStart;
                        return new StringLitToken(IToken.Kind.STRING_LIT, tokenStart, length, inputChars);
                    }
                    else {
                        nextChar();
                    }
                }
                case HAVE_EQ -> {
                    if (ch == '=') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.EQ, tokenStart, 2, inputChars);
                    }
                    else {
                        return new Token(IToken.Kind.ASSIGN, tokenStart, 1, inputChars);
                    }
                }
                case IN_COMMENT -> {
                    if (ch == 10) {
                        state = State.START;
                        nextChar();
                    }
                    else {
                        nextChar();
                    }
                }
                case HAVE_GT -> {
                    if (ch == '=') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.GE, tokenStart, 2, inputChars);
                    }
                    else {
                        return new Token(IToken.Kind.GT, tokenStart, 1, inputChars);
                    }
                }
                case HAVE_LT -> {
                    if (ch == '=') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.LE, tokenStart, 2, inputChars);
                    }
                    else if (ch == '-') {
                        nextChar();
                        if (ch == '>') {
                            state = State.START;
                            nextChar();
                            return new Token(IToken.Kind.EXCHANGE, tokenStart, 3, inputChars);
                        }
                        else {
                            error ("expected >");
                        }
                    }
                    else {
                        return new Token(IToken.Kind.LT, tokenStart, 1, inputChars);
                    }
                }
                case HAVE_AND -> {
                    if (ch == '&') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.AND, tokenStart, 2, inputChars);
                    }
                    else {
                        return new Token(IToken.Kind.BITAND, tokenStart, 1, inputChars);
                    }
                }
                case HAVE_OR -> {
                    if (ch == '|') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.OR, tokenStart, 2, inputChars);
                    }
                    else {
                        return new Token(IToken.Kind.BITOR, tokenStart, 1, inputChars);
                    }
                }
                case HAVE_STAR -> {
                    if (ch == '*') {
                        state = State.START;
                        nextChar();
                        return new Token(IToken.Kind.EXP, tokenStart, 2, inputChars);
                    }
                    else {
                        return new Token(IToken.Kind.TIMES, tokenStart, 1, inputChars);
                    }
                }
                default -> {
                    throw new UnsupportedOperationException("Bug in Scanner");
                }
            }
        }
    }
}