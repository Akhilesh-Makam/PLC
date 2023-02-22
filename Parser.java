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

    private Scanner scanner;
    private String input;
    private Vector<IToken> tokens;

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

        for(int i = 0; i < tokens.size();i++){
            System.out.println(tokens.get(i).getKind());
        }

        boolean check = legal(tokens);
        if (!check) { //function checks for valid concrete syntax
            throw new SyntaxException("Invalid sequence of tokens");
        }


        return null;
    }

    private boolean legal(Vector<IToken> tokens) { //did not understand the CONSUME stuff so just did switch statements
        int ifCount = 0;
        int questionCount = 0;
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < tokens.size(); i++) {
            IToken token = tokens.get(i);
            switch (token.getKind()) {
                case RES_if:
                    stack.push("ConditionalExpr");
                    ifCount++;
                    break;
                case QUESTION:
                    if (stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign")) {
                        return false;
                    }
                    questionCount++;
                    stack.pop();
                    stack.push("ConditionalExpr");
                    break;
                case OR:
                case BITOR:
                    if (stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign")) {
                        return false;
                    }
                    stack.push("OrSign");
                    break;
                case BITAND:
                case AND:
                    if (stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign")) {
                        return false;
                    }
                    stack.push("AndSign");
                    break;
                case LT:
                case GT:
                case EQ:
                case LE:
                case GE:
                    if (stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign")) {
                        return false;
                    }
                    stack.push("CompareSign");
                    break;
                case PLUS:
                    if(stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                    stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign") || stack.peek().equals("AdditiveSign")){
                        return false;
                    }
                    else{
                        stack.push("AdditiveSign");
                    }
                    break;
                case MINUS:
                    if(!stack.empty() && (stack.peek().equals("MultiExpr") || stack.peek().equals("UnaryExpr") || stack.peek().equals("PrimaryExpr"))){
                        stack.push("AdditiveSign");
                    }
                    else{
                        stack.push("UnarySign");
                    }
                    break;
                case EXP:
                    if(stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign") || stack.peek().equals("AdditiveSign")){
                        return false;
                    }
                    else{
                        stack.push("PowerSign");
                    }
                    break;
                case TIMES:
                case DIV:
                case MOD:
                    if (stack.isEmpty() || stack.peek().equals("UnarySign") || stack.peek().equals("MultiSign") ||
                            stack.peek().equals("PowerSign") || stack.peek().equals("CompareSign")||stack.peek().equals("AndSign")||
                            stack.peek().equals("OrSign")) {
                        return false;
                    }
                    stack.pop();
                    stack.push("MultiSign");
                    break;
                case BANG:
                case RES_sin:
                case RES_cos:
                case RES_atan:
                    stack.push("UnarySign");
                    break;
                case IDENT:
                case NUM_LIT:
                case STRING_LIT:
                case RES_Z:
                case RES_rand:
                    if(stack.isEmpty()){
                        stack.push("PrimaryExpr");
                    }
                    else if(stack.peek().equals("UnarySign")){
                        stack.pop();
                        stack.push("UnaryExpr");
                    }
                    else if(stack.peek().equals("MultiSign")){
                        stack.pop();
                        stack.push("MultiExpr");
                    }
                    else if(stack.peek().equals("AdditiveSign")){
                        stack.pop();
                        stack.push("AdditiveExpr");
                    }
                    else if(stack.peek().equals("PowerSign")){
                        stack.pop();
                        stack.push("PowerExpr");
                    }
                    else if(stack.peek().equals("CompareSign")){
                        stack.pop();
                        stack.push("CompareExpr");
                    }
                    else if(stack.peek().equals("AndSign")){
                        stack.pop();
                        stack.push("AndExpr");
                    }
                    else if(stack.peek().equals("OrSign")){
                        stack.pop();
                        stack.push("OrExpr");
                    }
                    else{
                        stack.push("PrimaryExpr");
                    }
                    break;
                case LPAREN:
                    stack.push("LPAREN");
                    break;
                case RPAREN:
                    if (stack.empty()) {
                        return false;
                    }
                    while(!stack.isEmpty() && !stack.peek().equals("LPAREN")){
                        stack.pop();
                    }
                    stack.pop();
                    stack.push("PrimaryExpr");
                    break;
                case EOF:
                    break;
                default:
                    return false;
            }
        }
        while(!stack.empty()){
            if(stack.peek() == "LPAREN"){
                return false;
            }
            stack.pop();
        }
        if(ifCount * 2 != questionCount){
            return false;
        }
        return true;
    }
}


    /*public Expr expr() throws PLCException {
        //IToken firstToken;
       // firstToken = tokens.get(currentIndex);
        Kind kind = firstToken.getKind();

        if (kind == RES_if) {
           left=condition
        } else {
            Expr left = orExpr();
            Object currentIndex = null;
            if ((tokens.size() > currentIndex) && (tokens.get((Integer) currentIndex).getKind() == QUESTION)) {
                return ConditionalExpr(left);
            } else {
                return left;
            }
        }
    }

    private ConditionalExpr ConditionalExpr(Expr left) throws LexicalException, SyntaxException {
        match(Kind.RES_if);  // match the "if" keyword
        Expr condition = expr();  // parse the condition expression
        match(Kind.QUESTION);  // match the first question mark
        Expr trueExpr = expr();  // parse the true expression
        match(Kind.COLON);  // match the colon
        Expr falseExpr = expr();  // parse the false expression
        return new ConditionalExpr(left.getFirstToken(), condition, trueExpr, falseExpr);
    }

    private Expr orExpr() throws LexicalException, SyntaxException {
        Expr left = andExpr(); // Parse the left operand.
        while (true) {
            if (isKind(BITOR) || isKind(OR)) {
                IToken opToken = consume(); // Consume the OR operator token.
                Expr right = andExpr(); // Parse the right operand.
                left = new BinaryExpr(IToken.Kind, Expr left, Kind op, Expr right); // Create the OR binary expression.
            } else {
                break; // No more OR operators, so return the current expression.
            }
        }
        return left;
    }

    private Expr andExpr() throws SyntaxException, LexicalException {
        Expr left = comparisonExpr();
        while (true) {
            Kind kind = t.kind;
            if (kind == BITAND || kind == AND) {
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

    //consume function gets next token
    private IToken consume() throws SyntaxException, LexicalException {
        IToken token = scanner.next();
        if (token.getKind() != expectedKind) {
            throw new SyntaxException("Expected " + expectedKind + ", but found " + token.getKind(), token.getPosition());
        }
        return token;
    }

    //used textbook chapter 6
    private Expr comparisonExpr() throws SyntaxException, LexicalException {
        Expr expr =term();
        IToken firstToken=t;
        Expr left=null;
        Expr right=null;
        left=additiveExpr();
        while (true) {
            switch (operators) {
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
        while (isKind(TIMES, DIV)){ //can also be match but idk
         Kind op;
            consume();
           expr= new BinaryExpr(expr, op, right);
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
            if (t.getKind() == MINUS || t.getKind() == PLUS) {

                IToken op = t;
                consume(); // consume the operator
                Expr right = multiplicativeExpr();

                left = new BinaryExpr(t, left, op.getKind(), right);
            } else {
                break;
            }

        }
        return left;
    }

   private Expr multiplicativeExpr() throws SyntaxException, LexicalException {
        Expr left = new UnaryExpr();
        Kind op;
        while (isKind(TIMES, DIV, MOD)) {
            IToken op = t;
            consume();
            Expr right = unaryExpr();
            left = new BinaryExpr(op, left, right);
        }
        return left;
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
            case Kind.RES_sin:
            case Kind.RES_cos:
            case Kind.RES_atan:
                op = ((Token) t).kind;
                consume();
                e = new UnaryExpr(firstToken, op, unaryExpr());
                break;
            default:
                e = PrimaryExpr();
                break;
        }
        return e;
    }

     Expr primaryExpr() throws PLCException{
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
                RandomExpr randExpr = new RandomExpr(t);
                consume();
                return randExpr;
            default:
                throw new SyntaxException(t, "Unexpected token");
        }
    }

    public void factor () throws LexicalException, SyntaxException {
        if (isKind(NUM_LIT)) {
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
        IToken t = null;
        return t.getKind() == kind;
    }
    protected boolean isKind(Kind... kinds) {
        for (IToken.Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
   */

