package edu.ufl.cise.plcsp23;

import java.io.Reader;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

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

public class Parser implements IParser {
    private Scanner scanner;
    private String input;

    //have to make Parser class and ASTVisitor Class
    public Parser(String input, Scanner scanner){
        this.scanner = scanner;
        this.input = input;
    }

    @Override
    public AST parse() throws PLCException { //where the actual parsing occurs
        Vector<IToken> tokens = new Vector<IToken>();
        IToken current = scanner.next();
        tokens.add(current);
        if(current.getKind() == Kind.EOF){
            throw new SyntaxException("No input");
        }
        while(current.getKind() != Kind.EOF){
            current = scanner.next();
            tokens.add(current);
        }

        for(int i = 0; i < tokens.size();i++){
            System.out.println(tokens.get(i).getKind() + " " + tokens.get(i).getTokenString());
        }

        boolean check = legal(tokens);
        if(!check){
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


}
