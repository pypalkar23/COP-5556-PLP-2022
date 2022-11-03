package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.IToken.Kind;

import java.util.ArrayList;
import java.util.List;


public class PLPTypeVisitor implements ASTVisitor {
    private SymbolTable symbolTable;
    private boolean isFirstPass;
    private Integer prevCounter;
    private Integer currCounter;
   private List<TypeCheckErrorRecord> errors;
    public PLPTypeVisitor(){
        this.symbolTable = new SymbolTable();
        this.currCounter = 0;
        this.isFirstPass = true;
        this.errors = new ArrayList<>();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        Block block= program.block;

        while(true){
            symbolTable.enterScope();
            block.visit(this,arg);
            symbolTable.leaveScope();

            symbolTable.resetScopeCounters();
            this.isFirstPass = false;
            if(currCounter == 0)
                    break;

            if(prevCounter!=null){
                if(prevCounter == currCounter){
                    TypeCheckErrorRecord error = errors.get(0);
                    throw new TypeCheckException(error.getMsg(),error.getSourceLocation().line(),error.getSourceLocation().column());
                }
            }

            prevCounter = currCounter;
            currCounter = 0;
            this.errors = new ArrayList<>();
        }
        /*symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();
        symbolTable.resetScopeCounters();

        this.isFinalPass = true;
        symbolTable.enterScope();
        block.visit(this,arg);
        symbolTable.leaveScope();*/

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
        if(isFirstPass)
            symbolTable.insert(varDec.ident.getStringValue(),varDec);
        return null;
    }

    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        Type type =  TypeCheckUtils.getConstType(constDec);
        constDec.setType(type);

        if(isFirstPass){
            symbolTable.insert(constDec.ident.getStringValue(),constDec);
        }
        return type;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        IToken ident = procDec.ident;

        procDec.setType(Type.PROCEDURE);
        if(isFirstPass){
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
            throw new TypeCheckException(TypeCheckUtils.ERROR_REASSIGNMENT_NOT_ALLOWED,ident.getSourceLocation().line(),ident.getSourceLocation().column());

        if(!(ident.getDec() instanceof VarDec))
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"VARIABLE"),ident.getSourceLocation().line(),ident.getSourceLocation().column());

        if(idType!=null && expType!=null){
            if(idType!=expType)
                throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,idType.toString()),statementAssign.getSourceLocation().line(),statementAssign.getSourceLocation().column());
        }

        if(idType==null && expType!=null){
            idType = expType;
            ident.visit(this,expType);
        }

        else if(idType!=null && expType==null){
            expType = idType;
            exp.visit(this,idType);
        }

        if(/*isFinalPass && */idType==null && expType==null){
            currCounter++;
            errors.add(new TypeCheckErrorRecord(String.format(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,TypeCheckUtils.PROCEDURE),ident.getSourceLocation()));
            //throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,TypeCheckUtils.PROCEDURE),ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }

        return ident.getDec().getType();
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        Ident ident = statementCall.ident;

        Type type = (Type)ident.visit(this,arg);
        if(type == null /*&& this.isFinalPass*/){
            currCounter++;
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,ident.getSourceLocation()));
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,ident.getSourceLocation().line(),ident.getSourceLocation().column());
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

        if(/*this.isFinalPass &&*/ type==null){
            currCounter++;
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementInput.getSourceLocation().line(),statementInput.getSourceLocation().column());
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementInput.getSourceLocation()));
        }
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        Expression exp = statementOutput.expression;
        Type type = (Type)exp.visit(this,arg);

        if(type!=null && type==Type.PROCEDURE){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"BOOLEAN,NUMBER,STRING"),statementOutput.getSourceLocation().line(),statementOutput.getSourceLocation().column());
        }
        if(/*this.isFinalPass &&*/ type==null){
            currCounter++;
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementOutput.getSourceLocation().line(),statementOutput.getSourceLocation().column());
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,statementOutput.getSourceLocation()));
        }

        return null;
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
        if(/*isFinalPass &&*/ expType == null){
            currCounter++;
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation().line(),exp.getSourceLocation().column());
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation()));
        }
        if(exp!=null && expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }

        /*if(statementType==null){
            currCounter++;
            //throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
            errors.add(new TypeCheckErrorRecord(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation()));
        }*/
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        Expression exp = statementWhile.expression;
        Statement statement = statementWhile.statement;
        Type expType =  (Type)exp.visit(this,arg);
        Type statementType = (Type)statement.visit(this,arg);

        /*if(isFinalPass &&  expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }*/
        if(/*isFinalPass &&*/ expType == null){
            currCounter++;
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation().line(),exp.getSourceLocation().column());
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,exp.getSourceLocation()));
        }
        if(exp!=null && expType!=Type.BOOLEAN){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }

        /*if(statementType==null){
            currCounter++;
            errors.add(new TypeCheckErrorRecord(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation()));
            //throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),exp.getSourceLocation().line(),exp.getSourceLocation().column());
        }*/

        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        Type expectedType = (Type)arg;
        Expression exp0 = expressionBinary.e0;
        IToken op = expressionBinary.op;
        Expression exp1 = expressionBinary.e1;
        Object newarg = arg;

        if(expectedType==Type.BOOLEAN && TypeCheckUtils.BOOLEAN_TOKEN_SET.contains(op.getKind())){
            newarg = null;
        }

        Type type0 = (Type)exp0.visit(this,newarg);
        Type type1 = (Type)exp1.visit(this,newarg);

        if(type0==Type.PROCEDURE || type1 == Type.PROCEDURE){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,"STRING,BOOLEAN,NUMBER"),expressionBinary.getSourceLocation().line(),expressionBinary.getSourceLocation().column());
        }

        if(type0!=null && type1==null){
            type1= (Type)exp1.visit(this,type0);
        }

        else if(type0==null && type1!=null){
            type0 =(Type)exp0.visit(this,type1);
        }

        if(type0!=null && type1!=null && (type0!=type1 || !TypeCheckUtils.isCompatible(type0,op))){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,type1.toString()),expressionBinary.getSourceLocation().line(),expressionBinary.getSourceLocation().column());
        }

        if(/*isFinalPass &&*/ (type0==null || type1==null)){
            currCounter++;
            errors.add(new TypeCheckErrorRecord(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,expressionBinary.getSourceLocation()));
            //throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,expressionBinary.getSourceLocation().line(),expressionBinary.getSourceLocation().column());
        }


        if(TypeCheckUtils.BOOLEAN_TOKEN_SET.contains(op.getKind())){
            expressionBinary.setType(Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else{
            expressionBinary.setType(type0);
            return type0;
        }

    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {

        Type expectedType = (Type) arg;
        Declaration dec = this.symbolTable.findDeclaration(expressionIdent.firstToken.getStringValue());

        if(dec.getType()!=null && expectedType!=null && dec.getType()!=expectedType){
            throw new TypeCheckException(TypeCheckUtils.ERROR_INCOMPLETE_INFORMATION,expressionIdent.getSourceLocation().line(),expressionIdent.getSourceLocation().column());
        }
        if(dec.getType()==null){
            dec.setType(expectedType);
        }

        expressionIdent.setType(dec.getType());
        return expressionIdent.getType();
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

        Declaration dec = this.symbolTable.findDeclaration(ident.getFirstToken().getStringValue());

        Type res = null;
        if(dec!=null){
            if(dec.getType()==null){
                dec.setType(expectedType);
                res = expectedType;
            }else{
                res = dec.getType();
            }
        }


        else if(dec!=null && expectedType!=null && dec.getType()!=null && dec.getType() != expectedType){
            throw new TypeCheckException(String.format(TypeCheckUtils.ERROR_TYPE_MISMATCH,TypeCheckUtils.BOOLEAN),ident.getSourceLocation().line(),ident.getSourceLocation().column());
        }
        return res;
    }





}
