package edu.ufl.cise.plcsp23;/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public website either during or after the course.
 */

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.ASTVisitor;

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

	public static AST makeTypeChecker(String input) throws PLCException { //not sure how to make this
		IScanner scanner = new Scanner(input);
		IParser parser = new Parser(scanner);
		AST x = parser.parse();
		return x;
	}
}