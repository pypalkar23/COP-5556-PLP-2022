/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized.
 */

package edu.ufl.cise.plpfa22;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plpfa22.CompilerComponentFactory;
import edu.ufl.cise.plpfa22.ILexer;
import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.LexicalException;

class LexerTest {


    /*** Useful functions ***/
    ILexer getLexer(String input){
        return CompilerComponentFactory.getLexer(input);
    }

    //makes it easy to turn output on and off (and less typing than System.out.println)
    static final boolean VERBOSE = true;
    void show(Object obj) {
        if(VERBOSE) {
            System.out.println(obj);
        }
    }

    //check that this token has the expected kind
    void checkToken(IToken t, Kind expectedKind) {
        assertEquals(expectedKind, t.getKind());
    }

    //check that the token has the expected kind and position
    void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn){
        assertEquals(expectedKind, t.getKind());
        assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
    }

    //check that this token is an IDENT and has the expected name
    void checkIdent(IToken t, String expectedName){
        assertEquals(Kind.IDENT, t.getKind());
        assertEquals(expectedName, String.valueOf(t.getText()));
    }

    //check that this token is an IDENT, has the expected name, and has the expected position
    void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn){
        checkIdent(t,expectedName);
        assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
    }


    //check that this token is an NUM_LIT with expected int value
    void checkInt(IToken t, int expectedValue) {
        assertEquals(Kind.NUM_LIT, t.getKind());
        assertEquals(expectedValue, t.getIntValue());
    }

    //check that this token  is an NUM_LIT with expected int value and position
    void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
        checkInt(t,expectedValue);
        assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
    }

    //check that this token is the EOF token
    void checkEOF(IToken t) {
        checkToken(t, Kind.EOF);
    }

    /***Tests****/

    //The lexer should add an EOF token to the end.
    @Test
    void testEmpty() throws LexicalException {
        String input = "";
        show(input);
        ILexer lexer = getLexer(input);
        show(lexer);
        checkEOF(lexer.next());
    }

    //A couple of single character tokens
    @Test
    void testSingleChar0() throws LexicalException {
        String input = """
				+
				-
				""";
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.PLUS, 1,1);
        checkToken(lexer.next(), Kind.MINUS, 2,1);
        checkEOF(lexer.next());
    }

    //comments should be skipped
    @Test
    void testComment0() throws LexicalException {
        //Note that the quotes around "This is a string" are passed to the lexer.
        String input = """
				"This is a string"
				// this is a comment
				*
				""";
        show(input);
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.STRING_LIT, 1,1);
        checkToken(lexer.next(), Kind.TIMES, 3,1);
        checkEOF(lexer.next());
    }

    //Example for testing input with an illegal character
    @Test
    void testError0() throws LexicalException {
        String input = """
				abc
				@
				""";
        show(input);
        ILexer lexer = getLexer(input);
        //this check should succeed
        checkIdent(lexer.next(), "abc");
        //this is expected to throw an exception since @ is not a legal
        //character unless it is part of a string or comment
        assertThrows(LexicalException.class, () -> {
            @SuppressWarnings("unused")
            IToken token = lexer.next();
        });
    }

    //Several identifiers to test positions
    @Test
    public void testIdent0() throws LexicalException {
        String input = """
				abc
				  def
				     ghi

				""";
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "abc", 1,1);
        checkIdent(lexer.next(), "def", 2,3);
        checkIdent(lexer.next(), "ghi", 3,6);
        checkEOF(lexer.next());
    }


    @Test
    public void testIdenInt() throws LexicalException {
        String input = """
				a123 456b
				""";
        show(input);
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "a123", 1,1);
        checkInt(lexer.next(), 456, 1,6);
        checkIdent(lexer.next(), "b",1,9);
        checkEOF(lexer.next());
    }


    @Test
    public void testEscapeSequences0() throws LexicalException {
        String input = "\"\\b \\t \\n \\f \\r \"";
        show(input);
        ILexer lexer = getLexer(input);
        IToken t = lexer.next();
        String val = t.getStringValue();
        String expectedStringValue = "\b \t \n \f \r ";
        assertEquals(expectedStringValue, val);
        String text = String.valueOf(t.getText());
        String expectedText = "\"\\b \\t \\n \\f \\r \"";
        assertEquals(expectedText,text);
    }

    @Test
    public void testEscapeSequences1() throws LexicalException {
        String input = "   \" ...  \\\"  \\\'  \\\\  \"";
        show(input);
        ILexer lexer = getLexer(input);
        IToken t = lexer.next();
        String val = t.getStringValue();
        String expectedStringValue = " ...  \"  \'  \\  ";
        assertEquals(expectedStringValue, val);
        String text = String.valueOf(t.getText());
        String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; //almost the same as input, but white space is omitted
        assertEquals(expectedText,text);
    }

    // Keyword followed by a token
    @Test
    public void testComplexCombinations() throws LexicalException {
        String input = """
				(value)
				(FALSE)
				PROCEDURE*
				""";
        show(input);
        ILexer lexer = getLexer(input);

        checkToken(lexer.next(), Kind.LPAREN, 1,1);
        checkIdent(lexer.next(), "value", 1, 2);
        checkToken(lexer.next(), Kind.RPAREN, 1, 7);

        checkToken(lexer.next(), Kind.LPAREN, 2,1);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 2, 2);
        checkToken(lexer.next(), Kind.RPAREN, 2, 7);

        checkToken(lexer.next(), Kind.KW_PROCEDURE, 3, 1);
        checkToken(lexer.next(), Kind.TIMES, 3, 10);
    }


    //Example showing how to handle number that are too big.
    @Test
    public void testIntTooBig() throws LexicalException {
        String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
        ILexer lexer = getLexer(input);
        checkInt(lexer.next(),42);
        Exception e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
    }

    @Test
    public void testBoolean() throws LexicalException {
        String input = """
				FALSE TRUE
				FALSE
				true
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 1,1);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 1,7);
        checkToken(lexer.next(), Kind.BOOLEAN_LIT, 2,1);
        checkToken(lexer.next(), Kind.IDENT, 3,1);
    }

    //Tokens in a string
    @Test
    public void testValidString() throws LexicalException {
        String input = """
				"test text"
				"@"
				"TRUE"
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.STRING_LIT, 1,1);
        checkToken(lexer.next(), Kind.STRING_LIT, 2,1);
        checkToken(lexer.next(), Kind.STRING_LIT, 3,1);
    }

    //Generic Expressions
    @Test
    public void testEQ() throws LexicalException {
        String input = """
				i=3;
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "i",1,1);
        checkToken(lexer.next(), Kind.EQ, 1,2);
        checkInt(lexer.next(), 3, 1, 3);
    }

    //ASSIGN
    @Test
    public void testASSIGN() throws LexicalException {
        String input = """
				i:=3;
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "i",1,1);
        checkToken(lexer.next(), Kind.ASSIGN, 1,2);
        checkInt(lexer.next(), 3, 1, 4);
    }

    //ASSIGN
    @Test
    public void testValidIdentifiers() throws LexicalException {
        String input = """
				variable_123
				a$5
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "variable_123",1,1);
        checkIdent(lexer.next(), "a$5",2,1);
    }

    //Generic <= expression
    @Test
    public void testLEExpression() throws LexicalException {
        String input = """
				IF I<=j
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.KW_IF, 1, 1);
        checkIdent(lexer.next(), "I",1, 4);
        checkToken(lexer.next(), Kind.LE, 1, 5);
        checkIdent(lexer.next(), "j",1,7);
    }

    //Generic <= expression
    @Test
    public void testGEExpression() throws LexicalException {
        String input = """
				I>=j
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "I",1, 1);
        checkToken(lexer.next(), Kind.GE, 1, 2);
        checkIdent(lexer.next(), "j",1,4);
    }

    //Generic <= expression
    @Test
    public void testGTExpression() throws LexicalException {
        String input = """
				I>j
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "I",1, 1);
        checkToken(lexer.next(), Kind.GT, 1, 2);
        checkIdent(lexer.next(), "j",1,3);
    }

    //Generic <= expression
    @Test
    public void testEQExpression() throws LexicalException {
        String input = """
				IF I5=j
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.KW_IF, 1, 1);
        checkIdent(lexer.next(), "I5",1, 4);
        checkToken(lexer.next(), Kind.EQ, 1, 6);
        checkIdent(lexer.next(), "j",1,7);
    }

    //Tokens in a string
    @Test
    public void testInvalidString() throws LexicalException {
        String input = """
				test text"
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "test", 1, 1);
        checkIdent(lexer.next(), "text", 1, 6);
        Exception e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
    }

    // #IF
    @Test
    public void testNEQIF() throws LexicalException {
        String input = """
				#IF
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.NEQ, 1, 1);
        checkToken(lexer.next(), Kind.KW_IF, 1, 2);
    }

    // #IF-THEN
    @Test
    public void testIFTHEN() throws LexicalException {
        String input = """
				IF i < 5 THEN i = j
				""";
        ILexer lexer = getLexer(input);
        checkToken(lexer.next(), Kind.KW_IF, 1, 1);
        checkIdent(lexer.next(), "i", 1, 4);
        checkToken(lexer.next(), Kind.LT, 1, 6);
        checkInt(lexer.next(), 5, 1, 8);
        checkToken(lexer.next(), Kind.KW_THEN, 1, 10);

        checkIdent(lexer.next(), "i", 1, 15);
        checkToken(lexer.next(), Kind.EQ, 1, 17);
        checkIdent(lexer.next(), "j", 1, 19);
    }

    //Valid Integer 0
    @Test
    public void testValidIntZero() throws LexicalException {
        String input = """
				i=0
				""";
        ILexer lexer = getLexer(input);
        checkIdent(lexer.next(), "i", 1, 1);
        checkToken(lexer.next(), Kind.EQ, 1, 2);
        checkInt(lexer.next(), 0, 1, 3);
    }

    //Invalid tokens
    @Test
    public void testInvalidToken() throws LexicalException {
        String input = """
				@ ^
				""";
        ILexer lexer = getLexer(input);
        Exception e = assertThrows(LexicalException.class, () -> {
            lexer.next();
            lexer.next();
        });
    }
}


