package edu.ufl.cise.plpfa22.utils;

import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.utils.CodeGenUtils;

public class CodeGenHelpers {
    public static final String STRING_TYPE = "java/lang/String";

    public static final String OBJECT_TYPE = "java/lang/Object";
    public static final String STRING_DESCRIPTOR = String.format("L%s;",STRING_TYPE);
    public static final String STRING_CONCAT_OP = "concat";
    public static final String STRING_EQUALS_OP = "equals";
    public static final String STRING_STARTS_WITH_OP = "startsWith";
    public static final String STRING_ENDS_WITH_OP = "endsWith";
    public static final String INTEGER_DATA_TYPE = "I";
    public static final String BOOLEAN_DATA_TYPE = "Z";
    public static final String VOID_DATA_TYPE = "V";

    public static final String RUN_MODE = "run";
    public static final String INIT_MODE = "<init>";
    public static final String VOID_DESCRIPTOR = "()V";
    public static final String[] interfacesList = {"java/lang/Runnable"};

    public static final String THIS_PREFIX = "this$";

    public static String getDescriptorForType(Type type){
        switch (type){
            case NUMBER -> {return INTEGER_DATA_TYPE;}
            case BOOLEAN -> {return BOOLEAN_DATA_TYPE;}
            case STRING -> {return STRING_DESCRIPTOR;}
            case PROCEDURE -> {throw null;}
        }
        return null;
    }


    public static String getInitDescriptor(String enclosingClass) {
        return "(" + CodeGenUtils.toJVMClassDesc(enclosingClass) + ")V";
    }
}
