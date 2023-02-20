package edu.ufl.cise.plcsp23;

import java.io.Reader;
import java.util.Vector;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.Scanner;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.ZExpr;

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
}
