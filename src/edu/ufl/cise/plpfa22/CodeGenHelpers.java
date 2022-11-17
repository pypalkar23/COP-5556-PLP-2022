package edu.ufl.cise.plpfa22;

public class CodeGenHelpers {
    public static final String STRING_TYPE = "java/lang/String";
    public static final String STRING_DESCRIPTOR = String.format("L%s;",STRING_TYPE);
    public static final String STRING_CONCAT_OP = "concat";
    public static final String STRING_EQUALS_OP = "equals";
    public static final String STRING_STARTS_WITH_OP = "startsWith";
    public static final String STRING_ENDS_WITH_OP = "endsWith";
    public static final String INTEGER_DATA_TYPE = "I";
    public static final String BOOLEAN_DATA_TYPE = "Z";
    public static final String VOID_DATA_TYPE = "V";

}
