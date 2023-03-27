package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.TypeCheckException;

import java.util.HashMap;


public class ASTVisitorX implements ASTVisitor{

    public static class SymbolTable{
        HashMap<String, Declaration> entries = new HashMap<>();

        public boolean insert(String name, Declaration declaration){
            return (entries.putIfAbsent(name, declaration) == null);
        }

        public Declaration lookup(String name){
            return entries.get(name);
        }
    }

    SymbolTable symbolTable = new SymbolTable();

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
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
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
        return null;
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
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }
}
