package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.DynamicClassLoader;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.DynamicCompiler;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.InMemoryBytecodeObject;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.InMemoryClassFileManager;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.StringJavaFileObject;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;




public class CodeGen implements ASTVisitor{

    HashMap<String, Type> idents;
    private int indent;
    private StringBuilder code;
    boolean write;
    boolean pixel;
    boolean file;
    boolean rand;
    boolean image;
    boolean returnConditional;
    boolean inReturn;
    boolean dec;
    String returnType;

    public CodeGen(){
        indent = 0;
        code = new StringBuilder();
        write = false;
        rand = false;
        returnConditional = false;
        inReturn = false;
        dec = false;
        idents = new HashMap<>();
        pixel = false;
        file = false;
        image = false;
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
        if(pixel){
            s += "import edu.ufl.cise.plcsp23.runtime.PixelOps;\n";
        }
        if(file){
            s += "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n";
        }
        if(image){
            s += "import edu.ufl.cise.plcsp23.runtime.ImageOps;\n";
            s += "import java.awt.image.BufferedImage;\n";
            s += "import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n";
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
                return "image";
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
        if(idents.containsKey(statementAssign.getLv().getIdent().getName()) && idents.get(statementAssign.getLv().getIdent().getName()) == Type.STRING && statementAssign.getE().toString().contains("NumLitExpr")){
            return e.append(statementAssign.getLv().visit(this,null)).append(" = String.valueOf(").append(statementAssign.getE().visit(this,null)).append(");\n");
        }
        e.append(statementAssign.getLv().visit(this,null)).append(" = ").append(statementAssign.getE().visit(this,null)).append(";\n");
        return e;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        boolean compeq = false;
        boolean comp = false;
        boolean compeq2 = false;
        StringBuilder s = new StringBuilder();
        String op = "";
        switch(binaryExpr.getOp()){
            case PLUS -> {
                op = "+";
                return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
            }
            case MINUS -> {
                op = "-";
                return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
            }
            case TIMES->{
                op = "*";
                return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
            }
            case DIV->{
                op = "/";
                return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
            }
            case MOD->{
                op = "%";
                return s.append(binaryExpr.getLeft().visit(this,arg)).append(" "+ op +" ").append(binaryExpr.getRight().visit(this,arg));
            }
            case LT->{
                op = "<";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append(") ? 1 : 0)");
            }
            case GT->{
                op = ">";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append(") ? 1 : 0)");
            }
            case LE->{
                op = "<=";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append(") ? 1 : 0)");
            }
            case GE->{
                op = ">=";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append(") ? 1 : 0)");
            }
            case EQ->{
                op = "==";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append("  " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append(" ) ? 1 : 0)");
            }
            case BITOR, OR -> {
                op = "||";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" != 0 " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append("!= 0) ? 1 : 0)");
            }
            case BITAND, AND -> {
                op = "&&";
                return s.append("(("+ binaryExpr.getLeft().visit(this,arg)).append(" != 0 " + op + " ").append(binaryExpr.getRight().visit(this,arg))
                        .append("!= 0) ? 1 : 0)");
            }
            case EXP->{
                op = "**";
                return s.append("(int) Math.pow(").append(binaryExpr.getLeft().visit(this,arg)).append(", ")
                        .append(binaryExpr.getRight().visit(this,arg)).append(")");
            }
            default ->{
                throw new PLCException("OP not allowed");
            }

        }
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        StringBuilder blockString = new StringBuilder();

        for(int i = 0;i < block.getDecList().size();i++){
            dec = true;
            blockString.append(block.getDecList().get(i).visit(this, null)).append(";\n");
        }
        dec = false;
        for(int i = 0;i < block.getStatementList().size();i++){
            blockString.append(block.getStatementList().get(i).visit(this, null));
        }
        return blockString.toString();
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        if(returnConditional){
            return e.append(conditionalExpr.getGuard().visit(this,arg))
                    .append("  != 0 ? ").append(conditionalExpr.getTrueCase().visit(this,arg))
                    .append(" : ").append(conditionalExpr.getFalseCase().visit(this,arg));
        }
        e.append("((").append(conditionalExpr.getGuard().visit(this,arg)).append(") != 0)").
                append(" ? ").append(conditionalExpr.getTrueCase().visit(this,arg))
                .append(" : ").append(conditionalExpr.getFalseCase().visit(this,arg));
        return e;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        idents.put(declaration.getNameDef().getIdent().getName(), declaration.getNameDef().getType());
        StringBuilder dec = new StringBuilder();
        dec.append(declaration.getNameDef().visit(this,arg));
        Type x = declaration.getNameDef().getType();
        if(x == Type.PIXEL){
            pixel = true;
            return dec.append(" = ").append(declaration.getInitializer().visit(this,arg));
        }
        if(x == Type.IMAGE){
            if(declaration.getNameDef().getDimension() == null){
                if(declaration.getInitializer().getType() == Type.STRING){
                    return dec.append(" = FileURLIO.readImage(").append(declaration.getInitializer().visit(this,arg) + ")");
                }
            }
        }
        if(declaration.getInitializer() != null){
            if(x == Type.STRING){
                return dec.append(" = String.valueOf(").append(declaration.getInitializer().visit(this,arg)).append(")");
            }
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
        StringBuilder e = new StringBuilder();
        return e.append("PixelOps.pack(").append(expandedPixelExpr.getRedExpr().visit(this,arg)+ ", " +
               expandedPixelExpr.getGrnExpr().visit(this,arg)+", "+expandedPixelExpr.bluExpr.visit(this,arg)+")");
    }


    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        System.out.println(ident.toString());
        String s = ident.getName();

        //this is set up like this because it could not find the variables of scope level 1.
        // might cause issues if something at level 0 gets redeclared in level 1, but not sure how that would happen
        if (ident.uniqueID > 1) {
            s += "_" + ident.uniqueID;
        }
        return s;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        System.out.println(identExpr.toString());
        String s = identExpr.getName();
        if (identExpr.uniqueID > 1) {
            s += "_" + identExpr.uniqueID;
        }
        return s;
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
        String x = type(nameDef.getType());
        if(x == "pixel") {
            x = "int";
        }
        if(x == "image"){
            x = "BufferedImage";
            image = true;
        }
        name.append(x).append(" ").append(nameDef.getIdent().visit(this,arg));
        if(nameDef.uniqueID > 1){
            name.append("_").append(nameDef.uniqueID);
        }

        //not implementing dimensions for assignment 5
        return name.toString();
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        String e = String.valueOf(numLitExpr.getValue());
        return e;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException { //NOT IMPLEMENTING FOR ASSIGNMENT 6
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
        returnType = type(program.getType());
        String x = type(program.getType());
        if(x == "pixel") {
            x = "int";
        }
        if(x == "image"){
            x = "BufferedImage";//test
        }
        code.append(indentMaker()).append("public static ").append(x).append(" apply(");
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
        e.append("(int) Math.floor(Math.random()*256)");
        return e.toString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        inReturn = true;
        if(returnStatement.getE().toString().contains("ConditionalExpr")){
            returnConditional = true;
        }
        if(returnType == "String"){
            return e.append("return ").append("String.valueOf(").append(returnStatement.getE().visit(this,arg)).append(");");
        }
        e.append("return ").append(returnStatement.getE().visit(this,arg)).append(";\n");
        inReturn = false;
        return e.toString();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        e.append("\"").append(stringLitExpr.getValue()).append("\"");
        return e;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException { //implemented
        StringBuilder e = new StringBuilder();
        if(unaryExpr.getOp() == IToken.Kind.BANG && unaryExpr.getE().getType() == Type.INT){
            return e.append("(").append(unaryExpr.getE().visit(this,arg)).append(" == 0 ? 1 : 0)");
        }
        String s;
        if(unaryExpr.getOp() == IToken.Kind.BANG){
            s = "!";
        }
        else{
            s = "-";
        }
        return e.append(s + unaryExpr.getE().visit(this,arg));
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException { //not implementing this for Assignment 5
        StringBuilder e = new StringBuilder();
        if(unaryExprPostfix.getPrimary().getType() == Type.PIXEL){
            if(unaryExprPostfix.getColor() == ColorChannel.red){
                return e.append("PixelOps.red(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
            }
            if(unaryExprPostfix.getColor() == ColorChannel.grn){
                return e.append("PixelOps.grn(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
            }
            if(unaryExprPostfix.getColor() == ColorChannel.blu){
                return e.append("PixelOps.blu(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
            }
        }

        if(unaryExprPostfix.getPrimary().getType() == Type.IMAGE){
            if(unaryExprPostfix.getPixel() == null){
                ColorChannel x = unaryExprPostfix.getColor();
                switch(x){
                    case red -> {
                        return e.append("ImageOps.extractRed(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
                    }
                    case grn -> {
                        return e.append("ImageOps.extractGrn(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
                    }
                    case blu -> {
                        return e.append("ImageOps.extractBlu(").append(unaryExprPostfix.getPrimary().visit(this,arg)).append(")");
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        StringBuilder e = new StringBuilder();
        e.append("while (").append(whileStatement.getGuard().visit(this, arg))
                .append(" != 0) {\n").append(whileStatement.getBlock().visit(this,arg))
                .append("\n}\n");
        return e;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        if(!write){
            write = true;
        }
        StringBuilder s = new StringBuilder();
        if(statementWrite.getE().toString().contains("pixel")){
            s.append("ConsoleIO.writePixel(").append(statementWrite.getE().visit(this,arg)).append(")").append(";\n");

        }
        s.append("ConsoleIO.write(").append(statementWrite.getE().visit(this,arg)).append(")").append(";\n");

        return s;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException { //done
        String e = String.valueOf(zExpr.getValue());
        return e;
    }
}