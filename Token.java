package edu.ufl.cise.plcsp23;



public class Token implements IToken { //base token class
    SourceLocation sourceLocation;
    Kind kind;
    String tokenString;
    public Token(SourceLocation sourceLocation, Kind kind, String tokenString){
        this.sourceLocation = sourceLocation;
        this.kind = kind;
        this.tokenString = tokenString;
    }

    public SourceLocation getSourceLocation(){
        return sourceLocation;
    }
    public Kind getKind(){return kind;}
    public String getTokenString(){return tokenString;}
}
