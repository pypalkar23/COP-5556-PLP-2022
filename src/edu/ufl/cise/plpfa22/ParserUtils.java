package edu.ufl.cise.plpfa22;

public class ParserUtils {

    public static final String WRONG_CHARACTER_DETECTED = "INVALID CHARACTER FOUND EXPECTED %s";
    public static final String INVALID_CHARACTER_FOUND = "INVALID CHARACTER FOUND";

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
