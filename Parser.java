package edu.ufl.cise.plcsp23;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plcsp23.IToken.Kind;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;

import javax.naming.Name;
import javax.swing.plaf.nimbus.State;

import static edu.ufl.cise.plcsp23.IToken.Kind.*;

public class Parser implements IParser {

    private IScanner scanner;
    private AST first;

    //Always holds current token
    private IToken current;


    //Parser constructor
    //Takes in a scanner that will be used to create a list of tokens
    public Parser(IScanner scanner) {
        this.scanner = scanner;
    }

    protected boolean isKind(Kind... kinds) {
        for(Kind k: kinds) {
            if(k == current.getKind()) {
                return true;
            }
        }
        return false;
    }

    void match(Kind expected) throws PLCException {
        if (current.getKind() == expected) {
            current = scanner.next();
            System.out.println(current.getKind());
        }
        else {
            throw new SyntaxException("Expected token kind " + expected + " different than provided kind " + current.getKind());
        }
    }

    void consume() throws PLCException {
        current = scanner.next();
        System.out.println(current.getKind());
    }
    @Override
    //where the actual parsing occurs
    public AST parse() throws PLCException {

        current = scanner.next();
        System.out.println(current.getKind());

        if (current.getKind() == Kind.EOF) {
            throw new SyntaxException("No input");
        }

        return program();
    }


    //------------------------------------------LEFT HAND PRODUCTION METHODS-----------------------------------------------
    private Program program() throws PLCException {
        IToken firstToken = current;

        Type type = null;
        if(isKind(RES_image, RES_pixel, RES_int, RES_string, RES_void)) {
            type = Type.getType(current);
            consume();
        }
        else {
            throw new SyntaxException("Expected image, pixel, int, string, or void for Type");
        }


        Ident ident = null;
        if(isKind(IDENT)) {
            ident = new Ident(current);
            consume();
        }
        else {
            throw new SyntaxException("Expected Ident");
        }

        List<NameDef> paramList = new ArrayList<>();
        if(isKind(LPAREN)) {
            consume();
            paramList = paramList();
            match(RPAREN);
        }
        else {
            throw new SyntaxException("Expected Left Parenthesis for ParamList");
        }

        Block block = null;
        if(isKind(LCURLY)) {
            block = block();
        }
        else {
            throw new SyntaxException("Expected Left Curly for Block");
        }

        return new Program(firstToken, type, ident, paramList, block);
    }

    private Block block() throws PLCException {
        match(LCURLY);
        IToken firstToken = current;


        List<Declaration> decList = new ArrayList<>();
        List<Statement> statementList = new ArrayList<>();

        while(!isKind(RCURLY)) {
            if(isKind(EOF)){
                throw new SyntaxException("Reached end of file while parsing; missing Right Curly Bracket");
            }
            decList = decList();
            statementList = statementList();
        }

        match(RCURLY);
        return new Block(firstToken, decList, statementList);
    }

    private List<Declaration> decList() throws PLCException {
        List<Declaration> toReturn = new ArrayList<>();

        while(isKind(RES_image, RES_pixel, RES_int, RES_string, RES_void)) {
            toReturn.add(declaration());
            match(DOT);
        }

        return toReturn;
    }

    private ReturnStatement returnStatement() throws PLCException {
        IToken firstToken = current;

        match(COLON);

        Expr expr = null;
        if (!isKind(COLON)) {
            expr = expr();
        }

        match(DOT);

        return new ReturnStatement(firstToken, expr);
    }

    private List<Statement> statementList() throws PLCException {
        List<Statement> toReturn = new ArrayList<>();

        while(isKind(IDENT, RES_write, RES_while, COLON)) {
            if(isKind(COLON)){
                toReturn.add(returnStatement());
                continue;
            }
            toReturn.add(statement());
            match(DOT);
        }

        return toReturn;
    }

    private List<NameDef> paramList() throws PLCException {
        List<NameDef> toReturn = new ArrayList<>();
        if(isKind(RES_image, RES_pixel, RES_int, RES_string, RES_void)) {
            toReturn.add(nameDef());
            while(isKind(COMMA)){
                consume();
                toReturn.add(nameDef());
            }
        }
        return toReturn;
    }

    private NameDef nameDef() throws PLCException {
        IToken firstToken = current;

        Type type = null;
        if(isKind(RES_image, RES_pixel, RES_int, RES_string, RES_void)){
            type = Type.getType(current);
            consume();
        }
        else {
            throw new SyntaxException("Expected image, pixel, int, string, or void for Type");
        }


        Dimension dimension = null;
        if(isKind(LSQUARE)) {
            dimension = dimension();
        }

        Ident ident = null;
        if(isKind(IDENT)) {
            ident = new Ident(current);
            consume();
        }
        else {
            throw new SyntaxException("Expected Ident");
        }

        return new NameDef(firstToken, type, dimension, ident);
    }

    private Declaration declaration() throws PLCException {
        IToken firstToken = current;

        NameDef nameDef = null;
        if(isKind(RES_image, RES_pixel, RES_int, RES_string, RES_void)) {
            nameDef = nameDef();
        }
        else {
            throw new SyntaxException("Expected image, pixel, int, string, or void for NameDef");
        }

        Expr initializer = null;
        if(isKind(ASSIGN)){
            consume();
            initializer = expr();
        }

        return new Declaration(firstToken, nameDef, initializer);
    }

    private Expr expr() throws PLCException{
        if (isKind(RES_if)){
            consume();
            return conditionalExpr();
        }
        return orExpr();
    }

    private Expr conditionalExpr() throws PLCException{
        IToken firstToken = current;
        Expr conditionExpr = expr();
        match(QUESTION);
        Expr thenExpr = expr();
        match(QUESTION);
        Expr elseExpr = expr();
        return new ConditionalExpr(firstToken, conditionExpr, thenExpr, elseExpr);
    }

    private Expr orExpr() throws PLCException{
        IToken firstToken = current;
        Expr left = andExpr();
        Expr right = null;
        while (isKind(OR, BITOR)) {
            Kind op = current.getKind();
            consume();
            right = andExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr andExpr() throws PLCException{
        IToken firstToken = current;
        Expr left = comparisonExpr();
        Expr right = null;
        while (isKind(AND, BITAND)) {
            Kind op = current.getKind();
            consume();
            right = comparisonExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr comparisonExpr() throws PLCException{
        IToken firstToken = current;
        Expr left = powerExpr();
        Expr right = null;
        while (isKind(LT, LE, GT, GE, EQ)) {
            Kind op = current.getKind();
            consume();
            right = powerExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr powerExpr() throws PLCException {
        IToken firstToken = current;
        Expr left = additiveExpr();
        Expr right = null;
        while (isKind(EXP)) {
            Kind op = current.getKind();
            consume();
            right = powerExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr additiveExpr() throws PLCException {
        IToken firstToken = current;
        Expr left = multiplicativeExpr();
        Expr right = null;
        while (isKind(PLUS, MINUS)) {
            Kind op = current.getKind();
            consume();
            right = multiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr multiplicativeExpr() throws PLCException {
        IToken firstToken = current;
        Expr left = unaryExpr();
        Expr right = null;
        while (isKind(TIMES, DIV, MOD)) {
            Kind op = current.getKind();
            consume();
            right = unaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr unaryExpr() throws PLCException {
        IToken firstToken = current;

        if (isKind(BANG, MINUS, RES_sin, RES_cos, RES_atan)) {
            Kind op = current.getKind();
            consume();
            Expr expr = unaryExpr();
            return new UnaryExpr(firstToken, op, expr);
        }

        Expr expr = unaryExprPostfix();
        return expr;
    }

    private Expr unaryExprPostfix() throws PLCException {   //PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
        IToken firstToken = current;

        Expr primaryExpr = primaryExpr();

        PixelSelector p = null;
        ColorChannel c = null;

        if (isKind(LSQUARE)) {
            p = pixelSelector();
        }


        if (isKind(COLON)) {
            consume();
            c = ColorChannel.getColor(current);
            consume();
        }

        if(p == null && c == null){
            return primaryExpr;
        }

        return new UnaryExprPostfix(firstToken, primaryExpr, p, c);
    }

    private Expr primaryExpr() throws PLCException {
        if (isKind(STRING_LIT)) {
            Expr stringLit = new StringLitExpr(current);
            consume();
            return stringLit;
        }
        else if (isKind(NUM_LIT)) {
            Expr numLit = new NumLitExpr(current);
            consume();
            return numLit;
        }
        else if (isKind(IDENT)) {
            Expr identExpr = new IdentExpr(current);
            consume();
            return identExpr;
        }
        else if (isKind(LPAREN)) {
            consume();
            Expr f = expr();

            while(!isKind(RPAREN)) {
                if(isKind(EOF)){
                    throw new SyntaxException("Reached end of file while parsing; missing Right Parentheses");
                }
                f = expr();
            }

            match(RPAREN);
            return f;
        }
        else if(isKind(RES_Z)){
            Expr zExpr = new ZExpr(current);
            consume();
            return zExpr;
        }
        else if(isKind(RES_rand)){
            Expr randomExpr = new RandomExpr(current);
            consume();
            return randomExpr;
        }
        else if(isKind(RES_x, RES_y, RES_a, RES_r)){
            Expr predeclaredVarExpr = new PredeclaredVarExpr(current);
            consume();
            return predeclaredVarExpr;
        }
        else if(isKind(LSQUARE)){
            ExpandedPixelExpr expandedPixel = expandedPixelExpr();
            return expandedPixel;
        }
        else if(isKind(RES_x_cart, RES_y_cart, RES_a_polar, RES_r_polar)) {
            PixelFuncExpr pixelFunc = pixelFuncExpr();
            return pixelFunc;
        }
        throw new SyntaxException("Token not yet implemented or DNE");
    }

    private PixelSelector pixelSelector() throws PLCException {
        match(LSQUARE);
        IToken firstToken = current;

        Expr x = expr();
        match(COMMA);
        Expr y = expr();
        match(RSQUARE);

        return new PixelSelector(firstToken, x, y);
    }

    private ExpandedPixelExpr expandedPixelExpr() throws PLCException {
        match(LSQUARE);
        IToken firstToken = current;

        Expr r = expr();
        match(COMMA);
        Expr g = expr();
        match(COMMA);
        Expr b = expr();
        match(RSQUARE);

        return new ExpandedPixelExpr(firstToken, r, g, b);
    }

    private PixelFuncExpr pixelFuncExpr() throws PLCException {
        IToken firstToken = current;

        Kind function = null;
        if(isKind(RES_x_cart, RES_y_cart, RES_a_polar, RES_r_polar)){
            function = current.getKind();
            consume();
        }
        else {
            throw new SyntaxException("Expected x_cart, y_cart, a_polar, or r_polar");
        }


        PixelSelector selector = null;
        if(isKind(LSQUARE)){
            selector = pixelSelector();
        }
        else {
            throw new SyntaxException("Expected PixelSelector beginning with Left Square Bracket");
        }

        return new PixelFuncExpr(firstToken, function, selector);
    }

    private Dimension dimension() throws PLCException {
        match(LSQUARE);
        IToken firstToken = current;

        Expr width = expr();
        match(COMMA);
        Expr height = expr();
        match(RSQUARE);

        return new Dimension(firstToken, width, height);
    }

    private LValue lValue() throws PLCException {
        IToken firstToken = current;

        Ident ident = null;
        if(isKind(IDENT)){
            ident = new Ident(current);
            consume();
        }
        else{
            throw new SyntaxException("Expected Ident for LValue");
        }


        PixelSelector p = null;
        ColorChannel c = null;

        if (isKind(LSQUARE)) {
            p = pixelSelector();
        }


        if (isKind(COLON)) {
            consume();
            c = ColorChannel.getColor(current);
            consume();
        }

        return new LValue(firstToken, ident, p, c);
    }


    private Statement statement() throws PLCException {
        IToken firstToken = current;

        if(isKind(IDENT)) {
            LValue lValue = lValue();
            match(ASSIGN);
            Expr expr = expr();
            return new AssignmentStatement(firstToken, lValue, expr);
        }
        else if(isKind(RES_write)){
            consume();
            Expr expr = expr();
            return new WriteStatement(firstToken, expr);
        }
        else if(isKind(RES_while)){
            consume();
            Expr expr = expr();
            Block block = block();
            return new WhileStatement(firstToken, expr, block);
        }
        else if(isKind(COLON)){
            consume();
            Expr expr = expr();
            return new ReturnStatement(firstToken, expr);
        }
        else {
            throw new SyntaxException("expected IDENT(LValue), write, colon, or while");
        }
    }
}