package edu.ufl.cise.plcsp23;

import java.util.Stack;
import java.util.Vector;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.*;

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
    private final int currentIndex;
    IToken t;//holds current token
    private Scanner scanner;
    private String input;
    private Vector <IToken> tokens;
    Expr conditionalExpr;
    Expr leftSide;
    Expr rightSide;
    IToken firstToken;
    int position=0;
IToken current;

    //have to make Parser class and ASTVisitor Class
    public Parser(String input, Scanner scanner) {
        this.scanner = scanner;
        this.input = input;
        this.currentIndex=0;
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

    private boolean legal(Vector<IToken> tokens) {
        Stack<String> stack = new Stack<>();
        System.out.println(stack.empty());
        for (int i = 0; i < tokens.size(); i++) {
            IToken token = tokens.get(i);
            switch (token.getKind()) {
                case RES_if:
                    stack.push("ConditionalExpr");
                    break;
                case QUESTION:
                    if (stack.empty() || !stack.peek().equals("Expr")) {
                        return false;
                    }
                    stack.pop();
                    stack.push("ConditionalExpr");
                    break;
                case COLON:
                    if (stack.empty() || !stack.peek().equals("ConditionalExpr")) {
                        return false;
                    }
                    stack.pop();
                    if (stack.empty() || !stack.peek().equals("Expr")) {
                        return false;
                    }
                    stack.pop();
                    if (stack.empty() || !stack.peek().equals("ConditionalExpr")) {
                        return false;
                    }
                    stack.pop();
                    stack.push("Expr");
                    break;
                case OR:
                case AND:
                case BITOR:
                case BITAND:
                case LT:
                case GT:
                case EQ:
                case LE:
                case GE:
                    if (stack.empty() || !stack.peek().equals("BinaryExpr")) {
                        return false;
                    }
                    stack.pop();
                    if (!stack.empty() && (stack.peek().equals("BinaryExpr") || stack.peek().equals("UnaryExpr"))) {
                        stack.pop();
                    }
                    stack.push("BinaryExpr");
                    break;
                case PLUS:
                case MINUS:
                    if (i == 0 || tokens.get(i - 1).getKind() == Kind.LPAREN) {
                        stack.push("UnaryExpr");
                    } else if (stack.empty() || !stack.peek().equals("BinaryExpr")) {
                        return false;
                    } else {
                        stack.pop();
                        if (!stack.empty() && (stack.peek().equals("BinaryExpr") || stack.peek().equals("UnaryExpr"))) {
                            stack.pop();
                        }
                        stack.push("BinaryExpr");
                    }
                    break;
                case TIMES:
                case DIV:
                case MOD:
                    if (stack.empty() || !stack.peek().equals("UnaryExpr")) {
                        return false;
                    }
                    stack.pop();
                    stack.push("UnaryExpr");
                    break;
                case BANG:
                case RES_sin:
                case RES_cos:
                case RES_atan:
                    stack.push("UnaryExpr");
                    break;
                case IDENT:
                case NUM_LIT:
                case STRING_LIT:
                    break;
                case LPAREN:
                    stack.push("LPAREN");
                    break;
                case RPAREN:
                    if (stack.empty() || !stack.peek().equals("Expr")|| !stack.peek().equals("LPAREN")) {
                        return false;
                    }
                    stack.pop();
                    stack.pop();
                    stack.push("PrimaryExpr");
                    break;
                case RES_Z:
                case RES_rand:
                case EOF:
                    break;
                default:
                    return false;
            }
        }
        return stack.empty();
    }


    public Expr expr() throws PLCException{

        Kind kind = firstToken.getKind();

        if (kind == RES_if) {
          leftSide=conditionalExpr();
        } else {
            leftSide = orExpr();

        }
        return leftSide;
    }

    private Expr conditionalExpr() throws PLCException {
        match(Kind.RES_if);  // match the "if" keyword
        Expr condition = expr();  // parse the condition expression
        match(Kind.QUESTION);  // match the first question mark
        Expr trueExpr = expr();  // parse the true expression
        match(Kind.COLON);  // match the colon
        Expr falseExpr = expr();  // parse the false expression

        return conditionalExpr;
    }

    private Expr orExpr() throws LexicalException, SyntaxException {
        Kind kind = firstToken.getKind();
        leftSide = andExpr(); // Parse the left operand.
        while (kind==Kind.BITOR ||kind== Kind.OR) {
            consume();
            rightSide = andExpr(); // Parse the right operand.
            return leftSide;
        }
        return leftSide;
    }
    private Expr andExpr() throws SyntaxException, LexicalException {
        Kind kind = firstToken.getKind();
        while  (kind == Kind.BITAND || kind == Kind.AND) {
            {
                consume();
                 rightSide = comparisonExpr();

            }
        }
        return leftSide;
    }

    private boolean match(Kind kind) {
        return current.getKind() == kind;
    }

    //consume function gets next token
    private IToken consume() throws SyntaxException, LexicalException {
        IToken token = scanner.next();
        if (token.getKind() != expected) {

        }
        return token;
    }

    //used textbook chapter 6
    private Expr comparisonExpr() throws SyntaxException, LexicalException {
        // Expr expr =term();


        leftSide=additiveExpr();
        //while (true) {
            switch (firstToken.getKind()) {
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




    private Expr powerExpr() throws PLCException {
       Kind kind=firstToken.getKind();
        leftSide=additiveExpr();
        while (kind == IToken.Kind.EXP) {
            consume();

            rightSide=multiplicativeExpr();
        }
        return leftSide;
    }
    private Expr additiveExpr() throws PLCException {
        Expr left = multiplicativeExpr();
        Kind kind=firstToken.getKind();
        while (kind == kind.MINUS || kind==kind.PLUS){
            consume(); // consume the operator
            rightSide = multiplicativeExpr();

        }
        return leftSide;
    }
    private Expr multiplicativeExpr() throws SyntaxException, LexicalException {
     Kind kind=firstToken.getKind();
        while(kind==kind.TIMES||kind==IToken.Kind.DIV|| kind==IToken.Kind.MOD) {

            consume();
             rightSide = unaryExpr();

        }
        return leftSide;
    }

    private Expr unaryExpr() throws SyntaxException, LexicalException {
        Token firstToken = (Token) t;
        Expr e;
        Kind op;
        switch (((Token) t).kind) {
            case BANG:
                op = ((Token) t).kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            case MINUS:
                op = ((Token) t).kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            case RES_sin:
            case RES_cos:
            case RES_atan:
                op = ((Token) t).kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            default:
                e = primaryExpr();
                break;
        }
       return e;
    }

   Expr primaryExpr() throws PLCException{
        Kind kind = firstToken.getKind();
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
                RandomExpr randExpr = new RandomExpr(t);
                consume();
                return randExpr;
            default:
                throw new SyntaxException(t, "Unexpected token");
        }
    }




    //used from Parsing 4 slides
    protected boolean isKind(Kind kind) {
        IToken t = null;
        return t.getKind() == kind;
    }
    protected boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
    }
}
