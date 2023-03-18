package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{

    /**
     * Constructor initializes final fields
     *
     * @param kind
     * @param pos
     * @param length
     * @param source
     */

    String value = "";

    public StringLitToken(Kind kind, int pos, int length, char[] source) throws LexicalException {
        super(kind, pos, length, source);

        try {
            value = getValue();
        }
        catch (Exception e) {
            throw new LexicalException("Invalid escape sequence.");
        }
    }

    @Override
    public String getValue() {
        String tempString = getTokenString();
        //This has surrounding quotes, so get rid of those
        String noQuotes = tempString.substring(1, length-1);

        String toReturn = "";
        for (int i = 0; i < noQuotes.length(); i++) {
            if (noQuotes.charAt(i) == '\b') {
                toReturn += '\b';
            }
            else if (noQuotes.charAt(i) == '\t') {
                toReturn += '\t';
            }
            else if (noQuotes.charAt(i) == '\n') {
                //force an exception using out of bounds
                i = noQuotes.length();
                noQuotes.charAt(i);
            }
            else if (noQuotes.charAt(i) == '\r') {
                //force an exception using out of bounds
                i = noQuotes.length();
                noQuotes.charAt(i);
            }
            else if (noQuotes.charAt(i) == '\"') {
                toReturn += '\"';
            }
            else if (noQuotes.charAt(i) == '\\') {
                i++;
                if (noQuotes.charAt(i) == 'b') {
                    toReturn += '\b';
                }
                else if (noQuotes.charAt(i) == 't') {
                    toReturn += '\t';
                }
                else if (noQuotes.charAt(i) == 'n') {
                    toReturn += '\n';
                }
                else if (noQuotes.charAt(i) == 'r') {
                    toReturn += '\r';
                }
                else if (noQuotes.charAt(i) == '"') {
                    toReturn += '\"';
                }
                else if (noQuotes.charAt(i) == '\\') {
                    toReturn += '\\';
                }
                else {
                    //force an exception using out of bounds
                    i = noQuotes.length();
                    noQuotes.charAt(i);
                }
            }
            else {
                toReturn += noQuotes.charAt(i);
            }
        }
        return toReturn;
    }
}