/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the course.
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.ASTVisitor;
import edu.ufl.cise.plcsp23.ast.ASTVisitorX;
import edu.ufl.cise.plcsp23.ast.CodeGen;

public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		//Add statement to return an instance of your scanner
		return new Scanner(input);
	}

	public static IParser makeAssignment2Parser(String input) {
		//Add statement to return an instance of your scanner and parser
		IScanner scanner = new Scanner(input);
		return new Parser(scanner);
	}

	public static IParser makeParser(String input) {
		//Add statement to return an instance of your scanner and parser
		IScanner scanner = new Scanner(input);
		return new Parser(scanner);
	}

	public static ASTVisitorX makeTypeChecker() {
		ASTVisitorX x = new ASTVisitorX();
		return x;
	}

	public static CodeGen makeCodeGenerator(String y) throws PLCException {
		CodeGen x = new CodeGen();
		return x;
	}
}