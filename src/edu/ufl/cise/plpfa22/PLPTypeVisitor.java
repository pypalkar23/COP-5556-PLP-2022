package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;
import edu.ufl.cise.plpfa22.ast.Types.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PLPTypeVisitor implements ASTVisitor {
    private SymbolTable symbolTable;
    private boolean isFinalPass;

    private Set<String> procedureTracker;

    public PLPTypeVisitor(){
        this.isFinalPass = false;
        this.symbolTable = new SymbolTable();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        Block block= program.block;

        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();

        this.isFinalPass = true;
        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.enterScope();

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
        for(int i=0; i< procDecs.size();i++){
            procDecs.get(i).visit(this,arg);
        }
        Statement statement = block.statement;
        statement.visit(this,arg);
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        if(!isFinalPass)
            symbolTable.insert(varDec.ident.getStringValue(),varDec);
        return null;
    }

    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        Type type =  TypeCheckUtils.getConstType(constDec);
        constDec.setType(type);

        if(!isFinalPass){
            symbolTable.insert(constDec.ident.getStringValue(),constDec);
        }
        return type;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        IToken ident = procDec.ident;

        procDec.setType(Type.PROCEDURE);
        if(!isFinalPass){
            this.symbolTable.insert(ident.getStringValue(),procDec);
        }


        Block block = procDec.block;
        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();
        return procDec.getType();
    }
    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Ident ident = statementAssign.ident;
        Type idType = (Type)ident.visit(this,arg);
        Expression exp = statementAssign.expression;
        Type expType = (Type)exp.visit(this,arg);
        if(ident.getDec() instanceof ConstDec)
            return new TypeCheckException(TypeCheckUtils.ERROR_REASSIGNMENT_NOT_ALLOWED,ident.getSourceLocation().line(),ident.getSourceLocation().column());

        if(!(ident.getDec() instanceof VarDec))
            return new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"VARIABLE"),ident.getSourceLocation().line(),ident.getSourceLocation().column());

        //get expression type
        // get ident type
        //if ident is not variable throw error
        //if ident type is not decided assign expression type
        //if ident type is decided and expression type is mismatched throw error
        if(idType!=null && expType!=null){
            if(idType!=expType)
                throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,idType.toString()),statementAssign.getSourceLocation().line(),statementAssign.getSourceLocation().column());
        }

        if(idType==null && expType!=null){
            ident.getDec().setType(expType);
        }

        else if(idType!=null && expType==null){
            exp.visit(this,idType);
        }

        if(isFinalPass && idType==null && expType==null){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,TypeCheckUtils.PROCEDURE),ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }

        return idType;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        Ident ident = statementCall.ident;

        Type type = (Type)ident.visit(this,arg);
        if(type == null && this.isFinalPass){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }
        if(type!=null && type!=Type.PROCEDURE){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.PROCEDURE),ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }
        return type;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        Ident ident = statementInput.ident;
        Type type = (Type)ident.visit(this,arg);

        if(!(ident.getDec() instanceof VarDec)){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"BOOLEAN,NUMBER,STRING"),statementInput.getSourceLocation().line(),statementInput.getSourceLocation().column());
        }

        if(this.isFinalPass && type==null){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementInput.getSourceLocation().line(),statementInput.getSourceLocation().column());
        }
        return type;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        Expression exp = statementOutput.expression;
        Type type = (Type)exp.visit(this,arg);

        if(type!=null && type==Type.PROCEDURE){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"BOOLEAN,NUMBER,STRING"),statementOutput.getSourceLocation().line(),statementOutput.getSourceLocation().column());
        }
        if(this.isFinalPass && type==null){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementOutput.getSourceLocation().line(),statementOutput.getSourceLocation().column());
        }

        return type;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        List<Statement> statements = statementBlock.statements;
        for(Statement statement:statements){
            statement.visit(this,arg);
        }
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        Expression exp = statementIf.expression;
        Statement statement = statementIf.statement;
        Type expType =  (Type)exp.visit(this,arg);
        Type statementType = (Type)statement.visit(this,arg);
        if(isFinalPass && expType == null){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }
        if(exp!=null && expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }

        if(isFinalPass && statementType==null){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }
        return expType;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        Expression exp = statementWhile.expression;
        Statement statement = statementWhile.statement;
        Type expType =  (Type)exp.visit(this,arg);
        Type statementType = (Type)statement.visit(this,arg);

        if(isFinalPass && expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }
        if(isFinalPass && expType == null){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }
        if(exp!=null && expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }

        if(isFinalPass && statementType==null){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }

        return expType;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        Type type = expressionIdent.getDec().getType();
        if(this.isFinalPass && type==null){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,expressionIdent.getSourceLocation().line(),expressionIdent.getSourceLocation().column());
        }
        expressionIdent.setType(type);
        return type;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        Type expectedType = (Type)arg;
        if(expectedType!=null && expectedType!=Type.NUMBER)
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.STRING),expressionNumLit.getSourceLocation().line(),expressionNumLit.getSourceLocation().column());
        return Type.NUMBER;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        Type expectedType = (Type)arg;
        if(expectedType!=null && expectedType!=Type.STRING)
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.STRING),expressionStringLit.getSourceLocation().line(),expressionStringLit.getSourceLocation().column());
        return Type.STRING;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        Type expectedType = (Type)arg;
        if(expectedType!=null && expectedType!=Type.BOOLEAN)
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),expressionBooleanLit.getSourceLocation().line(),expressionBooleanLit.getSourceLocation().column());
        return Type.BOOLEAN;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {

        Type expectedType = (Type)arg;
        Type res = null;
        Declaration dec = this.symbolTable.findDeclaration(ident.getFirstToken().getStringValue());

        if(dec.getType()==null){
            dec.setType(expectedType);
        }

        else if(expectedType!=null && dec.getType()!=null && dec.getType() != expectedType){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }
        return dec.getType();
    }


}
