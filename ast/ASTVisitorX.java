package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.TypeCheckException;

import java.util.HashMap;
import java.util.List;


public class ASTVisitorX implements ASTVisitor{

    public static class SymbolTable{
        HashMap<String, NameDef> entries = new HashMap<>();

        public boolean insert(String name, NameDef nameDef){
            return (entries.putIfAbsent(name, nameDef) == null);
        }

        public NameDef lookup(String name){
            return entries.get(name);
        }
    }

    SymbolTable symbolTable = new SymbolTable();
    Type programType;

    private void check(boolean condtion, AST node, String message) throws PLCException{
        if(!condtion){
            throw new TypeCheckException(message + node.getColumn() + node.getLine());
        }
    }
    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
       Token.Kind op = binaryExpr.getOp();
       Type left = (Type) binaryExpr.getLeft().visit(this, arg);
       Type right = (Type) binaryExpr.getRight().visit(this, arg);
       Type result = null;
       switch(op){
           case BITAND, BITOR ->{
               check((left == Type.PIXEL) && (right == Type.PIXEL),binaryExpr, "incompatible types with given operator");
               result = Type.PIXEL;
           }
           case AND, OR, LT, GT, LE, GE ->{
               check((left == Type.INT) && (right == Type.INT),binaryExpr, "incompatible types with given operator");
               result = Type.INT;
           }
           case EQ ->{
               check((left == Type.INT && right == Type.INT) || (left == Type.PIXEL && right == Type.PIXEL)
                       || (left == Type.IMAGE && right == Type.IMAGE) || (left == Type.STRING && right == Type.STRING),
                       binaryExpr, "incompatible types with given operator");
               result = Type.INT;
           }
           case EXP->{
               check((left == Type.INT && right == Type.INT) || (left == Type.PIXEL && right == Type.INT)
                               || (left == Type.INT && right == Type.PIXEL), binaryExpr,
                       "incompatible types with given operator");
               if(left == Type.PIXEL || right == Type.PIXEL){
                   result = Type.PIXEL;
               }
               else{
                   result = Type.INT;
               }
           }
           case PLUS -> {
               check((left == Type.INT && right == Type.INT) || (left == Type.PIXEL && right == Type.PIXEL)
                               || (left == Type.IMAGE && right == Type.IMAGE) || (left == Type.STRING && right == Type.STRING),
                       binaryExpr, "incompatible types with given operator");
               result = left;
           }
           case MINUS -> {
               check((left == Type.INT && right == Type.INT) || (left == Type.PIXEL && right == Type.PIXEL)
                               || (left == Type.IMAGE && right == Type.IMAGE),
                       binaryExpr, "incompatible types with given operator");
               result = left;
           }
           case TIMES, DIV, MOD -> {
               check((left == Type.INT && right == Type.INT) || (left == Type.PIXEL && right == Type.PIXEL)
                               || (left == Type.IMAGE && right == Type.IMAGE) || (left == Type.PIXEL && right == Type.INT)
                               || (left == Type.INT && right == Type.PIXEL) || (left == Type.IMAGE && right == Type.INT) ||
                               (left == Type.INT && right == Type.IMAGE)
                       ,
                       binaryExpr, "incompatible types with given operator");
               if(left == Type.PIXEL || right == Type.PIXEL){
                   result = Type.PIXEL;
               }
               else if(left == Type.IMAGE || right == Type.IMAGE){
                   result = Type.IMAGE;
               }
               else{
                   result = left;
               }
           }
           default -> {
               throw new TypeCheckException("Incompatible operator in BinaryExpr");
           }
       }
       binaryExpr.setType(result);
       return result;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> decList = block.getDecList();
        List<Statement> stateList = block.getStatementList();
        for(Declaration dec : decList){
            dec.visit(this, arg);
        }
        for(Statement state : stateList){
            state.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type x = (Type) conditionalExpr.getGuard().visit(this, arg);
        Type y = (Type) conditionalExpr.getTrueCase().visit(this, arg);
        Type z = (Type) conditionalExpr.getFalseCase().visit(this, arg);
        check((x == Type.INT) && (y == z), conditionalExpr, "incorrect Expr in conditionalExpr");
        return y;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Type x = (Type) dimension.getHeight().visit(this, arg);
        Type y = (Type) dimension.getWidth().visit(this,arg);
        check(x == Type.INT && y == Type.INT, dimension, "invalid dimension input");
        return x;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Type r = (Type) expandedPixelExpr.redExpr.visit(this, arg);
        Type b = (Type) expandedPixelExpr.bluExpr.visit(this, arg);
        Type g = (Type) expandedPixelExpr.grnExpr.visit(this, arg);
        check(r == b && b == g, expandedPixelExpr, "Invalid red, blue, or green for ExpandedPixelExpr");
        expandedPixelExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException { //have to change later for scope, confused on return
        check(symbolTable.lookup(identExpr.getName()) != null, identExpr, "IdentExpr not found in symbol table");
        //identExpr.setType();
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        if(nameDef.getDimension() != null){
            check(nameDef.getType() == Type.IMAGE, nameDef, "NameDef has dimensions with incorrect type");
            Type r = (Type) nameDef.getDimension().visit(this, arg);
        }
        check(nameDef.getType() != Type.VOID, nameDef, "NameDef cannot be void");
        if(symbolTable.lookup(nameDef.getIdent().getName()) == null){
            symbolTable.insert(nameDef.getIdent().getName(), nameDef);
            return null;
        }
        else{
            throw new TypeCheckException("Ident already exists");
        }
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        Type x = (Type) pixelFuncExpr.getSelector().visit(this, arg);
        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        Type x = (Type) pixelSelector.x.visit(this, arg);
        Type y = (Type) pixelSelector.y.visit(this,arg);
        check(x==y, pixelSelector, "invalid x and y for pixelSelector");
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        programType = program.getType();
        return null;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType((Type.INT));
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
       stringLitExpr.setType(Type.STRING);
       return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Token.Kind op = unaryExpr.getOp();
        Type x = (Type) unaryExpr.getE().visit(this, arg);
        Type result = null;
        switch(op){
            case BANG -> {
                check(x == Type.INT || x == Type.PIXEL, unaryExpr, "invalid type in UnaryExpr");
                result = x;
            }
            case MINUS, RES_cos, RES_sin, RES_atan -> {
                check(x == Type.INT, unaryExpr, "invalid type in UnaryExpr");
                result = x;
            }
            default -> {
                throw new TypeCheckException("Invalid UnaryExpr operator");
            }
        }
        unaryExpr.setType(result);
        return result;

    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        Type x = (Type) statementWrite.getE().visit(this, arg);
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }
}
