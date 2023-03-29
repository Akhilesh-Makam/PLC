package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.TypeCheckException;
import edu.ufl.cise.plcsp23.PLCException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ASTVisitorX implements ASTVisitor {

    public static class SymbolTable {
        //implemented scoping w/ a stack of HashMaps
        Stack<HashMap<String, NameDef>> scopeStack = new Stack<>();

        public boolean insert(String name, NameDef nameDef){
            return scopeStack.peek().putIfAbsent(name, nameDef) == null;
        }

        public NameDef lookup(String name){
            for (int i = scopeStack.size() - 1; i >= 0; i--) {
                HashMap<String, NameDef> scope = scopeStack.get(i);
                if (scope.containsKey(name)) {
                    return scope.get(name);
                }
            }
            return null;
        }

        public void enterScope() {
            scopeStack.push(new HashMap<>());
        }

        public void leaveScope() {
            scopeStack.pop();
        }
    }

    SymbolTable symbolTable = new SymbolTable();
    Type programType;

    private void check(boolean condition, AST node, String message) throws PLCException{
        if(!condition){
            throw new TypeCheckException(message + node.getColumn() + node.getLine());
        }
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        /*
        Assignment Statements don't have types themselves. They just make sure the LValue and Expr
        are properly typed (visited) and that their types are compatible.
         */

        Type lvType = (Type) statementAssign.getLv().visit(this, arg);
        Type exprType = (Type) statementAssign.getE().visit(this, arg);

        switch(lvType) {
            case IMAGE -> {
                check((exprType == Type.IMAGE) || (exprType == Type.PIXEL) || (exprType == Type.STRING), statementAssign,
                        "LValue with type IMAGE needs Expr with type IMAGE, PIXEL, or STRING");
            }
            case PIXEL -> {
                check((exprType == Type.PIXEL) || (exprType == Type.INT), statementAssign,
                        "LValue with type PIXEL needs Expr with type PIXEL or INT");
            }
            case INT -> {
                check((exprType == Type.PIXEL) || (exprType == Type.INT), statementAssign,
                        "LValue with type INT needs Expr with type PIXEL or INT");
            }
            case STRING -> {
                check((exprType == Type.STRING) || (exprType == Type.INT) || (exprType == Type.PIXEL) || (exprType == Type.IMAGE), statementAssign,
                        "LValue with type STRING needs Expr with type STRING, INT, PIXEL, or IMAGE");
            }
            default -> {
                throw new TypeCheckException("LValue must be of type IMAGE, PIXEL, INT, or STRING.");
            }
        }

        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Token.Kind op = binaryExpr.getOp();
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;

        switch(op) {
            case BITAND, BITOR ->{
                check((leftType == Type.PIXEL) && (rightType == Type.PIXEL),binaryExpr, "incompatible types with given operator");
                resultType = Type.PIXEL;
            }
            case AND, OR, LT, GT, LE, GE ->{
                check((leftType == Type.INT) && (rightType == Type.INT),binaryExpr, "incompatible types with given operator");
                resultType = Type.INT;
            }
            case EQ ->{
                check((leftType == Type.INT && rightType == Type.INT) || (leftType == Type.PIXEL && rightType == Type.PIXEL)
                                || (leftType == Type.IMAGE && rightType == Type.IMAGE) || (leftType == Type.STRING && rightType == Type.STRING),
                        binaryExpr, "incompatible types with given operator");
                resultType = Type.INT;
            }
            case EXP->{
                check((leftType == Type.INT && rightType == Type.INT) || (leftType == Type.PIXEL && rightType == Type.INT)
                                || (leftType == Type.INT && rightType == Type.PIXEL), binaryExpr,
                        "incompatible types with given operator");
                if(leftType == Type.PIXEL || rightType == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else{
                    resultType = Type.INT;
                }
            }
            case PLUS -> {
                check((leftType == Type.INT && rightType == Type.INT) || (leftType == Type.PIXEL && rightType == Type.PIXEL)
                                || (leftType == Type.IMAGE && rightType == Type.IMAGE) || (leftType == Type.STRING && rightType == Type.STRING),
                        binaryExpr, "incompatible types with given operator");
                resultType = leftType;
            }
            case MINUS -> {
                check((leftType == Type.INT && rightType == Type.INT) || (leftType == Type.PIXEL && rightType == Type.PIXEL)
                                || (leftType == Type.IMAGE && rightType == Type.IMAGE),
                        binaryExpr, "incompatible types with given operator");
                resultType = leftType;
            }
            case TIMES, DIV, MOD -> {
                check((leftType == Type.INT && rightType == Type.INT) || (leftType == Type.PIXEL && rightType == Type.PIXEL)
                                || (leftType == Type.IMAGE && rightType == Type.IMAGE) || (leftType == Type.PIXEL && rightType == Type.INT)
                                || (leftType == Type.INT && rightType == Type.PIXEL) || (leftType == Type.IMAGE && rightType == Type.INT) ||
                                (leftType == Type.INT && rightType == Type.IMAGE)
                        ,
                        binaryExpr, "incompatible types with given operator");
                if(leftType == Type.PIXEL || rightType == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else if(leftType == Type.IMAGE || rightType == Type.IMAGE){
                    resultType = Type.IMAGE;
                }
                else{
                    resultType = leftType;
                }
            }
            default -> {
                throw new TypeCheckException("Incompatible operator in BinaryExpr");
            }
        }

        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> decList = block.getDecList();
        List<Statement> stateList = block.getStatementList();
        for(Declaration dec : decList){
            Type type = (Type) dec.visit(this, arg);
        }
        for(Statement state : stateList){
            Type type = (Type) state.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type expr0 = (Type) conditionalExpr.getGuard().visit(this, arg);
        Type expr1 = (Type) conditionalExpr.getTrueCase().visit(this, arg);
        Type expr2 = (Type) conditionalExpr.getFalseCase().visit(this, arg);

        check(expr0 == Type.INT, conditionalExpr, "Guard condition must be type INT");
        check(expr1 == expr2, conditionalExpr, "true and false expr types must match");

        conditionalExpr.setType(expr1);
        return expr1;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        //check Expr initializer is properly typed
        Type initializer = (Type) declaration.getInitializer().visit(this, arg);
        //NameDef is properly typed
        Type nameDef = (Type) declaration.getNameDef().visit(this, arg);
        //this visit ordering should check that the initializer doesn't refer to the name being defined(?)

        //If NameDef has type IMAGE, then both initializerExpr, nameDef.dimension cannot be null
        if (nameDef == Type.IMAGE) {
            Expr initializerExpr = declaration.getInitializer();
            Dimension dimension = declaration.getNameDef().getDimension();
            check((initializerExpr != null) || (dimension != null), declaration,
                    "Declaration of NameDef with type IMAGE needs initializer, NameDef.dimension, or both");
        }


        //check assign compatibility between initializer and nameDef (same as AssignmentStatement LValue to Expr rules)
        switch(nameDef) {
            case IMAGE -> {
                check((initializer == Type.IMAGE) || (initializer == Type.PIXEL) || (initializer == Type.STRING), declaration,
                        "NameDef with type IMAGE needs Expr with type IMAGE, PIXEL, or STRING");
            }
            case PIXEL -> {
                check((initializer == Type.PIXEL) || (initializer == Type.INT), declaration,
                        "NameDef with type PIXEL needs Expr with type PIXEL or INT");
            }
            case INT -> {
                check((initializer == Type.PIXEL) || (initializer == Type.INT), declaration,
                        "NameDef with type INT needs Expr with type PIXEL or INT");
            }
            case STRING -> {
                check((initializer == Type.STRING) || (initializer == Type.INT) || (initializer == Type.PIXEL) || (initializer == Type.IMAGE), declaration,
                        "NameDef with type STRING needs Expr with type STRING, INT, PIXEL, or IMAGE");
            }
            default -> {
                throw new TypeCheckException("NameDef must be of type IMAGE, PIXEL, INT, or STRING.");
            }
        }

        //declaration itself doesn't get assigned type here => no need to return one
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Type expr0 = (Type) dimension.getWidth().visit(this, arg);
        Type expr1 = (Type) dimension.getHeight().visit(this, arg);

        check((expr0 == Type.INT) && (expr1 ==  Type.INT), dimension,
                "Width and Height must both be type INT");

        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Type expr0 = (Type) expandedPixelExpr.getRedExpr().visit(this, arg);
        Type expr1 = (Type) expandedPixelExpr.getGrnExpr().visit(this, arg);
        Type expr2 = (Type) expandedPixelExpr.getBluExpr().visit(this, arg);

        check((expr0 == Type.INT) && (expr1 ==  Type.INT) && (expr2 ==  Type.INT), expandedPixelExpr,
                "Red, Green, and Blue must all be type INT");

        expandedPixelExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        //"Set depending on type assigned when declared"
        String name = ident.getName();
        NameDef nameDef = symbolTable.lookup(name);

        check(nameDef != null, ident, "Ident not found in symbol table");

        Type type = nameDef.getType();
        return type;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        //Based on Lecture slide code to make sure identExpr.name has been defined and in scope
        String name = identExpr.getName();
        NameDef nameDef = symbolTable.lookup(name);

        check(nameDef != null, identExpr, "IdentExpr not found in symbol table");

        Type type = nameDef.getType();
        identExpr.setType(type);
        return type;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        Type ident = (Type) lValue.getIdent().visit(this, arg);

        //use table to set and return LVal type
        PixelSelector pixelSelector = lValue.getPixelSelector();
        ColorChannel colorChannel = lValue.getColor();
        Type result = null;

        switch(ident) {
            case IMAGE -> {
                if (pixelSelector == null && colorChannel == null) {
                    result = Type.IMAGE;
                }
                else if(pixelSelector != null && colorChannel == null) {
                    result = Type.PIXEL;
                }
                else if(pixelSelector == null && colorChannel != null) {
                    result = Type.IMAGE;
                }
                else if(pixelSelector != null && colorChannel != null) {
                    result = Type.INT;
                }
                else {
                    throw new TypeCheckException("for ident of type IMAGE, invalid pixelSelector or colorChannel");
                }
            }
            case PIXEL -> {
                if (pixelSelector == null && colorChannel == null) {
                    result = Type.PIXEL;
                }
                else if(pixelSelector == null && colorChannel != null) {
                    result = Type.INT;
                }
                else {
                    throw new TypeCheckException("for ident of type PIXEL, invalid pixelSelector or colorChannel");
                }
            }
            case STRING -> {
                if (pixelSelector == null && colorChannel == null) {
                    result = Type.STRING;
                }
                else {
                    throw new TypeCheckException("for ident of type STRING, pixelSelector and colorChannel should be null");
                }
            }
            case INT -> {
                if (pixelSelector == null && colorChannel == null) {
                    result = Type.INT;
                }
                else {
                    throw new TypeCheckException("for ident of type INT, pixelSelector and colorChannel should be null");
                }
            }
            default -> {
                throw new TypeCheckException("Invalid ident type");
            }
        }

        return result;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        if(nameDef.getDimension() != null){
            check(nameDef.getType() == Type.IMAGE, nameDef,
                    "NameDef must be type IMAGE if dimension is defined");
            Type r = (Type) nameDef.getDimension().visit(this, arg);
        }

        check(nameDef.getType() != Type.VOID, nameDef, "NameDef cannot be void");

        HashMap<String, NameDef> currentScope = symbolTable.scopeStack.peek();

        if(!currentScope.containsKey(nameDef.getIdent().getName())){
            symbolTable.insert(nameDef.getIdent().getName(), nameDef);
            return null;
        }
        else{
            throw new TypeCheckException("Ident already declared in this scope");
        }
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        Type pixelSelector = (Type) pixelFuncExpr.getSelector().visit(this, arg);

        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        Type expr0 = (Type) pixelSelector.getX().visit(this, arg);
        Type expr1 = (Type) pixelSelector.getY().visit(this, arg);

        check((expr0 == Type.INT) && (expr1 ==  Type.INT), pixelSelector,
                "X and Y must both be type INT");

        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        symbolTable.enterScope();

        programType = program.getType();

        List<NameDef> paramList = program.getParamList();
        for(NameDef def : paramList){
            def.visit(this, arg);
        }

        Type block = (Type) program.getBlock().visit(this, arg);

        symbolTable.leaveScope();

        return programType;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException {
        Type expr = (Type) returnStatement.getE().visit(this, arg);

        //check expr type against program type for assignment compatibility
        switch(programType) {
            case IMAGE -> {
                check(expr == Type.IMAGE || expr == Type.PIXEL || expr == Type.STRING, returnStatement,
                        "program of type IMAGE needs return expression of type IMAGE, PIXEL, or STRING");
            }
            case PIXEL -> {
                check(expr == Type.PIXEL || expr == Type.INT, returnStatement,
                        "program of type PIXEL needs return expression of type PIXEL or INT");
            }
            case INT -> {
                check(expr == Type.PIXEL || expr == Type.INT, returnStatement,
                        "program of type INT needs return expression of type PIXEL or INT");
            }
            case STRING -> {
                check(expr == Type.STRING || expr == Type.PIXEL || expr == Type.INT || expr == Type.IMAGE, returnStatement,
                        "program of type STRING needs return expression of type STRING, INT, PIXEL or IMAGE");
            }
            default -> {
                throw new TypeCheckException("Invalid program type");
            }
        }


        return null;
    }

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Token.Kind op = unaryExpr.getOp();
        Type expr = (Type) unaryExpr.getE().visit(this, arg);
        Type result = null;

        switch(op){
            case BANG -> {
                check(expr == Type.INT || expr == Type.PIXEL, unaryExpr,
                        "for op of type BANG, expr must be type INT or PIXEL");
                result = expr;
            }
            case MINUS, RES_cos, RES_sin, RES_atan -> {
                check(expr == Type.INT, unaryExpr,
                        "for op of given type, expr must be type INT");
                result = Type.INT;
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
        //check that primaryExpr is properly typed
        Type primary = (Type) unaryExprPostfix.getPrimary().visit(this, arg);

        PixelSelector pixelSelector = unaryExprPostfix.getPixel();
        ColorChannel colorChannel = unaryExprPostfix.getColor();
        Type result = null;

        if (pixelSelector == null && colorChannel == null) {
            throw new TypeCheckException("At least one of PixelSelector or ChannelSelector needed");
        }
        else if (pixelSelector == null && colorChannel != null) {
            check(primary == Type.PIXEL || primary == Type.IMAGE, unaryExprPostfix,
                    "When no pixelSelector, primaryExpr must be type PIXEL or IMAGE");
            if (primary == Type.PIXEL) {
                result = Type.INT;
            }
            else {
                result = Type.IMAGE;
            }
        }
        else if (pixelSelector != null && colorChannel == null) {
            check(primary == Type.IMAGE, unaryExprPostfix,
                    "When no colorChannel, primaryExpr must be type IMAGE");
            result = Type.PIXEL;
        }
        else {
            check(primary == Type.IMAGE, unaryExprPostfix,
                    "When both pixelSelector and colorChannel given, primaryExpr must be type IMAGE");
            result = Type.INT;
        }

        unaryExprPostfix.setType(result);
        return result;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        Type expr = (Type) whileStatement.getGuard().visit(this, arg);
        check(expr == Type.INT, whileStatement, "While GuardExpr must be type INT");

        symbolTable.enterScope();
        Type block = (Type) whileStatement.getBlock().visit(this, arg);
        symbolTable.leaveScope();

        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        Type expr = (Type) statementWrite.getE().visit(this, arg);
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }
}
