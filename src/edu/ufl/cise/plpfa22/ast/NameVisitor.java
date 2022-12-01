package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.PLPException;

import java.util.List;

public class NameVisitor extends VoidVisitor {
    String superClassName;

    public NameVisitor(String superClassName) {
        super();
        this.superClassName = superClassName;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        program.block.visit(this, superClassName);
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        String parentName = (String)arg;
        String currClassName = parentName + "$" + String.valueOf(procDec.ident.getText());

        procDec.setSetFullClassName(currClassName);
        procDec.setParentClass(parentName);
        procDec.block.visit(this, currClassName);
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {

        List<ProcDec> procs = block.procedureDecs;
        for (ProcDec dec: procs) {
            dec.visit(this, arg);
        }
        return null;
    }

}