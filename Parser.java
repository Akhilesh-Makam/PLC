package edu.ufl.cise.plcsp23;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.*;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;

import static edu.ufl.cise.plcsp23.IToken.Kind.*;

public class Parser implements IParser {

    private Scanner scanner;
    private int current;
    private String input;
    private Vector<IToken> tokens;
    private AST first;


    //have to make Parser class and ASTVisitor Class
    public Parser(String input, Scanner scanner) {
        this.scanner = scanner;
        this.input = input;
        current = 0;

    }

    @Override
    public AST parse() throws PLCException {//where the actual parsing occurs
        IToken current = scanner.next();
        tokens = new Vector<>();
        tokens.add(current);
        if (current.getKind() == Kind.EOF) {
            throw new SyntaxException("No input");
        }
        while (current.getKind() != Kind.EOF) {
            current = scanner.next();
            tokens.add(current);
        }

        for(int i = 0; i < tokens.size();i++){
            System.out.println(tokens.get(i).getKind());
        }

//        boolean check = legal(tokens);
//        if (!check) { //function checks for valid concrete syntax
//            throw new SyntaxException("Invalid sequence of tokens");
//        }

        //unable to get the AST generation working

        return expr();
    }

    private boolean match(Kind...expected){
        if (current >= tokens.size()){
            return false;
        }
        for(Kind actual : expected){
            if(check(actual)){
                current++;
                return true;
            }
        }
        return false;
    }

    private boolean check(Kind expected){
        if (current >= tokens.size()){
            return false;
        }
        return tokens.get(current).getKind() == expected;
    }

    private IToken previous(){
        return tokens.get(current - 1);
    }

    private void expect(Kind expected) throws PLCException{
        if(check(expected)==false){
            throw new SyntaxException("Unexpected token");
        }
        current++;
    }
    private Expr expr() throws PLCException{
        if (match(RES_if)){
            return conditionalExpr();
        }
        return orExpr();
    }

    private Expr conditionalExpr() throws PLCException{
        IToken tmp = tokens.get(current);
        Expr condition = expr();
        expect(QUESTION);
        Expr then = expr();
        expect(QUESTION);
        Expr elseExpr = expr();
        return new ConditionalExpr(tmp, condition, then, elseExpr);
    }

    private Expr orExpr() throws PLCException{
        Expr first = andExpr();
        IToken tmp = tokens.get(current);
        while(match(OR) || match(BITOR)){
            Kind op = previous().getKind();
            Expr second = andExpr();
            first = new BinaryExpr(tmp, first, op, second);
            tmp = tokens.get(current);
        }
        return first;
    }

    private Expr andExpr() throws PLCException{
        Expr first = comparisonExpr();
        IToken tmp = tokens.get(current);
        while(match(AND) || match(BITAND)){
            Kind op = previous().getKind();
            Expr second = comparisonExpr();
            first = new BinaryExpr(tmp, first, op, second);
            tmp = tokens.get(current);
        }
        return first;
    }

    private Expr comparisonExpr() throws PLCException{
        Expr first = powerExpr();
        IToken tmp = tokens.get(current);
        while(match(LT,LE,GT,GE)){
            Kind op = previous().getKind();
            Expr second = powerExpr();
            first = new BinaryExpr(tmp, first, op, second);
            tmp = tokens.get(current);
        }
        return first;
    }

    private Expr powerExpr() throws PLCException {
        Expr first = additiveExpr();
        IToken tmp = tokens.get(current);
        if (match(EXP)) {
            Expr right = powerExpr();
            return new BinaryExpr(tmp, first, EXP, right);
        }
        return first;
    }

    private Expr additiveExpr() throws PLCException {
        Expr first = multiplicativeExpr();
        IToken tmp = tokens.get(current);
        while (match(PLUS, MINUS)) {
            Kind op = previous().getKind();
            Expr second = multiplicativeExpr();
            first = new BinaryExpr(tmp, first, op, second);
        }
        return first;
    }

    private Expr multiplicativeExpr() throws PLCException {
        Expr first = unaryExpr();
        IToken tmp = tokens.get(current);
        while (match(TIMES, DIV, MOD)) {
            Kind op = previous().getKind();
            Expr second = unaryExpr();
            first = new BinaryExpr(tmp, first, op, second);
        }
        return first;
    }

    private Expr unaryExpr() throws PLCException {
        if (match(BANG, MINUS, RES_sin, RES_cos, RES_atan)) {
            IToken tmp = tokens.get(current);
            Kind op = previous().getKind();
            Expr expr = unaryExpr();
            return new UnaryExpr(tmp, op, expr);
        }
        return primaryExpr();
    }
    private Expr primaryExpr() throws PLCException {
        if (match(STRING_LIT)) {
            return new StringLitExpr(previous());
        } else if (match(NUM_LIT)) {
            return new NumLitExpr(previous());
        } else if (match(IDENT)) {
            return new IdentExpr(previous());
        } else if (match(LPAREN)) {
            Expr f = expr();
            while(current < tokens.size()){
                if(!match(RPAREN)){
                    f = expr();
                }
                else{
                    return f;
                }
            }
            if(current >= tokens.size()){
                throw new SyntaxException("Expected primary expression");
            }

        }
        else if(match(RES_Z)){
            return new ZExpr(previous());
        }
        else if(match(RES_rand)){
            return new RandomExpr(previous());
        }
        throw new SyntaxException("Token not yet implemented or DNE");
    }
}

