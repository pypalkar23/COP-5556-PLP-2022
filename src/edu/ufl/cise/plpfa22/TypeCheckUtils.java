package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.IToken.Kind;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TypeCheckUtils {
    public static final String DEC_VARIABLE = "VARIABLE";

    public static final String ERROR_TYPE_MISMATCH = "INCOMPATIABLE_TYPE EXPECTED - %s";

    public static final String ERROR_INCOMPLETE_INFORMATION = "CANNOT DETERMINE TYPE";

    public static final String ERROR_REASSIGNMENT_NOT_ALLOWED = "REASSIGNMENT NOT ALLOWED";

    private static final Kind[] CONDITIONAL_TOKENS = new Kind[]{Kind.EQ, Kind.NEQ, Kind.LT, Kind.LE, Kind.GT, Kind.GE};

    public static final Set<Kind> BOOLEAN_TOKEN_SET = new HashSet<>(Arrays.asList(CONDITIONAL_TOKENS));

    public static final Kind[] NUMBER_OP_TOKENS = new Kind[]{Kind.MINUS, Kind.DIV, Kind.MOD};

    public static final Set<Kind> NUMBER_OP_TOKEN_SET = new HashSet<>(Arrays.asList(NUMBER_OP_TOKENS));

    private static final Type[] PLUS_TYPES = new Type[]{Type.NUMBER, Type.BOOLEAN, Type.STRING};

    public static final Set<Type> PLUS_TYPES_SET = new HashSet<>(Arrays.asList(PLUS_TYPES));

    private static final Type[] TIMES_TYPES = new Type[]{Type.NUMBER, Type.BOOLEAN};

    public static final Set<Type> TIMES_TYPES_SET = new HashSet<>(Arrays.asList(TIMES_TYPES));

    public static Type getConstType(ConstDec constDec) {
        Object val = constDec.val;
        if (val instanceof Integer) {
            return Type.NUMBER;
        }
        if (val instanceof Boolean)
            return Type.BOOLEAN;

        return Type.STRING;
    }

    public static boolean isCompatible(Type type1, IToken op) {
        if (op.getKind() == Kind.PLUS && !PLUS_TYPES_SET.contains(type1)) {
            return false;
        } else if (NUMBER_OP_TOKEN_SET.contains(op.getKind()) && type1 != Type.NUMBER) {
            return false;
        } else if (op.getKind() == Kind.TIMES && !TIMES_TYPES_SET.contains(type1)) {
            return false;
        } else if (BOOLEAN_TOKEN_SET.contains(op.getKind()) && !PLUS_TYPES_SET.contains(type1)) {
            return false;
        }
        return true;
    }
}
