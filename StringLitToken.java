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
        return '"'+ tokenString + '"';
    }

}
