package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.SourceLocation;

public class TypeCheckErrorRecord {
    private String msg;
    private SourceLocation sourceLocation;

    public TypeCheckErrorRecord(String msg, SourceLocation sourceLocation) {
        this.msg = msg;
        this.sourceLocation = sourceLocation;
    }

    public String getMsg() {
        return msg;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }
}
