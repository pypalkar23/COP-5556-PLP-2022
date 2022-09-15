package edu.ufl.cise.plpfa22;

public class Driver {
    public static void main(String[] args) {
        String input = """
                0 1 2 3 4 """;
        ILexer lexer = CompilerComponentFactory.getLexer(input);
        try {
            while (true) {
                IToken token = lexer.next();
                if (token.getKind() == IToken.Kind.EOF)
                    break;
                System.out.println(
                        String.format("Kind:%s\nText:%s\nLine:%s",
                                token.getKind().toString(), token.getStringValue(), token.getSourceLocation().toString()));
                System.out.println("------");
            }
        } catch (LexicalException e) {
            System.out.println(e.getMessage());
        }

       /*String input = "   \" ...  \\\"  \\\'  \\\\  \"";
       System.out.println(input);
       checkForInvertedCommas(input);
       input = "\"\\b \\t \\n \\f \\r \"";

       System.out.println(input.toCharArray());
       checkForInvertedCommas(input);
       input = """
				"This is a string"
				""";
        System.out.println(input);
        checkForInvertedCommas(input);*/
    }

    static void checkForInvertedCommas(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\\') {
                System.out.print(i + " ");
            }
        }

        System.out.println("");
    }

}
