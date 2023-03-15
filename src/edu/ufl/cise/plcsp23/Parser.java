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
    private int current = 0;
    private List<IToken> tokens;
    private AST first;


    //Parser constructor
    //Takes in a scanner that will be used to create a list of tokens
    public Parser(IScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    //where the actual parsing occurs
    public AST parse() throws PLCException {
        IToken current = scanner.next();
        tokens = new ArrayList<>();
        tokens.add(current);
        if (current.getKind() == Kind.EOF) {
            throw new SyntaxException("No input");
        }
        while (current.getKind() != Kind.EOF) {
            current = scanner.next();
            tokens.add(current);
        }

        for(int i = 0; i < tokens.size(); i++){
            System.out.println(tokens.get(i).getKind());
        }

        return program();
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
        if(!check(expected)){
            throw new SyntaxException("Unexpected token");
        }
        current++;
    }

    //------------------------------------------LEFT HAND PRODUCTION METHODS-----------------------------------------------
    private Program program() throws PLCException {
        IToken tmp = tokens.get(current);

        Type type = Type.getType(tokens.get(current));

        Ident ident = null;
        expect(IDENT);
        ident = new Ident(previous());

        List<NameDef> paramList = null;
        expect(LPAREN);
        while(!match(RPAREN)) {
            paramList = paramList();
        }

        Block block = null;
        expect(LCURLY);
        block = block();

        return new Program(tmp, type, ident, paramList, block);
    }

    private Block block() throws PLCException {
        IToken tmp = tokens.get(current);

        List<Declaration> decList = null;
        List<Statement> statementList = null;
        while(!match(RCURLY)) {
            decList = decList();
            statementList = statementList();
        }

        return new Block(tmp, decList, statementList);
    }

    private List<Declaration> decList() throws PLCException {
        List<Declaration> toReturn = null;

        while(check(RES_image) || check(RES_pixel) || check(RES_int) || check(RES_string) || check(RES_void)) {
            toReturn.add(declaration());
            match(DOT);
        }

        return toReturn;
    }

    private List<Statement> statementList() throws PLCException {
        List<Statement> toReturn = null;

        while(check(IDENT) || check(RES_write) || check(RES_while)) {
            toReturn.add(statement());
            match(DOT);
        }

        return toReturn;
    }

    private List<NameDef> paramList() throws PLCException {
        List<NameDef> toReturn = null;
        if(check(RES_image) || check(RES_pixel) || check(RES_int) || check(RES_string) || check(RES_void)) {
            toReturn.add(nameDef());
            while(match(COMMA)){
                toReturn.add(nameDef());
            }
        }
        return toReturn;
    }

    private NameDef nameDef() throws PLCException {
        IToken tmp = tokens.get(current);

        Type type = Type.getType(tokens.get(current));

        Dimension dimension = null;
        if(check(LSQUARE)) {
            dimension = dimension();
        }

        expect(IDENT);
        Ident ident = new Ident(previous());

        return new NameDef(tmp, type, dimension, ident);
    }

    private Declaration declaration() throws PLCException {
        IToken tmp = tokens.get(current);

        NameDef nameDef = null;
        if(check(RES_image) || check(RES_pixel) || check(RES_int) || check(RES_string) || check(RES_void)) {
            nameDef = nameDef();
        }
        else {
            throw new SyntaxException("Expected NameDef");
        }

        Expr initializer = null;
        if(check(ASSIGN)){
            expect(ASSIGN);
            initializer = expr();
        }

        return new Declaration(tmp, nameDef, initializer);
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
        while (match(OR) || match(BITOR)) {
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
        while (match(AND) || match(BITAND)) {
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
        while (match(LT,LE,GT,GE)) {
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
        return unaryExprPostfix();
    }

    private Expr unaryExprPostfix() throws PLCException {   //PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
        IToken tmp = tokens.get(current);

        Expr first = primaryExpr();

        PixelSelector p = null;
        ColorChannel c = null;

        if (match(LSQUARE)) {
            p = pixelSelector();
        }


        if (match(COLON)) {
            c = ColorChannel.getColor(tokens.get(current));
        }

        return new UnaryExprPostfix(tmp, first, p, c);
    }

    private Expr primaryExpr() throws PLCException {
        if (match(STRING_LIT)) {
            return new StringLitExpr(previous());
        }
        else if (match(NUM_LIT)) {
            return new NumLitExpr(previous());
        }
        else if (match(IDENT)) {
            return new IdentExpr(previous());
        }
        else if (match(LPAREN)) {
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
        else if(match(RES_x)){
            return new PredeclaredVarExpr(previous());
        }
        else if(match(RES_y)){
            return new PredeclaredVarExpr(previous());
        }
        else if(match(RES_a)){
            return new PredeclaredVarExpr(previous());
        }
        else if(match(RES_r)){
            return new PredeclaredVarExpr(previous());
        }
        else if(match(LSQUARE)){
            ExpandedPixelExpr expandedPixel = expandedPixelExpr();
            return expandedPixel;
        }
        else if(check(RES_x_cart) || check(RES_y_cart) || check(RES_a_polar) || check(RES_r_polar)){
            PixelFuncExpr pixelFunc = pixelFuncExpr();
            return pixelFunc;
        }
        throw new SyntaxException("Token not yet implemented or DNE");
    }

    private PixelSelector pixelSelector() throws PLCException {
        IToken tmp = tokens.get(current);

        Expr x = expr();
        expect(COMMA);
        Expr y = expr();
        expect(RSQUARE);

        return new PixelSelector(tmp, x, y);
    }

    private ExpandedPixelExpr expandedPixelExpr() throws PLCException {
        IToken tmp = tokens.get(current);

        Expr r = expr();
        expect(COMMA);
        Expr g = expr();
        expect(COMMA);
        Expr b = expr();
        expect(RSQUARE);

        return new ExpandedPixelExpr(tmp, r, g, b);
    }

    private PixelFuncExpr pixelFuncExpr() throws PLCException {
        IToken tmp = tokens.get(current);

        Kind function = null;
        if(check(RES_x_cart) || check(RES_y_cart) || check(RES_a_polar) || check(RES_r_polar)){
            function = tokens.get(current).getKind();
        }
        else {
            throw new SyntaxException("Expected x_cart, y_cart, a_polar, or r_polar");
        }

        expect(LSQUARE);
        PixelSelector selector = pixelSelector();

        return new PixelFuncExpr(tmp, function, selector);
    }

    private Dimension dimension() throws PLCException {
        IToken tmp = tokens.get(current);
        expect(LSQUARE);
        Expr width = expr();
        expect(COMMA);
        Expr height = expr();
        expect(RSQUARE);

        return new Dimension(tmp, width, height);
    }

    private LValue lValue() throws PLCException {
        IToken tmp = tokens.get(current);

        Ident ident = null;
        expect(IDENT);
        ident = new Ident(previous());

        PixelSelector p = null;
        ColorChannel c = null;

        if (match(LSQUARE)) {
            p = pixelSelector();
        }


        if (match(COLON)) {
            c = ColorChannel.getColor(tokens.get(current));
        }

        return new LValue(tmp, ident, p, c);
    }

    private Statement statement() throws PLCException {
        IToken tmp = tokens.get(current);

        if(check(IDENT)) {
            LValue lValue = lValue();
            expect(ASSIGN);
            Expr expr = expr();
            return new AssignmentStatement(tmp, lValue, expr);
        }
        else if(match(RES_write)){
            Expr expr = expr();
            return new WriteStatement(tmp, expr);
        }
        else if(match(RES_while)){
            Expr expr = expr();
            Block block = block();
            return new WhileStatement(tmp, expr, block);
        }
        else {
            throw new SyntaxException("expected IDENT(LValue), write, or while");
        }
    }
}