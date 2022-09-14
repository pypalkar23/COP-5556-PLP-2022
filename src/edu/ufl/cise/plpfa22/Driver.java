package edu.ufl.cise.plpfa22;

public class Driver {
    public static void main(String[] args) {
        String input="""
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
        ILexer lexer =  CompilerComponentFactory.getLexer(input);
        IToken<IToken.SourceLocation> token = null;
        try{
            System.out.println(lexer.next().getText());
            System.out.println(lexer.next().getText());
            System.out.println(lexer.next().getText());
            System.out.println(lexer.next().getText());
        }
        catch(LexicalException e){
            System.out.println(e.getMessage());
        }
    }
}
