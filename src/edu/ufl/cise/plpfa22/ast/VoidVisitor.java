package edu.ufl.cise.plpfa22.ast;


import java.util.List;

import edu.ufl.cise.plpfa22.PLPException;

public abstract class VoidVisitor implements ASTVisitor {
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        Block block =program.block;
        block.visit(this, arg);
        return program;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        List<VarDec> varDecs = block.varDecs;
        List<ConstDec> constDecs = block.constDecs;
        List<ProcDec> procDecs = block.procedureDecs;

        for( ConstDec dec : constDecs) {
            dec.visit(this,arg);
        }
        for (VarDec dec: varDecs) {
            dec.visit(this, arg);
        }

        int pass = 0;
        for (ProcDec dec: procDecs) {
            dec.visit(this, pass);
        }
        pass++;
        for (ProcDec dec: procDecs) {
            dec.visit(this, pass);
        }
        Statement statement = block.statement;
        statement.visit(this, arg);
        return block;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        return varDec;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        Ident ident = statementCall.ident;
        ident.visit(this, arg);
        return statementCall;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        Ident ident = statementInput.ident;
        ident.visit(this, arg);
        return statementInput;
    }


    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident ident = statementAssign.ident;
        Expression expression = statementAssign.expression;
        ident.visit(this, arg);
        expression.visit(this, arg);
        return statementAssign;
    }


    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        Expression expression = statementOutput.expression;
        expression.visit(this, arg);
        return statementOutput;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        List<Statement> statements = statementBlock.statements;
        for (Statement s: statements) {
            s.visit(this, arg);
        }
        return statementBlock;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        Expression expression = statementIf.expression;
        Statement statement = statementIf.statement;
        expression.visit(this, arg);
        statement.visit(this, arg);
        return statementIf;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        Expression expression = statementWhile.expression;
        Statement statement = statementWhile.statement;
        expression.visit(this, arg);
        statement.visit(this, arg);
        return statementWhile;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        expressionBinary.e0.visit(this, arg);
        expressionBinary.e1.visit(this, arg);
        return expressionBinary;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        return expressionIdent;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        return expressionNumLit;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        return expressionBooleanLit;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        return expressionStringLit;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        Block block = procDec.block;
        block.visit(this, arg);
        return procDec;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        return constDec;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return statementEmpty;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        return ident;
    }

}
