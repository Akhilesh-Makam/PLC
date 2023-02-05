package edu.ufl.cise.plcsp23.edu.ufl.cise.plcsp23;

import java.util.HashMap;
import java.io.*;
import java.util.*;
import java.util.ArrayList;


public class Scanner implements IScanner,IToken {
    private String input;
    //would use vector pair but its no longer supported
    Vector<String> found1; //the substring of each token
    Vector<IToken> found2; //the token type, parallel

    private int currentIndex;
    private String now;
    private IToken type;
    private char current;
    private int lineNumber;
    private int columnNumber;


    public Scanner(String input) {
        this.input = input;
        currentIndex = 0;
        lineNumber = 1;
        columnNumber = 1;


        HashMap <String, Kind> reservedWords = new HashMap<String, Kind>();
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

    @Override
    public IToken next() throws LexicalException {
        return null; //not sure how to work this in yet

    }
}
