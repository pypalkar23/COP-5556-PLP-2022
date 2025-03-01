/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.interfaces.IToken;
import edu.ufl.cise.plpfa22.exceptions.PLPException;

public class Ident extends ASTNode {
	
	Declaration dec;
	int nest;
	public Ident(IToken firstToken) {
		super(firstToken);
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLPException {
		return v.visitIdent(this,arg);
	}


	public Declaration getDec() {
		return dec;
	}

	public String getStringIdentifier() {
		return String.valueOf(firstToken.getText());
	}
	@Override
	public String toString() {
		return "Ident [dec=" + dec + ", firstToken=" + firstToken + "]";
	}


	public void setDec(Declaration dec) {
		this.dec = dec;
	}

	public char[] getText() {
		return firstToken.getText();
	}

	public int getNest() {
		return nest;
	}

	public void setNest(int nest) {
		this.nest = nest;
	}


}
