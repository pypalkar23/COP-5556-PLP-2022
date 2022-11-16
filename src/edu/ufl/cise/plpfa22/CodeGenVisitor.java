package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.PLPException;
import edu.ufl.cise.plpfa22.ast.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Types.Type;

import java.util.List;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
    final String packageName;
    final String className;
    final String sourceFileName;
    final String fullyQualifiedClassName;
    final String classDesc;
    ClassWriter classWriter;

    public CodeGenVisitor(String className, String packageName, String
            sourceFileName) {
        super();
        this.packageName = packageName;
        this.className = className;
        this.sourceFileName = sourceFileName;
        this.fullyQualifiedClassName = packageName + "/" + className;
        this.classDesc = "L" + this.fullyQualifiedClassName + ';';
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        MethodVisitor methodVisitor = (MethodVisitor) arg;
        methodVisitor.visitCode();
        for (ConstDec constDec : block.constDecs) {
            constDec.visit(this, null);
        }
        for (VarDec varDec : block.varDecs) {
            varDec.visit(this, methodVisitor);
        }
        for (ProcDec procDec : block.procedureDecs) {
            procDec.visit(this, null);
        }
        //add instructions from statement to method
        block.statement.visit(this, arg);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        //create a classWriter and visit it
        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        //Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
        // instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
        // but you will be able to print it so you can see the instructions.After fixing,
        // restore ClassWriter.COMPUTE_FRAMES
        classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName,
                null, "java/lang/Object", null);
        //get a method visitor for the main method.
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC |
                ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        //visit the block, passing it the methodVisitor
        program.block.visit(this, methodVisitor);
        //finish up the class
        classWriter.visitEnd();
        //return the bytes making up the classfile
        return classWriter.toByteArray();
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object
            arg) throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg)
            throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg)
            throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object
            arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        statementOutput.expression.visit(this, arg);
        Type etype = statementOutput.expression.getType();
        String JVMType = (etype.equals(Type.NUMBER) ? "I" :
                (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
        String printlnSig = "(" + JVMType + ")V";
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                printlnSig, false);
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg)
            throws PLPException {
        List<Statement> statements = statementBlock.statements;
        for (Statement statement : statements) {
            statement.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws
            PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
            throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object
            arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Type argType = expressionBinary.e0.getType();
        Kind op = expressionBinary.op.getKind();
        expressionBinary.e0.visit(this, arg);
        expressionBinary.e1.visit(this, arg);
        Label conditionNotMet = new Label();
        switch (argType) {
            case NUMBER -> {
                switch (op) {
                    case PLUS -> mv.visitInsn(IADD);
                    case MINUS -> mv.visitInsn(ISUB);
                    case TIMES -> mv.visitInsn(IMUL);
                    case DIV -> mv.visitInsn(IDIV);
                    case MOD -> mv.visitInsn(IREM);
                    case EQ -> {
                        mv.visitJumpInsn(IF_ICMPNE, conditionNotMet);
                    }
                    case NEQ -> {
                        //Label labelNumEqBr = new Label();
                        mv.visitJumpInsn(IF_ICMPEQ, conditionNotMet);
                        /*mv.visitInsn(ICONST_1);
                        Label labelPostNumNotEq = new Label();
                        mv.visitJumpInsn(GOTO, labelPostNumNotEq);
                        mv.visitLabel(labelNumEqBr);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(labelPostNumNotEq);*/
                    }
                    case LT -> {
                        //throw new UnsupportedOperationException();
                        //Label labelNumEqBr = new Label();
                        mv.visitJumpInsn(IF_ICMPGE, conditionNotMet);
                        /*mv.visitInsn(ICONST_1);
                        Label labelPostNumNotEq = new Label();
                        mv.visitJumpInsn(GOTO, labelPostNumNotEq);
                        mv.visitLabel(labelNumEqBr);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(labelPostNumNotEq);*/
                    }
                    case LE -> {
                        //throw new UnsupportedOperationException();
                        //Label labelNumEqBr = new Label();
                        mv.visitJumpInsn(IF_ICMPGT, conditionNotMet);
                        /*mv.visitInsn(ICONST_1);
                        Label labelPostNumNotEq = new Label();
                        mv.visitJumpInsn(GOTO, labelPostNumNotEq);
                        mv.visitLabel(labelNumEqBr);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(labelPostNumNotEq);*/
                    }
                    case GT -> {
                        //throw new UnsupportedOperationException();
                        //Label labelNumEqBr = new Label();
                        mv.visitJumpInsn(IF_ICMPLE, conditionNotMet);
                        /*mv.visitInsn(ICONST_1);
                        Label labelPostNumNotEq = new Label();
                        mv.visitJumpInsn(GOTO, labelPostNumNotEq);
                        mv.visitLabel(labelNumEqBr);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(labelPostNumNotEq);*/
                    }
                    case GE -> {
                        //throw new UnsupportedOperationException();
                        //Label labelNumEqBr = new Label();
                        mv.visitJumpInsn(IF_ICMPLT, conditionNotMet);
                        /*mv.visitInsn(ICONST_1);
                        Label labelPostNumNotEq = new Label();
                        mv.visitJumpInsn(GOTO, labelPostNumNotEq);
                        mv.visitLabel(labelNumEqBr);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(labelPostNumNotEq);*/
                    }
                    default -> {
                        throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
                    }
                }
                if (TypeCheckUtils.BOOLEAN_TOKEN_SET.contains(op)) {
                    mv.visitInsn(ICONST_1);
                    Label conditionMet = new Label();
                    mv.visitJumpInsn(GOTO, conditionMet);
                    mv.visitLabel(conditionNotMet);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(conditionMet);
                }
            }
            case BOOLEAN -> {
                switch(op){
                    case EQ->{
                        mv.visitJumpInsn(IF_ICMPNE, conditionNotMet);
                    }
                    case NEQ->{
                        mv.visitJumpInsn(IF_ICMPEQ, conditionNotMet);
                    }
                    case LT->{
                        mv.visitJumpInsn(IF_ICMPGE, conditionNotMet);
                    }
                    case LE->{
                        mv.visitJumpInsn(IF_ICMPGT, conditionNotMet);
                    }
                    case GT->{
                        mv.visitJumpInsn(IF_ICMPLE, conditionNotMet);
                    }
                    case GE->{
                        mv.visitJumpInsn(IF_ICMPLT, conditionNotMet);
                    }
                }
                mv.visitInsn(ICONST_1);
                Label conditionMet = new Label();
                mv.visitJumpInsn(GOTO, conditionMet);
                mv.visitLabel(conditionNotMet);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(conditionMet);

                //throw new UnsupportedOperationException();
            }
            case STRING -> {
                switch(op){
                    case PLUS -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
                    }
                    case EQ ->{
                        mv.visitJumpInsn(IF_ACMPNE, conditionNotMet);
                    }
                    case NEQ->{
                        mv.visitJumpInsn(IF_ACMPEQ, conditionNotMet);
                    }
                    case LT,LE->{
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","startsWith","(Ljava/lang/String;)Z",false);
                    }
                    case GT,GE->{
                        mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","startsWith","(Ljava/lang/String;)Z",false);
                    }

                }

                    mv.visitInsn(ICONST_0);
                    Label conditionMet = new Label();
                    mv.visitJumpInsn(GOTO, conditionMet);
                    mv.visitLabel(conditionNotMet);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(conditionMet);


                //throw new UnsupportedOperationException();
            }
            default -> {
                throw new IllegalStateException("code gen bug in visitExpressionBinary");
            }
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object
            arg) throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object
            arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit
                                                   expressionStringLit, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
        return null;
        //throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit
                                                    expressionBooleanLit, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
        return null;
        //throw new UnsupportedOperationException();
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws
            PLPException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg)
            throws PLPException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        throw new UnsupportedOperationException();
    }
}
