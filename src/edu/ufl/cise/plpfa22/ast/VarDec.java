/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.interfaces.IToken;
import edu.ufl.cise.plpfa22.exceptions.PLPException;

public class VarDec extends Declaration {

	public final IToken ident;

	public VarDec(IToken firstToken, IToken id) {
		super(firstToken);
		this.ident = id;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLPException {
		return v.visitVarDec(this, arg);
	}

//	@Override
//	public String toString() {
//		return "VarDec [" + (ident != null ? "ident=" + ident + ", " : "") + (type != null ? "type=" + type : "") + "]";
//	}



	@Override
	public String getAnotherDesc() {
		return toString();
	}

	@Override
	public String toString() {
		return "VarDec [ident=" + ident + ", nest=" + nest + "]";
	}

	public String getVarName() {
		return String.valueOf(ident.getText());
	}



}
