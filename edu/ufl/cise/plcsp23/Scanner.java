package edu.ufl.cise.plcsp23.edu.ufl.cise.plcsp23;

import java.util.HashMap;
import java.util.*;


public class Scanner implements IScanner,IToken {

    private SourceLocation sourceLocation;
    private Kind kind;
    private String tokenString;
    private String input;

    private int currentIndex;
    private String now;
    private char current;
    private int currentLine;
    private int currentColumn;
    public static HashMap <String, Kind> reservedWords;
    
    
    //defining states of DFA
    private enum State{
    	START, 
    	HAVE_EQ, 
    	IN_IDENT,
    	IN_NUM_LIT
    }

    static{
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
    }


    public Scanner(String input) {

        now = "";
        this.input = input;
        currentIndex = 0;
        currentLine = 1;
        currentColumn = 0;
        

        }

    public void found(SourceLocation sourceLocation, Kind kind, String tokenString){
        this.sourceLocation = sourceLocation;
        this.kind = kind;
        this.tokenString = tokenString;
    }

    public boolean isWhiteSpace(char c){
        return (c == 32 || c == 13 || c == 10 || c == 9 || c == 12);
    }
    public IToken next() throws LexicalException {return null;}


    @Override
    public SourceLocation getSourceLocation(){
        return sourceLocation;
    }
    @Override
    public Kind getKind(){return kind;}
    @Override
    public String getTokenString(){return tokenString;}


}
