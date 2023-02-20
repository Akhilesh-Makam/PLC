package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IStringLitToken;

public class StringLitToken implements IStringLitToken {
    String tokenString;
    SourceLocation sourceLocation;
    Kind kind;
    public StringLitToken(String tokenString, SourceLocation sourceLocation, Kind kind){
        this.tokenString = tokenString;
        this.sourceLocation = sourceLocation;
        this.kind = kind;

    }

    public String getValue() {
        String result = ""; //string for processed characters
        if (tokenString.length() == 1) {
            char c = tokenString.charAt(0);
                switch (c) {
                    case 'n':
                        result += "\n";
                        break;
                    case 't':
                        result += "\t";
                        break;
                    case '"':
                        result += "\"";
                        break;
                    case '\\':
                        result += "\\";
                        break;
                    default:
                        // Handling invalid escape sequence
                        result += "\\" + c;
                        break;
                }
             }
        else {
            if (tokenString == "\\") {
                return "\"";
            }

            for (int i = 0; i < tokenString.length(); i++) {
                char c = tokenString.charAt(i);
                if (c == '\\') {
                    i++;
                    c = tokenString.charAt(i);
                    switch (c) {
                        case 'n':
                            result += "\n";
                            break;
                        case 't':
                            result += "\t";
                            break;
                        case '"':
                            result += "\"";
                            break;
                        case '\\':
                            result += "\\";
                            break;
                        default:
                            // Handling invalid escape sequence
                            result += "\\" + c;
                            break;
                    }
                } else {
                    result += c;
                }
            }
        }
        return result;
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

