package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;
import edu.ufl.cise.plpfa22.SymbolTable;
import edu.ufl.cise.plpfa22.SymbolTable.SymbolTableRecord;
import java.util.List;

public class PLPScopeVisitor implements ASTVisitor {

    SymbolTable symbolTable;
    public PLPScopeVisitor(){
        symbolTable = new SymbolTable();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        Block block = (Block)program.block;
        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        List<VarDec> varDecs = block.varDecs;
        List<ConstDec> constDecs = block.constDecs;
        List<ProcDec> procDecs = block.procedureDecs;
        for(VarDec varDec:varDecs){
            varDec.visit(this,arg);
        }

        for(ConstDec constDec:constDecs){
            constDec.visit(this,arg);
        }

        for(ProcDec procDec:procDecs){
            procDec.visit(this,false);
        }

        for(ProcDec procDec:procDecs){
            procDec.visit(this,true);
        }
        Statement stmt = block.statement;
        stmt.visit(this,arg);
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident ident = statementAssign.ident;
        Expression exp = statementAssign.expression;
        ident.visit(this,arg);
        exp.visit(this,arg);
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        varDec.setNest(symbolTable.currScope);
        if(!symbolTable.insert(varDec.ident.getStringValue(),varDec)){
            throw new ScopeException(ScopeUtils.SYMBOL_ALREADY_DEFINED,varDec.ident.getSourceLocation().line(),varDec.ident.getSourceLocation().column());
        }
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        Ident ident = statementCall.ident;
        ident.visit(this,arg);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        Ident ident = statementInput.ident;
        ident.visit(this,arg);
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        Expression expression =  statementOutput.expression;
        expression.visit(this,arg);
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        List<Statement> statements =  statementBlock.statements;
        for(Statement statement:statements){
            statement.visit(this,arg);
        }
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        Expression expression = statementIf.expression;
        Statement statement = statementIf.statement;
        expression.visit(this,arg);
        statement.visit(this,arg);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        Expression expression = statementWhile.expression;
        Statement statement = statementWhile.statement;
        expression.visit(this,arg);
        statement.visit(this,arg);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        Expression exp1 = expressionBinary.e0;
        Expression exp2 = expressionBinary.e1;
        exp1.visit(this,arg);
        exp2.visit(this,arg);
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        String key = expressionIdent.firstToken.getStringValue();
        SymbolTableRecord record = this.symbolTable.findRecord(key);
        if(record==null){
            throw new ScopeException(ScopeUtils.SYMBOL_NOT_DEFINED,expressionIdent.getSourceLocation().line(),expressionIdent.getSourceLocation().column());
        }
        expressionIdent.setDec(record.dec);
        expressionIdent.setNest(this.symbolTable.currScope);
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        return expressionNumLit.firstToken.getIntValue();
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        return expressionStringLit.firstToken.getStringValue();
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        return expressionBooleanLit.firstToken.getStringValue().equals("TRUE");
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        Boolean isParseBlock = (Boolean) arg;
        if(isParseBlock){
            symbolTable.enterScope();
            Block block = procDec.block;
            block.visit(this,arg);
            symbolTable.leaveScope();
        }
        else{
            if(!symbolTable.insert(procDec.ident.getStringValue(),procDec)){
                throw new ScopeException(ScopeUtils.SYMBOL_ALREADY_DEFINED,procDec.ident.getSourceLocation().line(),procDec.ident.getSourceLocation().column());
            }
        }

        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        constDec.setNest(symbolTable.currScope);
        if(!symbolTable.insert(constDec.ident.getStringValue(),constDec)){
            throw new ScopeException(ScopeUtils.SYMBOL_ALREADY_DEFINED,constDec.ident.getSourceLocation().line(),constDec.ident.getSourceLocation().column());
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        SymbolTableRecord record = symbolTable.findRecord(ident.firstToken.getStringValue());
        if(record==null){
            throw new ScopeException(ScopeUtils.SYMBOL_NOT_DEFINED,ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }
        ident.setNest(this.symbolTable.currScope);
        ident.setDec(record.dec);
        return null;
    }
}
