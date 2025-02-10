/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized.
 */

package edu.ufl.cise.plpfa22.utils;

/**
 * This class contains several static methods useful when developing and testing
 * the code generation part of our compiler.
 *
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import edu.ufl.cise.plpfa22.interfaces.IToken;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.ufl.cise.plpfa22.ast.Types.Type;

public class CodeGenUtils{

    public record GenClass(String className, byte[] byteCode) {}


    /**
     * Converts a classNume in Java notation with . separator to JVM classname
     * with / separator
     *
     * If already in JVM form, the original String is returned
     *
     * @param className
     * @return
     */
    static String toJMVClassName(String className) {
        return className.replace('.','/');
    }


    /**
     * Converts a classNume in JVM notation with / separator to Java style classname
     * with . separator
     *
     * If already in Java-style form, the original String is returned
     *
     * @param jvmClassName
     * @return
     */
    static String toJavaClassName(String jvmClassName) {
        return jvmClassName.replace('/', '.');
    }

    public static String toJVMClassDesc(String className) {
        return "L"+ toJMVClassName(className)+";";
    }

    /**
     * Converts the provided byte array
     * in a human readable format and returns as a String.
     *
     * @param bytecode
     */
    public static String bytecodeToString(byte[] bytecode) {
        int flags = ClassReader.SKIP_DEBUG;
        ClassReader cr;
        cr = new ClassReader(bytecode);
        StringWriter out = new StringWriter();
        cr.accept(new TraceClassVisitor(new PrintWriter(out)), flags);
        return out.toString();
    }

    /**
     */
    public static class DynamicClassLoader extends ClassLoader {

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public DynamicClassLoader() {
            super();
        }

        public Class<?> define(String className, byte[] bytecode) {
            return super.defineClass(className, bytecode, 0, bytecode.length);
        }

        //requires mainclass to be first class in list
        public Class<?> define(List<GenClass> generatedClasses) {
            Class<?> mainClass = null;
            for (GenClass genClass : generatedClasses) {
                Class<?> cl = define(toJavaClassName(genClass.className()), genClass.byteCode());
                if (mainClass == null)
                    mainClass = cl;
            }
            return mainClass;
        }

    };

    /**
     * Use for debugging only.
     * Generates code to print the given String followed by ; to the standard output to allow observation of execution of generated program
     * during development.
     *
     * @param mv
     * @param message
     */
    public static void genDebugPrint(MethodVisitor mv, String message) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(message + ";");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
    }

    /**
     * Use for debugging only
     * Generates code to print the value on top of the stack to the standard output without consuming it.
     * Requires stack not empty
     *
     * @param mv
     * @param type
     */
    public static void genDebugPrintTOS(MethodVisitor mv, Type type) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(Opcodes.SWAP);
        if (type.equals(Type.NUMBER)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);
        }
        else if (type.equals(Type.BOOLEAN)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Z)V", false);
        }
        else if (type.equals(Type.STRING)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
        }
        else
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(";\n");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
    }

    public static class ParserUtils {

        public static final String WRONG_CHARACTER_DETECTED = "INVALID CHARACTER FOUND EXPECTED %s";
        public static final String INVALID_CHARACTER_FOUND = "INVALID CHARACTER FOUND";
        public static final String SYNTAX_ERROR = "SYNTAX_ERROR";

        public static Object getConstVal(IToken token) {
            switch (token.getKind()) {
                case STRING_LIT -> {
                    return token.getStringValue();
                }
                case NUM_LIT -> {
                    return token.getIntValue();
                }
                case BOOLEAN_LIT -> {
                    return token.getBooleanValue();
                }
                default -> {
                    return null;
                }
            }
        }
    }

    public static class LexerUtils {
        public enum State{
            START,
            KEYWORD,
            IDENTIFIER,
            STRING_LIT_ESCAPE_QUOTED,
            STRING_LIT_DOUBLE_QUOTED,
            INT_DETECTED,
            COLON_DETECTED,
            ESCAPE_SEQ,
            COMPARISON_DETECTED,
            WHITESPACE,
            COMMENT_START
        }

        public static final String DOT = ".";
        public static final String COMMA = ",";
        public static final String SEMI = ";";
        public static final String QUOTE = "\"";
        public static final String LPAREN = "(";
        public static final String RPAREN = ")";
        public static final String PLUS = "+";
        public static final String MINUS = "-";
        public static final String TIMES = "*";
        public static final String DIV = "/";
        public static final String MOD = "%";
        public static final String QUESTION = "?";
        public static final String BANG = "!";
        public static final String COLON = ":";
        public static final String ASSIGN = ":=";
        public static final String EQ = "=";
        public static final String NEQ = "#";
        public static final String LT = "<";
        public static final String LE = "<=";
        public static final String GT = ">";
        public static final String GE = ">=";
        public static final String KW_CONST = "CONST";
        public static final String KW_VAR = "VAR";
        public static final String KW_PROCEEDURE = "PROCEDURE";
        public static final String KW_CALL = "CALL";
        public static final String KW_BEGIN = "BEGIN";
        public static final String KW_END = "END";
        public static final String KW_IF = "IF";
        public static final String KW_WHILE = "WHILE";
        public static final String KW_DO = "DO";
        public static final String KW_THEN = "THEN";
        public static final String BOOL_TRUE = "TRUE";
        public static final String BOOL_FALSE = "FALSE";
        public static final String WHITESPACE = " ";
        public static final String TAB = "\t";
        public static final String NEW_LINE = "\n";
        public static final String CARRIAGE_RETURN = "\r";
        public static final String FORM_FEED = "\f";
        public static final String BACKSPACE = "\b";
        public static final String BACKSLASH = "\\";
        public static  final String SINGLE_QUOTE = "\'";

        public static final String ERROR_NUM_TOO_BIG = "Number to big to parse";
        public static final String ERROR_INVALID_CHAR_DETECTED = "Invalid Character Detected";
        public static final String ERROR_REACHED_END_OF_FILE = "Reached End of File";


        static String[] symbolArr = new String[]{DOT, COMMA, SEMI, LPAREN, RPAREN, PLUS, MINUS, TIMES, DIV, MOD, QUESTION, BANG, ASSIGN, EQ, NEQ, LT, GT};
        static String[] keywordArr = new String[]{KW_CONST, KW_VAR, KW_PROCEEDURE, KW_CALL, KW_BEGIN, KW_END, KW_IF, KW_WHILE, KW_DO};
        static String[] boolean_lit = new String[]{BOOL_TRUE, BOOL_FALSE};
        static String[] spaceChars = new String[]{"b","t","n","f","r"};

        static Set<String> spaceCharsSet = new HashSet<>(Arrays.asList(spaceChars));

        public static Map<String, IToken.Kind> KIND_MAP = new HashMap<>() {
            {
                put(DOT, IToken.Kind.DOT);
                put(COMMA, IToken.Kind.COMMA);
                put(SEMI, IToken.Kind.SEMI);
                put(QUOTE, IToken.Kind.QUOTE);
                put(LPAREN, IToken.Kind.LPAREN);
                put(RPAREN, IToken.Kind.RPAREN);
                put(PLUS, IToken.Kind.PLUS);
                put(MINUS, IToken.Kind.MINUS);
                put(TIMES, IToken.Kind.TIMES);
                put(DIV, IToken.Kind.DIV);
                put(MOD, IToken.Kind.MOD);
                put(QUESTION, IToken.Kind.QUESTION);
                put(BANG, IToken.Kind.BANG);
                put(ASSIGN, IToken.Kind.ASSIGN);
                put(EQ, IToken.Kind.EQ);
                put(NEQ, IToken.Kind.NEQ);
                put(GT, IToken.Kind.GT);
                put(LT, IToken.Kind.LT);
                put(LE, IToken.Kind.LE);
                put(GE, IToken.Kind.GE);
                put(BOOL_TRUE, IToken.Kind.BOOLEAN_LIT);
                put(BOOL_FALSE, IToken.Kind.BOOLEAN_LIT);
                put(KW_CONST, IToken.Kind.KW_CONST);
                put(KW_VAR, IToken.Kind.KW_VAR);
                put(KW_PROCEEDURE, IToken.Kind.KW_PROCEDURE);
                put(KW_CALL, IToken.Kind.KW_CALL);
                put(KW_BEGIN, IToken.Kind.KW_BEGIN);
                put(KW_END, IToken.Kind.KW_END);
                put(KW_IF, IToken.Kind.KW_IF);
                put(KW_WHILE, IToken.Kind.KW_WHILE);
                put(KW_DO, IToken.Kind.KW_DO);
                put(KW_THEN, IToken.Kind.KW_THEN);
            }
        };


    }
}
