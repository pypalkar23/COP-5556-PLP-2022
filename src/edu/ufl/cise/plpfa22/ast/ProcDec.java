/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.interfaces.IToken;
import edu.ufl.cise.plpfa22.exceptions.PLPException;

public class ProcDec extends Declaration {

	public final IToken ident;
	public final Block block;
	public String parentClass;

	public ProcDec(IToken firstToken, IToken name, Block body) {
		super(firstToken);
		this.ident = name;
		this.block = body;
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLPException {
		return v.visitProcedure(this, arg);
	}

	@Override
	public String toString() {
		return "ProcDec [" + (ident != null ? "ident=" + ident + ", " : "") + (block != null ? "block=" + block : "") + "]";
	}

	public String getParentClass() {
		return parentClass;
	}


	public void setParentClass(String enclosingClass) {
		this.parentClass = enclosingClass;
	}

	public String getAnotherDesc() {
		return "ProcDec [" + (ident != null ? "ident=" + ident + ", " : "") + "]"+ " nest=" + nest + "\n  ";
	}


}
