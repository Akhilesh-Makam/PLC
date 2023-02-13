package edu.ufl.cise.plcsp23;

public class StringLitToken implements IStringLitToken{
    String tokenString;
    SourceLocation sourceLocation;
    Kind kind;
    public StringLitToken(String tokenString, SourceLocation sourceLocation, Kind kind){
        this.tokenString = tokenString;
        this.sourceLocation = sourceLocation;
        this.kind = kind;

    }

    public String getValue(){
        for (int i = 0; i < tokenString.length(); i++) {
            char c = tokenString.charAt(i);
            if (c == '\\') {
                i++;
                c = tokenString.charAt(i);
                switch (c) {
                    case 'n':
                        tokenString.concat("\n");
                        break;
                    case 't':
                        tokenString.concat("\t");
                        break;
                    case '"':
                        tokenString.concat("\"");
                        break;
                    case '\\':
                        tokenString.concat("\\");
                        break;
                    default:
                        // Handling invalid escape sequence
                        tokenString.concat("\\").concat(String.valueOf(c));
                        break;
                }
            } else {
                tokenString.substring(c);
            }
        }
        return tokenString;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getTokenString() {
        return '"'+tokenString+'"';
    }

}

