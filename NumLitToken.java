package edu.ufl.cise.plcsp23;

public class NumLitToken implements INumLitToken{
    int value;
    String tokenString;
    SourceLocation sourceLocation;
    Kind kind;
    public NumLitToken(String tokenString, SourceLocation sourceLocation, Kind kind){
        this.tokenString = tokenString;
        value = Integer.parseInt(tokenString);
        this.sourceLocation = sourceLocation;
        this.kind = kind;

    }

    public int getValue(){
        return value;
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
        return tokenString;
    }
}
