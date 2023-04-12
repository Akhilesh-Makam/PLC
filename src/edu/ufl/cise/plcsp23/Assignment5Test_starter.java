package edu.ufl.cise.plcsp23;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.Program;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.DynamicClassLoader;
import edu.ufl.cise.plcsp23.javaCompilerClassLoader.DynamicCompiler;

class Assignment5Test_starter {

    Object genCodeAndRun(String input, String mypackage, Object[] params) throws Exception{
        show("**** Input ****");
        show(input);
        AST ast = CompilerComponentFactory.makeParser(input).parse();
        ast.visit(CompilerComponentFactory.makeTypeChecker(), null);
        String name = ((Program)ast).getIdent().getName();
        String packageName = "";
        String code = (String) ast.visit(CompilerComponentFactory.makeCodeGenerator(packageName),null);
        show("**** Generated Code ****");
        show(code);
        show("**** Output ****");
        byte[] byteCode = DynamicCompiler.compile(name, code);
        Object result = DynamicClassLoader.loadClassAndRunMethod(byteCode, name, "apply", params);
        show("**** Returned Result ****");
        show(result);
        return result;
    }



    // makes it easy to turn output on and off (and less typing thanSystem.out.println)
    static final boolean VERBOSE = true;
    void show(Object obj) {
        if (VERBOSE) {
            System.out.println(obj);
        }
    }


    @Test
        //Demonstrates dynamic compilation and execution using a small valid Java class.
    void testDynamicCompileAndRun() throws Exception {
        String code = """
				public class Class1 {
				   public static int f(int x){
				     return x+1;
				   }
				 }
				""";
        String className = "Class1";
        byte[] byteCode = DynamicCompiler.compile(className, code);
        int paramVal = 3;
        Object[] params = {paramVal};
        Object result = DynamicClassLoader.loadClassAndRunMethod(byteCode, className, "f", params);
        System.out.println(result);
        assertEquals(paramVal+1, ((Integer)result).intValue());
    }

    @Test
    void cg0() throws Exception {
        String input = "void f(){}";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params );
        assertEquals(null, result);
    }

    @Test
    void cg1() throws Exception {
        String input = "int f(){:3.}";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params );
        assertEquals(3,((Integer)result).intValue());
    }

    @Test
    void cg2() throws Exception {
        String input = """
				int f(int aa, int bb){
				   :aa+bb.
				}""";
        int aa = 5;
        int bb = 7;
        Object[] params = {aa,bb};
        Object result = genCodeAndRun(input, "", params);
        assertEquals(aa+bb,((Integer)result).intValue());
    }

    @Test
    void cg3() throws Exception {
        String input = """
				string f(string aa, string bb){
				   :aa+bb.
				}""";
        String aa = "aa";
        String bb = "bb";
        Object[] params = {aa,bb};
        Object result = genCodeAndRun(input, "", params);
        assertEquals(aa+bb, result);
    }

    @Test
    void cg5a() throws Exception{
        String input= """
				string c(int val){
				   string ss = if val > 0 ? "greater" ? "not greater".
				   :ss.
				   } 
				""";
        int v = -2;
        Object[] params = {v};
        Object result = genCodeAndRun(input,"",params);
        assertEquals(v>0?"greater":"not greater", result);
    }

    @Test
    void cg5b() throws Exception{
        String input= """
				string c(int val){
				   string ss = if val > 0 ? "greater" ? "not greater".
				   :ss.
				   } 
				""";
        int v = 2;
        Object[] params = {v};
        Object result = genCodeAndRun(input,"",params);
        assertEquals(v>0?"greater":"not greater", result);
    }

    @Test
    void cg5c() throws Exception{
        String input= """
				string c(int val){
				   string ss = if val > 0 ? "greater" ? "not greater".
				   :ss.
				   } 
				""";
        int v = 0;
        Object[] params = {v};
        Object result = genCodeAndRun(input,"",params);
        assertEquals(v>0?"greater":"not greater", result);
    }

    @Test
    void cg6() throws Exception{
        String input= """
				string c(int val){
				   string ss = if val > 0 ? "greater" ? "not greater".
				   :ss.
				   } 
				""";
        int v = 2;
        Object[] params = {v};
        Object result = genCodeAndRun(input,"",params);
        show(result);
        assertEquals("greater", result);
    }

    @Test
    void cg7() throws Exception{
        String input = """
				void d(int val){
				int vv = val/2.
				write vv.
				}
				""";
        int v = 4;
        Object[] params = {v};
        Object result = genCodeAndRun(input,"",params);
        //should write 2 to OUTPUT
        assertEquals(null,result);
    }

    @Test
    void cg8() throws Exception{
        String input = """
			int d(int val){
			int aaa = val.
			int bbb = aaa.
			while (bbb>0) {
			   write bbb.
			   bbb = bbb - 1.
			   }.
			   :bbb.	
			   }
				""";
        int v = 4;
        Object[] params = {v};
        int result = (Integer)genCodeAndRun(input,"",params);
        //should write
        // 4
        // 3
        // 2
        // 1
        // to OUTPUT
        assertEquals(0, result);
    }

    @Test
    void cg9a() throws Exception{
        String input = """
				int BBoolean(int xx){
				int yy = xx>=0.
				int z = yy+1.
				z = z-1.
				: if z ? z ? 0.
				}				
				""";
        int v = 1;
        Object[] params = {v};
        int result = (Integer)genCodeAndRun(input,"",params);
        assertEquals(1,result);
    }

    @Test
    void cg9b() throws Exception{
        String input = """
				int BBoolean(int xx){
				int yy = xx>=0.
				int z = yy+1.
				z = z-1.
				: if z ? z ? 0.
				}				
				""";
        int v = -1;
        Object[] params = {v};
        int result = (Integer)genCodeAndRun(input,"",params);
        assertEquals(0,result);
    }

    @Test
    void cg11() throws Exception{
        String input = """
 int testWhile(int val){
 int aa = val.
 int g = aa.
 write val.
 while (g > 0) {
 int aa = val/2.
 write "outer loop: aa=".
 write aa.
 g = (aa%2==0).
 val = val-1.
 while (val > 0){
 int aa = val/5.
 write "inner loop: aa=".
 write aa.
 val = 0.
 }.
 write "outer loop after inner loop: aa=".
 write aa.
 }.
 : aa.
 }
""";
        int v = 100;
        Object[] params = {v};
        int result = (int) genCodeAndRun(input,"",params);
        assertEquals(v, (Integer)result);

    }

    @Test
    void andWhatIsUp() throws Exception {
        String input = "int up(int up){ :up. }";
        Object[] params = { 1 };
        Object result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
    }

    @Test
    void andReturnString() throws Exception {
        String input = """
				string fun() { :"Hello, World!". }
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals("Hello, World!", (String) result);
    }

    @Test
    void andAddingStrings() throws Exception {
        String input = """
				string fun(string start) {
					string end = ", World!".
					:start + end. }
				""";
        Object[] params = { "Hello" };
        Object result = genCodeAndRun(input, "", params);
        assertEquals("Hello, World!", (String) result);
        params[0] = "Goodbye";
        result = genCodeAndRun(input, "", params);
        assertEquals("Goodbye, World!", (String) result);
    }

    @Test
    void andBasicMath() throws Exception {
        String input = """
				int fun(int num1, int num2) {
					int num3 = num1 + num2.
					: num3.
				}
				""";
        int num1 = 5;
        int num2 = 2;
        Object[] params = { num1, num2 };
        Object result = genCodeAndRun(input, "", params);
        assertEquals(num1 + num2, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 - num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(num1 - num2, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 * num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(num1 * num2, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 / num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(num1 / num2, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 % num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(num1 % num2, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 ** num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals((int) Math.pow(num1, num2), result);
    }

    @Test
    void andLogicalOps() throws Exception {
        String input = """
				int fun(int num1, int num2) {
					int num3 = num1 || num2.
					: num3.
				}
				""";
        Object[] params = { 1, 0 };
        Object result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
        input = """
				int fun(int num1, int num2) {
					int num3 = num1 && num2.
					: num3.
				}
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(0, result);
    }

    @Test
    void andComparisonOps() throws Exception {
        String input = """
				int fun() {	: 1 > 0. }
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
        input = """
				int fun() {	: 1 < 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(0, result);
        input = """
				int fun() {	: 1 <= 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(0, result);
        input = """
				int fun() {	: 1 >= 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
        input = """
				int fun() {	: 0 <= 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
        input = """
				int fun() {	: 0 >= 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(1, result);
        input = """
				int fun() {	: 0 < 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(0, result);
        input = """
				int fun() {	: 0 > 0. }
				""";
        result = genCodeAndRun(input, "", params);
        assertEquals(0, result);
    }

    @Test
    void andIfExpr() throws Exception {
        String input = """
				string fun(int num) { : if num ? "true" ? "false". }
				""";
        Object[] params = { 1 };
        Object result = genCodeAndRun(input, "", params);
        assertEquals("true", (String) result);
        params[0] = 0;
        result = genCodeAndRun(input, "", params);
        assertEquals("false", (String) result);
        params[0] = -1;
        result = genCodeAndRun(input, "", params);
        assertEquals("true", (String) result);
        params[0] = 123456;
        result = genCodeAndRun(input, "", params);
        assertEquals("true", (String) result);
    }

    @Test
    void andMultipleDeclarations() throws Exception {
        String input = """
				void fun(int fun) {
					fun = 0.
					while (fun) {
						int fun = 0.
						while (fun) {
							int fun = 0.
							while (fun) {
								string fun = "".
							}.
						}.
						fun = 0.
						while (fun) {
							string fun = "".
						}.
					}.

				}
				""";
        Object[] params = { 0 };
        genCodeAndRun(input, "", params);
    }

    @Test
    void andSeparateVariables() throws Exception {
        String input = """
				int fun() {
					int val = 5.
					int i = 0.
					while (i < 1) {
						int val = 3.
						i = i + 1.
					}.
					: val.
				}
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals(5, result);
    }

    @Test
    void andAddingInALoop() throws Exception {
        String input = """
				int fun() {
					int val = 0.
					int i = 0.
					while (i <= 6) {
						val = val + i.
						i = i + 1.
					}.
					: val.
				}
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals(21, result);
    }

    @Test
    void andItsBinary() throws Exception {
        String input = """
				string fun(int num) {
					string result = "".
					while (num > 0) {
						result = (if num % 2 == 0 ? "0" ? "1") + result.
						num = num / 2.
					}.
					: result.
				}
				""";
        int num = 3563;
        Object[] params = { num };
        Object result = genCodeAndRun(input, "", params);
        assertEquals(Integer.toBinaryString(num), (String) result);
    }

    @Test
    void andReturnIntAsString() throws Exception {
        String input = """
				string fun() { :1. }
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals("1", (String) result);
    }

    @Test
    void andAssignIntToString() throws Exception {
        String input = """
				string fun() {
					string result.
					result = 1.
					: result.
				}
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals("1", (String) result);
    }

    @Test
    void andDeclareStringWithInt() throws Exception {
        String input = """
				string fun() {
					string result = 1.
					: result.
				}
				""";
        Object[] params = {};
        Object result = genCodeAndRun(input, "", params);
        assertEquals("1", (String) result);
    }
}