package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.PLCException;

public class CodeGen implements ASTVisitor{

    private int indent;
    private StringBuilder code;
    boolean write;
    boolean rand;
    boolean power;
    boolean returnConditional;

    public CodeGen(){
        indent = 0;
        code = new StringBuilder();
        write = false;
        rand = false;
        power = false;
        returnConditional = false;

    }

    public String indentMaker(){
        String r = "";
        for(int i = 0; i < indent;i++){
            r += "\t";
        }
        return r;
    }

    public String imports(){
        String s = "";
        if(write){
            s += "import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n";
        }
        if(rand){
            s += "import Java.util.Math;\n";
        }
        if(power){
            s += "import java.lang.Math.pow;\n";
        }
        return s;
    }

    public String type(Type e) throws PLCException{
        switch(e){
            case INT -> {
                return "int";
            }
            case VOID -> {
                return "void";
            }
            case IMAGE -> {
                return "Image";
            }
            case PIXEL -> {
                return "pixel";
            }
            case STRING -> {
                return "String";
            }
            default -> {
                throw new PLCException("W");
            }
        }
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        e.append(statementAssign.getLv().visit(this,null)).append(" = ").append(statementAssign.getE().visit(this,null));
        return e;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        if(binaryExpr.getOp() == IToken.Kind.EXP){
            power = true;
        }
        String op = "";
        switch(binaryExpr.getOp()){
            case PLUS -> {
                op = "+";
            }
            case MINUS -> {
                op = "-";
            }
            case TIMES->{
                op = "*";
            }
            case DIV->{
                op = "/";
            }
            case MOD->{
                op = "%";
            }
            case LT->{
                op = "<";
            }
            case GT->{
                op = ">";
            }
            case LE->{
                op = "<=";
            }
            case GE->{
                op = ">=";
            }
            case EQ->{
                op = "==";
            }
            case BITOR -> {
                op = "|";
            }
            case OR->{
                op = "||";
            }
            case BITAND -> {
                op = "&";
            }
            case AND->{
                op = "&&";
            }
            case EXP->{
                op = "**";
            }
            default ->{
                throw new PLCException("OP not allowed");
            }

        }
        StringBuilder s = new StringBuilder();
        return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        StringBuilder blockk = new StringBuilder();

        for(int i = 0;i < block.getDecList().size();i++){

            blockk.append(block.getDecList().get(i).visit(this, null)).append(";\n");

        }

        for(int i = 0;i < block.getStatementList().size();i++){
            blockk.append(block.getStatementList().get(i).visit(this, null)).append(";\n");;
        }
        return blockk.toString();
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
           if(returnConditional){
               return e.append(conditionalExpr.getGuard().visit(this,arg)).
                       append("  != 0 ? ").append(conditionalExpr.getTrueCase().visit(this,arg))
                       .append(" : ").append(conditionalExpr.getFalseCase().visit(this,arg));
           }
        e.append(conditionalExpr.getGuard().visit(this,arg)).
                append(" ? ").append(conditionalExpr.getTrueCase().visit(this,arg))
                .append(" : ").append(conditionalExpr.getFalseCase().visit(this,arg));
        return e;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        StringBuilder dec = new StringBuilder();
        dec.append(declaration.getNameDef().visit(this,arg));
        if(declaration.getInitializer() != null){
            dec.append(" = ").append(declaration.getInitializer().visit(this,arg));
        }
        return dec.toString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return ident.getName();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return identExpr.getName();
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        StringBuilder s = new StringBuilder();
        if(lValue.getPixelSelector() == null && lValue.getColor() == null){
            s.append(lValue.getIdent().visit(this,null));
        }
        return s.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        StringBuilder name = new StringBuilder();
        name.append(type(nameDef.getType())).append(" ").append(nameDef.getIdent().visit(this,arg));

        //not implementing dimensions for assignment 5
        return name.toString();

    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        String e = String.valueOf(numLitExpr.getValue());
        return e;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        code.append("public class ").append(program.getIdent().getName()).append(" {\n");
        code.append(indentMaker()).append("public static ").append(type(program.getType())).append(" apply(");
        if(!program.getParamList().isEmpty()){
            code.append(program.getParamList().get(0).visit(this, null));
            for(int i = 1; i <program.getParamList().size();i++){
                code.append(", ").append(program.getParamList().get(i).visit(this, null));
            }
        }
        code.append(") {\n");
        indent++;
        code.append(indentMaker()).append(program.getBlock().visit(this, null));
        indent--;

        code.append(indentMaker()).append("}\n");
        code.append("}\n");
        return imports() + code.toString();
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        if(!rand){
            rand = true;
        }
        StringBuilder e = new StringBuilder();
        e.append("Math.floor(Math.random()*256)");
        return e.toString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        if(returnStatement.getE().toString().contains("ConditionalExpr")){
            returnConditional = true;
        }
        e.append("return ").append(returnStatement.getE().visit(this,arg));
        return e.toString();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        e.append("\"").append(stringLitExpr.getValue()).append("\"");
        return e;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException { //not implementing this for Assignment 5
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {

        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        if(!write){
            write = true;
        }
        StringBuilder s = new StringBuilder();
        s.append("ConsoleIO.write(").append(statementWrite.getE().visit(this,arg)).append(")");

        return s;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException { //done
        String e = String.valueOf(zExpr.getValue());
        return e;
    }
}