package edu.ufl.cise.plcsp23;

import java.io.Reader;
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

        boolean check = legal(tokens);
        if(!check){
            throw new SyntaxException("Invalid sequence of tokens");
        }
        return null;
    }

    private boolean legal(Vector<IToken> tokens){
        Stack<IToken> paren = new Stack<>();
        boolean opOrParen = true;
        for(int i = 0; i < tokens.size();i++){
            Kind kind = tokens.get(i).getKind();
            if(kind == Kind.LPAREN){
                if(!opOrParen){
                    return false;
                }
                paren.push(tokens.get(i));
                opOrParen = true;
            }
            else if(kind == Kind.RPAREN){
                if(paren.empty() || paren.peek().getKind() != Kind.LPAREN){
                    return false;
                }
                paren.pop();
                opOrParen = false;
            }
            else if(kind == Kind.OR ||kind == Kind.BITOR ||kind == Kind.AND ||kind == Kind.BITAND ||kind == Kind.LT ||
                    kind == Kind.GT || kind == Kind.EQ ||kind == Kind.LE ||kind == Kind.GE ||kind == Kind.EXP ||
                    kind == Kind.PLUS || kind == Kind.MINUS ||kind == Kind.TIMES ||kind == Kind.DIV ||
                    kind == Kind.MOD){
                if(opOrParen){
                    return false;
                }
                opOrParen = true;
            }
            else if(kind == Kind.IDENT||kind == Kind.NUM_LIT ||kind == Kind.STRING_LIT
                    || kind == Kind.RES_Z || kind == Kind.RES_rand){
                if(!opOrParen){
                    return false;
                }
                opOrParen = false;
            }
            else{
                return false;
            }
        }
        return paren.empty() && !opOrParen;
    }
}
