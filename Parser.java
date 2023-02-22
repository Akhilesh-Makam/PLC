package edu.ufl.cise.plcsp23;

import java.util.Stack;
import java.util.Vector;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.*;
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
import static edu.ufl.cise.plcsp23.IToken.Kind.*;

public class Parser implements IParser {
    IToken t;//holds current token
    private Scanner scanner;
    private String input;


    //have to make Parser class and ASTVisitor Class
    public Parser(String input, Scanner scanner) {
        this.scanner = scanner;
        this.input = input;
    }

    @Override
    public AST parse() throws PLCException {//where the actual parsing occurs
        Vector<IToken> tokens = new Vector<>();
        IToken current = scanner.next();
        tokens.add(current);
        if (current.getKind() == Kind.EOF) {
            throw new SyntaxException("No input");
        }
        while (current.getKind() != Kind.EOF) {
            current = scanner.next();
            tokens.add(current);
        }

        boolean check = legal(tokens);
        if (!check) {
            throw new SyntaxException("Invalid sequence of tokens");
        }
        return null;
    }

    public boolean legal(Vector<IToken> tokens) {
        Stack<IToken> paren = new Stack<>();
        boolean opOrParen = true;
        for (int i = 0; i < tokens.size(); i++) {
            Kind kind = tokens.get(i).getKind();
            if (kind == Kind.LPAREN) {
                if (!opOrParen) {
                    return false;
                }
                paren.push(tokens.get(i));
                opOrParen = true;
            } else if (kind == Kind.RPAREN) {
                if (paren.empty() || paren.peek().getKind() != Kind.LPAREN) {
                    return false;
                }
                paren.pop();
                opOrParen = false;
            } else if (kind == Kind.OR || kind == Kind.BITOR || kind == Kind.AND || kind == Kind.BITAND || kind == LT ||
                    kind == GT || kind == Kind.EQ || kind == Kind.LE || kind == Kind.GE || kind == Kind.EXP ||
                    kind == Kind.PLUS || kind == MINUS || kind == Kind.TIMES || kind == Kind.DIV ||
                    kind == Kind.MOD) {
                if (opOrParen) {
                    return false;
                }
                opOrParen = true;
            } else if (kind == Kind.IDENT || kind == Kind.NUM_LIT || kind == Kind.STRING_LIT
                    || kind == Kind.RES_Z || kind == Kind.RES_rand) {
                if (!opOrParen) {
                    return false;
                }
                opOrParen = false;
            } else {
                return false;
            }
        }
        return paren.empty() && !opOrParen;
    }

    public Expr expr() throws LexicalException, SyntaxException {
        IToken firstToken = tokens.get(currentIndex);
        Kind kind = firstToken.getKind();

        if (kind == Kind.RES_if) {
            return ConditionalExpr();
        } else {
            Expr left = orExpr();
            Object currIndex;
            if (tokens.size() > currentIndex && tokens.get(currentIndex).getKind() == Kind.QUESTION) {
                return ConditionalExpr(left);
            } else {
                return left;
            }
        }
    }

    private ConditionalExpr ConditionalExpr() throws LexicalException, SyntaxException {
        match(Kind.RES_if);  // match the "if" keyword
        Expr condition = expr();  // parse the condition expression
        match(Kind.QUESTION);  // match the first question mark
        Expr trueExpr = expr();  // parse the true expression
        match(Kind.QUESTION);  // match the second question mark
        Expr falseExpr = expr();  // parse the false expression
        return new ConditionalExpr(condition, trueExpr, falseExpr);
    }

    private Expr orExpr() throws LexicalException, SyntaxException {
        Expr left = andExpr(); // Parse the left operand.
        while (true) {
            if (isKind(Kind.BITOR) || isKind(Kind.OR)) {
                IToken opToken = consume(); // Consume the OR operator token.
                Expr right = andExpr(); // Parse the right operand.
                left = new BinaryExpr(left, opToken, right); // Create the OR binary expression.
            } else {
                break; // No more OR operators, so return the current expression.
            }
        }
        return left;
    }

    private Expr andExpr() throws SyntaxException, LexicalException {
        Expr left = andExpr();
        while (true) {
            Kind kind = t.kind;
            if (kind == Kind.BITAND || kind == Kind.AND) {
                consume();
                ComparisonExpr right = comparisonExpr();
                left = new BinaryExpr(left, kind, right);
            } else {
                break;
            }
        }
        return left;
    }

    private boolean match(IToken.Kind kind) {
        return current.getKind() == kind;
    }

    private IToken consume(Kind expectedKind) throws SyntaxException, LexicalException {
        IToken token = scanner.next();
        if (token.getKind() != expectedKind) {
            throw new SyntaxException("Expected " + expectedKind + ", but found " + token.getKind(), token.getPosition());
        }
        return token;
    }

    //used textbook chapter 6
    private Expr comparisonExpr() throws SyntaxException, LexicalException {
        Expr expr =term();
        while (true) {
            switch (IToken.Kind) {
                case LT:
                case GT:
                case EQ:
                case LE:
                case GE:
                    Token op = consume();
                    Expr right = powerExpr();
                    left = new BinaryExpr(left, op, right);
                    break;
                default:
                    return expr;
            }
        }

    }
    //used from parsing 4 slides
    public Expr term(){
      Expr expr= factor();
        while (isKind(TIMES, DIV)){
     //       Kind op;
            consume(op);
           expr= new BinaryExpr(left, op, right);
        }
        return expr;
    }

    private Expr powerExpr() throws SyntaxException, LexicalException {
        Expr left = additiveExpr();
        while (t.kind == Kind.EXP) {
            consume();

            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    private Expr additiveExpr() throws SyntaxException, LexicalException {
        Expr left = multiplicativeExpr();
        while (true) {
            if ((Kind.MINUS) || (Kind.PLUS)) {
                Token op = t;
                consume(); // consume the operator
                Expr right = multiplicativeExpr();
                left = new BinaryExpr(left, op, right);
            } else {
                break;
            }
        }
        return left;
    }

    private Expr multiplicativeExpr() throws SyntaxException, LexicalException {
        Expr left = UnaryExpr();
        while (isKind(TIMES, DIV, MOD)) {
            IToken op = t;
            consume();
            Expr right = unaryExpr();
            left = new BinaryExpr(op, left, right);
        }
        return left;
    }

    private Expr unaryExpr() throws SyntaxException, LexicalException {
        Token firstToken = t;
        Expr e;
        Kind op;
        switch (t.kind) {
            case EXCLAMATION:
                op = t.kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            case MINUS:
                op = t.kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            case Kind.RES_sin:
            case Kind.RES_cos:
            case Kind.RES_atan:
                op = t.kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            default:
                e = primaryExpr();
                break;
        }
        return e;
    }

    private Expr primaryExpr() throws SyntaxException, LexicalException {
        Kind kind = t.kind();
        switch (kind) {
            case STRING_LIT:
                StringLitExpr stringLitExpr = new StringLitExpr(t);
                consume();
                return stringLitExpr;
            case NUM_LIT:
                NumLitExpr numLitExpr = new NumLitExpr(t);
                consume();
                return numLitExpr;
            case IDENT:
                IdentExpr identExpr = new IdentExpr(t);
                consume();
                return identExpr;
            case LPAREN:
                consume();
                Expr expr = expr();
                match(RPAREN);
                return expr;
            case RES_Z:
                ZExpr zExpr = new ZExpr(t);
                consume();
                return zExpr;
            case RES_rand:
                RandExpr randExpr = new RandExpr(t);
                consume();
                return randExpr;
            default:
                throw new SyntaxException(t, "Unexpected token");
        }
    }

    public void factor (){
        if (isKind(INT_LIT)) {
            consume();
        }
        else if(isKind(LPAREN)){
            consume();
            expr();
            match(RPAREN);
        }
        else{
            error();

        }
        return;
    }


    //used from Parsing 4 slides
    protected boolean isKind(Kind kind) {
        IToken t;
        return t.getKind() == kind;
    }
    protected boolean isKind(Kind... kinds) {
        for (IToken.Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
    }
}
