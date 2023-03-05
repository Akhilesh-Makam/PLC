package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {

    /**
     * Constructor initializes final fields
     *
     * @param kind
     * @param pos
     * @param length
     * @param source
     */
    int value;

    public NumLitToken(Kind kind, int pos, int length, char[] source) throws LexicalException {
        super(kind, pos, length, source);

        try {
            value = getValue();
        }
        catch (NumberFormatException e) {
            throw new LexicalException("NumLit too large.");
        }
    }

    @Override
    public int getValue() {
        return Integer.parseInt(getTokenString());
    }
}
