/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.interfaces.IToken;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public abstract class Declaration extends ASTNode {

	Type type;
	int nest;
	String setFullClassName;

	public String getSetFullClassName() {
		return setFullClassName;
	}

	public void setSetFullClassName(String setFullClassName) {
		this.setFullClassName = setFullClassName;
	}

	public Declaration(IToken firstToken) {
		super(firstToken);
		nest = -1;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setNest(int nest) {
		this.nest = nest;
	}

	public int getNest() {
		return nest;
	}

	abstract public String getAnotherDesc();
}
