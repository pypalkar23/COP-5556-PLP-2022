package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public class TypeCheckUtils {
    public static final String STRING = "STRING";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String NUMBER = "NUMBER";
    public static final String PROCEDURE = "PROCEDURE";
    public static final String ERROR_TYPE_MISMATCH = "INCOMPATIABLE_TYPE EXPECTED %s";
    public static final String ERROR_INCOMPLETE_INFORMATION = "CANNOT DETERMINE TYPE";

    public static final String ERROR_REASSIGNMENT_NOT_ALLOWED = "REASSIGNMENT NOT ALLOWED";


    public static String getStringMismatchError(String[] expected, String actual){
        String TYPE_MISMATCH = "Expected %s found %s";
        String expectedString=String.join(" OR ",expected);
        return String.format(TYPE_MISMATCH,expectedString,actual);
    }
    public static Type getConstType(ConstDec constDec){
        Object val = constDec.val;
        if(val instanceof Integer){
            return Type.NUMBER;
        }
        if(val instanceof String){
            if(val.equals("TRUE") || val.equals("FALSE")){
                return Type.BOOLEAN;
            }
        }
        return Type.STRING;
    }
}
