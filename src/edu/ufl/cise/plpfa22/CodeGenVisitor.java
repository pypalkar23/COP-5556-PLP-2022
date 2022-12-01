package edu.ufl.cise.plpfa22;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.*;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;



public class CodeGenVisitor implements ASTVisitor, Opcodes {
    final String packageName;
    final String className;
    final String sourceFileName;
    final String classDescriptor;
    final String superClass;

    GenClassWriter superClassWriter;

    LinkedList<GenClassWriter> classWriters = new LinkedList<>();
    LinkedList<String> classNames = new LinkedList<>();

    LinkedList<GenClass> classList = new LinkedList<>();

    public CodeGenVisitor(String className, String packageName, String sourceFileName) {
        super();
        this.packageName = packageName;
        this.className = className;
        this.sourceFileName = sourceFileName;
        this.superClass = packageName + "/" + className;
        this.classDescriptor = "L" + this.superClass + ';';
    }


    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        GenClassWriter genClassWriter = (GenClassWriter) arg;
        for (VarDec varDec : block.varDecs) {
            varDec.visit(this, genClassWriter);
        }
        for (ProcDec procDec : block.procedureDecs) {
            procDec.visit(this, genClassWriter.className);
        }

        MethodVisitor methodVisitor = genClassWriter.visitMethod(ACC_PUBLIC, CodeGenHelpers.RUN_MODE, CodeGenHelpers.VOID_DESCRIPTOR, null, null);
        methodVisitor.visitCode();

        block.statement.visit(this, methodVisitor);

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();

        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        //create a classWriter and visit it
        NameVisitor setNamesVisitor = new NameVisitor(superClass);
        program.visit(setNamesVisitor, superClass);
        superClassWriter = new GenClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriters.add(superClassWriter);
        classNames.add(superClass);
        //Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
        // instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
        // but you will be able to print it so you can see the instructions.After fixing,
        // restore ClassWriter.COMPUTE_FRAMES
        superClassWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, superClass, null, "java/lang/Object", CodeGenHelpers.interfacesList);
        superClassWriter.setClassName(superClass);

        MethodVisitor first = superClassWriter.visitMethod(ACC_PUBLIC, CodeGenHelpers.INIT_MODE, "()V", null, null);
        first.visitCode();
        first.visitVarInsn(ALOAD, 0);
        first.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", CodeGenHelpers.INIT_MODE, "()V", false);
        first.visitInsn(RETURN);
        first.visitMaxs(1, 1);
        first.visitEnd();


        MethodVisitor second = superClassWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        second.visitCode();
        second.visitTypeInsn(NEW, superClass);
        second.visitInsn(DUP);
        second.visitMethodInsn(INVOKESPECIAL, superClass, CodeGenHelpers.INIT_MODE, "()V", false);
        second.visitMethodInsn(INVOKEVIRTUAL, superClass, CodeGenHelpers.RUN_MODE, "()V", false);
        second.visitInsn(RETURN);
        second.visitMaxs(0, 0);
        second.visitEnd();
        //visit the block, passing it the methodVisitor
        program.block.visit(this, superClassWriter);
        //finish up the class
        superClassWriter.visitEnd();
        byte[] bytecode = superClassWriter.toByteArray();
        classList.addFirst(new GenClass(superClass, bytecode));
        return classList;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        Expression expression = statementAssign.expression;
        Ident ident = statementAssign.ident;
        expression.visit(this, arg);
        ident.visit(this, arg);
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        GenClassWriter genClassWriter = (GenClassWriter) arg;
        String name = varDec.getVarName();
        Type type = varDec.getType();
        if (type != null) {
            FieldVisitor fieldVisitor = genClassWriter.visitField(ACC_PUBLIC, name, CodeGenHelpers.getDescriptorForType(varDec.getType()), null, null);
            fieldVisitor.visitEnd();
        }
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        ProcDec procDec = (ProcDec) statementCall.ident.getDec();
        String procDecClassStr = procDec.getSetFullClassName();
        int procDecNest = procDec.getNest();

        int statementCallNest = statementCall.ident.getNest();
        String parentClassStr = classNames.get(statementCallNest);

        mv.visitTypeInsn(NEW, procDecClassStr);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);

        if (procDecNest == statementCallNest) {
            mv.visitMethodInsn(INVOKESPECIAL, procDecClassStr, CodeGenHelpers.INIT_MODE, "(" + CodeGenUtils.toJVMClassDesc(parentClassStr) + ")V", false);
        } else {
            String tempClassName = classNames.get(procDecNest);
            for (int i = statementCallNest; i > procDecNest; i--) {
                String temp = classNames.get(i);
                tempClassName = classNames.get(i - 1);
                String referenceClassName = CodeGenHelpers.THIS_PREFIX + (i - 1);
                mv.visitFieldInsn(GETFIELD, temp, referenceClassName, CodeGenUtils.toJVMClassDesc(tempClassName));
            }
            mv.visitMethodInsn(INVOKESPECIAL, procDecClassStr, CodeGenHelpers.INIT_MODE, "(" + CodeGenUtils.toJVMClassDesc(tempClassName) + ")V", false);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, procDecClassStr, CodeGenHelpers.RUN_MODE, CodeGenHelpers.VOID_DESCRIPTOR, false);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
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
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Label afterIf = new Label();
        statementIf.expression.visit(this, arg);
        mv.visitJumpInsn(IFEQ, afterIf);
        statementIf.statement.visit(this, arg);
        mv.visitLabel(afterIf);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Label condition = new Label();
        Label whileBlock = new Label();
        mv.visitJumpInsn(GOTO, condition);
        mv.visitLabel(whileBlock);
        statementWhile.statement.visit(this, arg);
        mv.visitLabel(condition);
        statementWhile.expression.visit(this, arg);
        mv.visitJumpInsn(IFNE, whileBlock);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object
            arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Type argType = expressionBinary.e0.getType();
        Kind op = expressionBinary.op.getKind();

        Label conditionNotMet = new Label();
        switch (argType) {
            case NUMBER -> {
                expressionBinary.e0.visit(this, arg);
                expressionBinary.e1.visit(this, arg);
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
                        mv.visitJumpInsn(IF_ICMPEQ, conditionNotMet);
                    }
                    case LT -> {
                        mv.visitJumpInsn(IF_ICMPGE, conditionNotMet);
                    }
                    case LE -> {
                        mv.visitJumpInsn(IF_ICMPGT, conditionNotMet);
                    }
                    case GT -> {
                        mv.visitJumpInsn(IF_ICMPLE, conditionNotMet);
                    }
                    case GE -> {
                        mv.visitJumpInsn(IF_ICMPLT, conditionNotMet);
                    }
                    default -> {
                        throw new UnsupportedOperationException("Cannot Support this for Number Expressions");
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
                expressionBinary.e0.visit(this, arg);
                Label start = new Label();
                switch (op) {
                    case PLUS -> {
                        mv.visitJumpInsn(IFNE, start);
                        expressionBinary.e1.visit(this, arg);
                        mv.visitJumpInsn(IFNE, start);
                    }
                    case TIMES -> {
                        mv.visitJumpInsn(IFEQ, start);
                        expressionBinary.e1.visit(this, arg);
                        mv.visitJumpInsn(IFEQ, start);

                    }
                    case EQ -> {
                        expressionBinary.e1.visit(this, arg);
                        start = new Label();
                        mv.visitJumpInsn(IF_ICMPNE, start);
                    }
                    case NEQ -> {
                        expressionBinary.e1.visit(this, arg);
                        mv.visitInsn(IXOR);
                    }
                    case LT, GE -> {
                        mv.visitJumpInsn(IFNE, start);
                        expressionBinary.e1.visit(this, arg);
                        mv.visitJumpInsn(IFEQ, start);
                    }
                    case LE, GT -> {
                        mv.visitJumpInsn(IFEQ, start);
                        expressionBinary.e1.visit(this, arg);
                        mv.visitJumpInsn(IFNE, start);
                    }

                    default -> {
                        throw new UnsupportedOperationException("Cannot Support this for Boolean Expressions");
                    }
                }

                if (op == Kind.PLUS || op == Kind.LE || op == Kind.GE) {
                    mv.visitInsn(ICONST_0);
                    Label label45 = new Label();
                    mv.visitJumpInsn(GOTO, label45);
                    mv.visitLabel(start);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(label45);
                }
                if (op == Kind.TIMES || op == Kind.GT || op == Kind.LT || op == Kind.EQ) {
                    mv.visitInsn(ICONST_1);
                    Label afterAnd = new Label();
                    mv.visitJumpInsn(GOTO, afterAnd);
                    mv.visitLabel(start);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(afterAnd);
                }
            }
            case STRING -> {
                expressionBinary.e0.visit(this, arg);
                expressionBinary.e1.visit(this, arg);
                switch (op) {
                    case PLUS -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_CONCAT_OP, "(Ljava/lang/String;)Ljava/lang/String;", false);
                    }
                    case EQ, NEQ -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_EQUALS_OP, "(Ljava/lang/Object;)Z", false);
                    }
                    case LE,GT -> {
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_STARTS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }
                    case GE -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_ENDS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }
                    case LT ->{
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_ENDS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }
                    /*case GT ->{
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_STARTS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }*/
                    default -> {
                        throw new UnsupportedOperationException("Cannot Support this for String Expressions");
                    }
                }

                if (op == Kind.NEQ || op == Kind.LT || op == Kind.GT) {
                    Label start = new Label();
                    mv.visitJumpInsn(IFEQ, start);
                    mv.visitInsn(ICONST_0);
                    Label end = new Label();
                    mv.visitJumpInsn(GOTO, end);
                    mv.visitLabel(start);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(end);
                }
            }
            default -> {
                throw new UnsupportedOperationException("Cannot Support this for Binary Expression");
            }
        }
        return null;
    }


    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Declaration dec = expressionIdent.getDec();
        Type type = dec.getType();
        String jvmType = CodeGenHelpers.getDescriptorForType(type);
        if (dec instanceof ConstDec) {
            ConstDec cdec = (ConstDec) dec;
            Object val = cdec.val;
            if (type.equals(Type.BOOLEAN)) {
                boolean bval = ((Boolean) val).booleanValue();
                val = Integer.valueOf(bval ? 1 : 0);
            }
            mv.visitLdcInsn(val);
        } else {
            VarDec vdec = (VarDec) dec;
            int currNestLevel = expressionIdent.getNest();
            int declarationNestLevel = vdec.getNest();
            String decClassName = classNames.get(declarationNestLevel);
            mv.visitVarInsn(ALOAD, 0);
            for (int i = currNestLevel; i > declarationNestLevel; i--) {
                String nName = classNames.get(i);
                String tName = classNames.get(i - 1);
                String fullReferenceDesc = CodeGenHelpers.THIS_PREFIX + (i - 1);
                mv.visitFieldInsn(GETFIELD, nName, fullReferenceDesc, CodeGenUtils.toJVMClassDesc(tName));
            }
            mv.visitFieldInsn(GETFIELD, decClassName, expressionIdent.getName(), jvmType);

        }
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
        return null;
    }



    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        String currClassName = procDec.getSetFullClassName();
        String parentName = procDec.getParentClass();
        classNames.add(currClassName);

        GenClassWriter procedureWriter = new GenClassWriter(ClassWriter.COMPUTE_FRAMES);
        procedureWriter.setClassName(currClassName);
        classWriters.add(procedureWriter);
        procedureWriter.visit(V17, ACC_PUBLIC, currClassName, null, "java/lang/Object", CodeGenHelpers.interfacesList);

        procedureWriter.visitNestHost(superClass);
        superClassWriter.visitNestMember(currClassName);

        String currRef = CodeGenHelpers.THIS_PREFIX + procDec.getNest();

        FieldVisitor fieldVisitor = procedureWriter.visitField(ACC_FINAL | ACC_SYNTHETIC, currRef, CodeGenUtils.toJVMClassDesc(parentName), null, null);
        fieldVisitor.visitEnd();


        MethodVisitor procVisitor = procedureWriter.visitMethod(0, "<init>", CodeGenHelpers.getInitDescriptor(parentName), null, null);
        procVisitor.visitCode();
        procVisitor.visitVarInsn(ALOAD, 0);
        procVisitor.visitVarInsn(ALOAD, 1);
        procVisitor.visitFieldInsn(PUTFIELD, currClassName, currRef, CodeGenUtils.toJVMClassDesc(parentName));
        procVisitor.visitVarInsn(ALOAD, 0);
        procVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        procVisitor.visitInsn(RETURN);
        procVisitor.visitMaxs(0, 0);
        procVisitor.visitEnd();


        procDec.block.visit(this, procedureWriter);

        procedureWriter.visitEnd();
        byte[] byteCode = procedureWriter.toByteArray();
        classList.add(new GenClass(currClassName, byteCode));
        classNames.removeLast();
        classWriters.removeLast();
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Declaration dec = ident.getDec();
        int identNest = ident.getNest();
        int declarationNest = dec.getNest();
        String currClassName = classNames.get(identNest);
        String declarationClassName = classNames.get(declarationNest);
        mv.visitVarInsn(ALOAD, 0);
        for (int i = identNest; i > declarationNest; i--) {
            String prevLevelClass = classNames.get(i);
            String currLevelClass = classNames.get(i - 1);
            String fullRefDesc = CodeGenHelpers.THIS_PREFIX + (i - 1);
            mv.visitFieldInsn(GETFIELD, prevLevelClass, fullRefDesc, CodeGenUtils.toJVMClassDesc(currLevelClass));
        }
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(PUTFIELD, declarationClassName, ident.getStringIdentifier(), CodeGenHelpers.getDescriptorForType(dec.getType()));
        return null;
    }

}
