package edu.ufl.cise.plcsp23;

import java.io.Reader;
import java.util.Vector;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.Scanner;
import edu.ufl.cise.plcsp23.ast.*;

import static edu.ufl.cise.plcsp23.IToken.Kind.OR;

public class Parser implements IParser {
    private Scanner scanner;
    private String input;

    //have to make Parser class and ASTVisitor Class
    public Parser(String input, Scanner scanner){
        this.scanner = scanner;
        this.input = input;
    }

    @Override
    public AST parse() throws PLCException { //where the actual parsing occurs
        Vector<IToken> tokens = new Vector<IToken>();
        IToken current = scanner.next();
        tokens.add(current);
        while(current.getKind() != Kind.EOF){
            current = scanner.next();
            tokens.add(current);
        }
        return null;
    }



    //match function used to see if kind of token matches the expected kind
    private void match(IToken.Kind expectedKind) throws PLCException {
        IToken current = scanner.next();
        if (current.getKind() == expectedKind) {
            scanner.next();
        } else {
            throw new PLCException("Syntax error: expected " + expectedKind + ", found " + current.getKind());
        }
    }
    //used from textbook chapter 6
    private Token peek() {

        return tokens.get(current);
    }
    public AST expr() throws PLCException {
        AST ast = null;
        switch (scanner.peek().getKind()) {
            case OR:
                ast = expr();
                break;
            default:
                notifyAll();
                break;
        }
        return ast;
    }

//implementation used from the slides
void factor(){

}


}
