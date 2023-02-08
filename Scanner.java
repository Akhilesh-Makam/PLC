package edu.ufl.cise.plcsp23;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.IToken.SourceLocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Scanner implements IScanner {
    private SourceLocation sourceLocation;
    private Kind kind;
    private String tokenString;
    private String input;
    private int currentIndex;
    private int currentLine;
    private int currentColumn;
    private int limit;
    public HashMap<String, IToken.Kind> reservedWords;
    public HashMap<String, Kind> operators;

    //defining states of DFA
    private enum State {
        START,
        OP,
        IN_IDENT,
        IN_NUM_LIT,
        IN_STRING_LIT,
        IN_COMMENT
    }


    public Scanner(String input) {
        this.input = input;
        currentIndex = 0;
        currentLine = 1;
        currentColumn = 1;
        limit = input.length();

        reservedWords = new HashMap<String, Kind>();
        reservedWords.put("image", Kind.RES_image);
        reservedWords.put("pixel", Kind.RES_pixel);
        reservedWords.put("int", Kind.RES_int);
        reservedWords.put("string", Kind.RES_string);
        reservedWords.put("void", Kind.RES_void);
        reservedWords.put("nil", Kind.RES_nil);
        reservedWords.put("load", Kind.RES_load);
        reservedWords.put("display", Kind.RES_display);
        reservedWords.put("write", Kind.RES_write);
        reservedWords.put("x", Kind.RES_x);
        reservedWords.put("y", Kind.RES_y);
        reservedWords.put("a", Kind.RES_a);
        reservedWords.put("r", Kind.RES_r);
        reservedWords.put("X", Kind.RES_X);
        reservedWords.put("Y", Kind.RES_Y);
        reservedWords.put("Z", Kind.RES_Z);
        reservedWords.put("x_cart", Kind.RES_x_cart);
        reservedWords.put("y_cart", Kind.RES_y_cart);
        reservedWords.put("a_polar", Kind.RES_a_polar);
        reservedWords.put("r_polar", Kind.RES_r_polar);
        reservedWords.put("rand", Kind.RES_rand);
        reservedWords.put("sin", Kind.RES_sin);
        reservedWords.put("cos", Kind.RES_cos);
        reservedWords.put("atan", Kind.RES_atan);
        reservedWords.put("if", Kind.RES_if);
        reservedWords.put("while", Kind.RES_while);

        operators = new HashMap<String, Kind>();
        operators.put(".", Kind.DOT);
        operators.put(",", Kind.COMMA);
        operators.put("?", Kind.QUESTION);
        operators.put(":", Kind.COLON);
        operators.put("(", Kind.LPAREN);
        operators.put(")", Kind.RPAREN);
        operators.put("<", Kind.LT);
        operators.put(">", Kind.GT);
        operators.put("[", Kind.LSQUARE);
        operators.put("]", Kind.RSQUARE);
        operators.put("{", Kind.LCURLY);
        operators.put("}", Kind.RCURLY);
        operators.put("=", Kind.ASSIGN);
        operators.put("==", Kind.EQ);
        operators.put("<->", Kind.EXCHANGE);
        operators.put("<=", Kind.LE);
        operators.put(">=", Kind.GE);
        operators.put("!", Kind.BANG);
        operators.put("&", Kind.BITAND);
        operators.put("&&", Kind.AND);
        operators.put("|", Kind.BITOR);
        operators.put("||", Kind.OR);
        operators.put("+", Kind.PLUS);
        operators.put("-", Kind.MINUS);
        operators.put("*", Kind.TIMES);
        operators.put("**", Kind.EXP);
        operators.put("/", Kind.DIV);
        operators.put("%", Kind.MOD);
    }

    public IToken next() throws LexicalException {
        int startLine = currentLine;
        int startColumn = currentColumn;
        tokenString = "";

        if (currentIndex >= limit) {
            kind = Kind.EOF;
            sourceLocation = new SourceLocation(startLine, startColumn);
            return new Token(sourceLocation, kind, tokenString);
        }

        if (input.isBlank()) {
            sourceLocation = new SourceLocation(startLine, startColumn);
            kind = Kind.EOF;
            return new Token(sourceLocation, kind, tokenString);
        }

        State state = State.START;
        tokenString = "";
        char c = input.charAt(currentIndex);


        while (Character.isWhitespace(c) && currentIndex < limit) {
            if (c == ' ') {
                currentIndex++;
                currentColumn++;
            } else if (c == '\n') {
                currentIndex++;
                currentLine++;
                currentColumn = 1;
            } else if (c == '\t') {
                currentIndex++;
                currentColumn += 4;
            }
            if (currentIndex >= limit) {
                kind = Kind.EOF;
                sourceLocation = new SourceLocation(startLine, startColumn);
                return new Token(sourceLocation, kind, tokenString);
            }
            c = input.charAt(currentIndex);
        }
        startLine = currentLine;
        startColumn = currentColumn;

        if (currentIndex >= limit) {
            kind = Kind.EOF;
            sourceLocation = new SourceLocation(startLine, startColumn);
            return new Token(sourceLocation, kind, tokenString);
        }

        if (Character.isDigit(c)) {
            if(c=='0'){
                currentIndex++;
                return new NumLitToken("0", new SourceLocation(currentLine,currentColumn),kind.NUM_LIT);
            }
            state = State.IN_NUM_LIT;
        } else if (Character.isLetter(c) || c == '_') {
            state = State.IN_IDENT;
        } else if (operators.containsKey(c)) {
            state = State.OP;
            kind = operators.get(c);
        } else if (c == '"') {
            state = State.IN_STRING_LIT;
        } else if (c == '~') {
            state = State.IN_COMMENT;
        } else {
            kind = Kind.ERROR;
            sourceLocation = new SourceLocation(startLine, startColumn);
            currentColumn++;
            currentIndex++;
            throw new LexicalException("Unrecognized char entered in input");
        }


        if (state == State.IN_IDENT) {
            tokenString += c;
            currentIndex++;
            while (currentIndex < limit) {
                c = input.charAt(currentIndex);
                if (Character.isLetterOrDigit(c) || c == '_') {
                    tokenString += c;
                    currentIndex++;
                    currentColumn++;
                } else if (Character.isWhitespace(c)) {
                    Kind k = reservedWords.get(tokenString);
                    if (k == null) {
                        kind = Kind.IDENT;
                    } else {
                        kind = k;
                    }
                    sourceLocation = new SourceLocation(startLine, startColumn);
                    return new Token(sourceLocation, kind, tokenString);
                } else {
                    currentIndex++;
                    currentColumn++;
                    throw new LexicalException("Invalid character/number in Identifier");
                }
            }
            if (currentIndex >= limit) {
                sourceLocation = new SourceLocation(startLine, startColumn);
                kind = Kind.IDENT;
                return new Token(sourceLocation, kind, tokenString);

            }
        }
        else if (state == State.IN_NUM_LIT) {
            tokenString += c;
            currentIndex++;
            while (currentIndex < limit) {
                c = input.charAt(currentIndex);
                if (Character.isDigit(c)) {
                    tokenString += c;
                    currentIndex++;
                    currentColumn++;
                } else if (Character.isWhitespace(c)) {
                    kind = Kind.NUM_LIT;
                    sourceLocation = new SourceLocation(startLine, startColumn);
                    long x = Integer.parseInt(tokenString);
                    if (x > 2147483647 || x < -2147483648) {
                        throw new LexicalException("Number out of range for NUM_LIT");
                    } else {
                        return new NumLitToken(tokenString, sourceLocation, kind);
                    }
                } else {
                    throw new LexicalException("Invalid character in NUM_LIT");
                }
            }
        } else if (state == State.OP) {
            if (kind == Kind.ASSIGN) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '=') {
                    tokenString += '=';
                    kind = Kind.EQ;
                    currentIndex += 2;
                    currentColumn += 2;
                } else {
                    currentColumn++;
                    currentIndex++;
                }
            } else if (kind == Kind.LT) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '=') {
                    tokenString += '=';
                    kind = Kind.LE;
                    currentIndex += 2;
                    currentColumn += 2;
                } else if (x < limit && input.charAt(x) == '-') {
                    tokenString += '-';
                    int y = currentIndex + 2;
                    currentIndex++;
                    if (y < limit && input.charAt(y) == '>') {
                        tokenString += '>';
                        kind = Kind.EXCHANGE;
                        currentIndex += 2;
                        currentColumn += 2;
                    } else {
                        currentIndex++;
                        currentColumn++;
                        throw new LexicalException("Unexpected char in Operator");
                    }
                }
            } else if (kind == Kind.GT) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '=') {
                    tokenString += '=';
                    kind = Kind.GE;
                    currentIndex += 2;
                    currentColumn += 2;
                } else {
                    currentIndex++;
                    currentColumn++;
                }
            } else if (kind == Kind.BITAND) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '&') {
                    tokenString += '&';
                    kind = Kind.AND;
                    currentIndex += 2;
                    currentColumn += 2;
                } else {
                    currentColumn++;
                    currentIndex++;
                }
            } else if (kind == Kind.BITOR) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '|') {
                    tokenString += '|';
                    kind = Kind.OR;
                    currentIndex += 2;
                    currentColumn += 2;
                } else {
                    currentColumn++;
                    currentIndex++;
                }
            } else if (kind == Kind.TIMES) {
                int x = currentIndex + 1;
                if (x < limit && input.charAt(x) == '*') {
                    tokenString += '*';
                    kind = Kind.EXP;
                    currentIndex += 2;
                    currentColumn += 2;
                } else {
                    currentColumn++;
                    currentIndex++;
                }
            } else {
                currentColumn++;
                currentIndex++;
            }
            sourceLocation = new SourceLocation(startLine, startColumn);
            return new Token(sourceLocation, kind, tokenString);
        } else if (state == State.IN_STRING_LIT) {
            currentColumn++;
            currentIndex++;
            while (currentIndex < limit) {
                c = input.charAt(currentIndex);
                if (c == '"') {
                    currentIndex++;
                    return new StringLitToken(tokenString,new SourceLocation(startLine,startColumn),kind);
                }
                tokenString += c;
                currentIndex++;
                currentColumn++;
            }
            throw new LexicalException("No closing \" found in String Lit");
        }
        throw new LexicalException("No token found");
    }
}









