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

    LinkedList<GenClassWriter> genList = new LinkedList<>();
    LinkedList<String> trackerList = new LinkedList<>();
    LinkedList<GenClass> resultList = new LinkedList<>();

    GenClassWriter mainWriter;

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
        mainWriter = new GenClassWriter(ClassWriter.COMPUTE_FRAMES);
        genList.add(mainWriter);
        trackerList.add(superClass);
        //Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
        // instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
        // but you will be able to print it so you can see the instructions.After fixing,
        // restore ClassWriter.COMPUTE_FRAMES
        mainWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, superClass, null, CodeGenHelpers.OBJECT_TYPE, CodeGenHelpers.interfacesList);
        mainWriter.setClassName(superClass);

        MethodVisitor first = mainWriter.visitMethod(ACC_PUBLIC, CodeGenHelpers.INIT_MODE, CodeGenHelpers.VOID_DESCRIPTOR, null, null);
        first.visitCode();
        first.visitVarInsn(ALOAD, 0);
        first.visitMethodInsn(INVOKESPECIAL, CodeGenHelpers.OBJECT_TYPE, CodeGenHelpers.INIT_MODE, CodeGenHelpers.VOID_DESCRIPTOR, false);
        first.visitInsn(RETURN);
        first.visitMaxs(1, 1);
        first.visitEnd();

        MethodVisitor second = mainWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        second.visitCode();
        second.visitTypeInsn(NEW, superClass);
        second.visitInsn(DUP);
        second.visitMethodInsn(INVOKESPECIAL, superClass, CodeGenHelpers.INIT_MODE, CodeGenHelpers.VOID_DESCRIPTOR, false);
        second.visitMethodInsn(INVOKEVIRTUAL, superClass, CodeGenHelpers.RUN_MODE, CodeGenHelpers.VOID_DESCRIPTOR, false);
        second.visitInsn(RETURN);
        second.visitMaxs(0, 0);
        second.visitEnd();
        //visit the block, passing it the methodVisitor
        program.block.visit(this, mainWriter);
        //finish up the class
        mainWriter.visitEnd();
        byte[] bytecode = mainWriter.toByteArray();
        resultList.addFirst(new GenClass(superClass, bytecode));
        return resultList;
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
        String parentClassStr = trackerList.get(statementCallNest);

        mv.visitTypeInsn(NEW, procDecClassStr);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);

        if (procDecNest == statementCallNest) {
            mv.visitMethodInsn(INVOKESPECIAL, procDecClassStr, CodeGenHelpers.INIT_MODE, String.format("(%s)V",CodeGenUtils.toJVMClassDesc(parentClassStr)), false);
        } else {
            String tempClassStr = trackerList.get(procDecNest);
            for (int i = statementCallNest; i > procDecNest; i--) {
                String temp = trackerList.get(i);
                tempClassStr = trackerList.get(i - 1);
                String referenceClassName = CodeGenHelpers.THIS_PREFIX + (i - 1);
                mv.visitFieldInsn(GETFIELD, temp, referenceClassName, CodeGenUtils.toJVMClassDesc(tempClassStr));
            }
            mv.visitMethodInsn(INVOKESPECIAL, procDecClassStr, CodeGenHelpers.INIT_MODE, String.format("(%s)V",CodeGenUtils.toJVMClassDesc(tempClassStr)), false);
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
        Statement statement = statementIf.statement;
        Expression expression = statementIf.expression;
        Label statementBlockLabel = new Label();
        expression.visit(this, arg);
        mv.visitJumpInsn(IFEQ, statementBlockLabel);
        statement.visit(this, arg);
        mv.visitLabel(statementBlockLabel);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Statement statement = statementWhile.statement;
        Expression expression = statementWhile.expression;
        Label condition = new Label();
        Label whileBlock = new Label();
        mv.visitJumpInsn(GOTO, condition);
        mv.visitLabel(whileBlock);
        statement.visit(this, arg);
        mv.visitLabel(condition);
        expression.visit(this, arg);
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
                    Label end = new Label();
                    mv.visitJumpInsn(GOTO, end);
                    mv.visitLabel(start);
                    mv.visitInsn(ICONST_1);
                    mv.visitLabel(end);
                }
                if (op == Kind.TIMES || op == Kind.GT || op == Kind.LT || op == Kind.EQ) {
                    mv.visitInsn(ICONST_1);
                    Label end = new Label();
                    mv.visitJumpInsn(GOTO, end);
                    mv.visitLabel(start);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(end);
                }
            }
            case STRING -> {
                expressionBinary.e0.visit(this, arg);
                expressionBinary.e1.visit(this, arg);
                switch (op) {
                    case PLUS -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_CONCAT_OP, "(Ljava/lang/String;)Ljava/lang/String;", false);
                    }
                    case EQ-> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_EQUALS_OP, "(Ljava/lang/Object;)Z", false);
                    }
                    case NEQ ->{
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_EQUALS_OP, "(Ljava/lang/Object;)Z", false);
                        Label start = new Label();
                        mv.visitJumpInsn(IFEQ, start);
                        mv.visitInsn(ICONST_0);
                        Label end = new Label();
                        mv.visitJumpInsn(GOTO, end);
                        mv.visitLabel(start);
                        mv.visitInsn(ICONST_1);
                        mv.visitLabel(end);
                    }
                    case LE-> {
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_STARTS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }
                    case GE -> {
                        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_ENDS_WITH_OP, "(Ljava/lang/String;)Z", false);
                    }
                    case LT ->{
                        mv.visitInsn(DUP2);
                        mv.visitMethodInsn(INVOKEVIRTUAL,  CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_EQUALS_OP, "(Ljava/lang/Object;)Z", false);
                        Label start = new Label();
                        mv.visitJumpInsn(IFNE, start);
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL,  CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_STARTS_WITH_OP, "(Ljava/lang/String;)Z", false);
                        Label end = new Label();
                        mv.visitJumpInsn(GOTO, end);
                        mv.visitLabel(start);
                        mv.visitInsn(POP2);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(end);
                    }
                    case GT ->{
                        mv.visitInsn(DUP2);
                        mv.visitMethodInsn(INVOKEVIRTUAL,  CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_EQUALS_OP, "(Ljava/lang/Object;)Z", false);
                        Label start = new Label();
                        mv.visitJumpInsn(IFNE, start);
                        mv.visitMethodInsn(INVOKEVIRTUAL,  CodeGenHelpers.STRING_TYPE, CodeGenHelpers.STRING_ENDS_WITH_OP, "(Ljava/lang/String;)Z", false);
                        Label end = new Label();
                        mv.visitJumpInsn(GOTO, end);
                        mv.visitLabel(start);
                        mv.visitInsn(POP2);
                        mv.visitInsn(ICONST_0);
                        mv.visitLabel(end);
                    }
                    default -> {
                        throw new UnsupportedOperationException("Cannot Support this for String Expressions");
                    }
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
        String typeDescriptor = CodeGenHelpers.getDescriptorForType(type);
        if (dec instanceof ConstDec) {
            ConstDec constDec = (ConstDec) dec;
            Object res = constDec.val;
            if (type.equals(Type.BOOLEAN)) {
                Boolean temp = (Boolean) res;
                if(temp.booleanValue() == true){
                    res = 1;
                }
                else{
                    res = 0;
                }
            }
            mv.visitLdcInsn(res);
        } else {
            VarDec varDec = (VarDec) dec;
            int currNestLevel = expressionIdent.getNest();
            int declarationNestLevel = varDec.getNest();
            String decClassName = trackerList.get(declarationNestLevel);
            mv.visitVarInsn(ALOAD, 0);
            for (int i = currNestLevel; i > declarationNestLevel; i--) {
                String prevClass = trackerList.get(i);
                String currClass = trackerList.get(i - 1);
                String fullReferenceDesc = CodeGenHelpers.THIS_PREFIX + (i - 1);
                mv.visitFieldInsn(GETFIELD, prevClass, fullReferenceDesc, CodeGenUtils.toJVMClassDesc(currClass));
            }
            mv.visitFieldInsn(GETFIELD, decClassName, expressionIdent.getName(), typeDescriptor);

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
        trackerList.add(currClassName);
        GenClassWriter procedureWriter = new GenClassWriter(ClassWriter.COMPUTE_FRAMES);
        procedureWriter.setClassName(currClassName);
        genList.add(procedureWriter);
        procedureWriter.visit(V17, ACC_PUBLIC, currClassName, null, CodeGenHelpers.OBJECT_TYPE, CodeGenHelpers.interfacesList);
        procedureWriter.visitNestHost(superClass);
        mainWriter.visitNestMember(currClassName);
        String currRef = CodeGenHelpers.THIS_PREFIX + procDec.getNest();
        FieldVisitor fieldVisitor = procedureWriter.visitField(ACC_FINAL | ACC_SYNTHETIC, currRef, CodeGenUtils.toJVMClassDesc(parentName), null, null);
        fieldVisitor.visitEnd();

        MethodVisitor procVisitor = procedureWriter.visitMethod(0, CodeGenHelpers.INIT_MODE, CodeGenHelpers.getInitDescriptor(parentName), null, null);
        procVisitor.visitCode();
        procVisitor.visitVarInsn(ALOAD, 0);
        procVisitor.visitVarInsn(ALOAD, 1);
        procVisitor.visitFieldInsn(PUTFIELD, currClassName, currRef, CodeGenUtils.toJVMClassDesc(parentName));
        procVisitor.visitVarInsn(ALOAD, 0);
        procVisitor.visitMethodInsn(INVOKESPECIAL, CodeGenHelpers.OBJECT_TYPE, CodeGenHelpers.INIT_MODE, CodeGenHelpers.VOID_DESCRIPTOR, false);
        procVisitor.visitInsn(RETURN);
        procVisitor.visitMaxs(0, 0);
        procVisitor.visitEnd();
        procDec.block.visit(this, procedureWriter);
        procedureWriter.visitEnd();
        byte[] byteCode = procedureWriter.toByteArray();
        resultList.add(new GenClass(currClassName, byteCode));
        trackerList.removeLast();
        genList.removeLast();
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        MethodVisitor mv = (MethodVisitor) arg;
        Declaration dec = ident.getDec();
        int identNest = ident.getNest();
        int declarationNest = dec.getNest();
        String declarationClassName = trackerList.get(declarationNest);
        mv.visitVarInsn(ALOAD, 0);
        for (int i = identNest; i > declarationNest; i--) {
            String prevLevelClass = trackerList.get(i);
            String currLevelClass = trackerList.get(i - 1);
            String fullRefDesc = CodeGenHelpers.THIS_PREFIX + (i - 1);
            mv.visitFieldInsn(GETFIELD, prevLevelClass, fullRefDesc, CodeGenUtils.toJVMClassDesc(currLevelClass));
        }
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(PUTFIELD, declarationClassName, ident.getStringIdentifier(), CodeGenHelpers.getDescriptorForType(dec.getType()));
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



}
