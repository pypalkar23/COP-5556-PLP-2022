/**  This code is provided for solely for use of students in the course COP5556
 Programming Language Principles at the
 * University of Florida during the Fall Semester 2022 as part of the course
 project.  No other use is authorized.
 */
package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.implementations.PLPLexer;
import edu.ufl.cise.plpfa22.implementations.PLPParser;
import edu.ufl.cise.plpfa22.implementations.PLPScopeVisitor;
import edu.ufl.cise.plpfa22.implementations.PLPTypeVisitor;
import edu.ufl.cise.plpfa22.interfaces.ILexer;
import edu.ufl.cise.plpfa22.interfaces.IParser;

public class CompilerComponentFactory {
    public static ILexer getLexer(String input) {
        return new PLPLexer(input);
    }

    public static IParser getParser(ILexer lexer){
        return new PLPParser(lexer);
    }

    public static ASTVisitor getScopeVisitor(){
        return new PLPScopeVisitor();
    }

    public static ASTVisitor getTypeInferenceVisitor(){
        return new PLPTypeVisitor();
    }

    public static ASTVisitor getCodeGenVisitor(String className, String packageName, String sourceFileName){
        return new CodeGenVisitor(className,packageName,sourceFileName);
    }
}