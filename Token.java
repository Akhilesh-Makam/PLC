package edu.ufl.cise.plcsp23;

public class Token implements IToken {

    final Kind kind;
    final int pos;
    final int length;
    final char[] source;

    /**
     * Constructor initializes final fields
     *
     */
    public Token(Kind kind, int pos, int length, char[] source) throws LexicalException {
        this.kind = kind;
        this.pos = pos;
        this.length = length;
        this.source = source;
    }

    public SourceLocation getSourceLocation() {
        int line = 1;
        int col = 1;
        for(int i = 0; i < source.length; i++) {
            if (i == pos) {
                return new SourceLocation(line, col);
            }
            else if (source[i] == 10) {
                line++;
                col = 1;
            }
            else {
                col++;
            }
        }
        return new SourceLocation(0, 0);
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * returns the characters from the source belonging to the token
     *
     */
    public String getTokenString() {
        String toReturn = "";
        for (int i = pos; i < pos+length; i++) {
            toReturn += source[i];
        }
        return toReturn;
    }

    /**
     * prints token, used during development
     *
     */
    @Override public String toString() {
        String toReturn = "";
        toReturn += kind + " " + pos + " " + length + " " + getTokenString();
        return toReturn;
    }
}