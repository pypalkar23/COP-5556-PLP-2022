package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
public class Driver {
    public static void main(String[] args) {
        String input = """
				CONST a=3;
				VAR x,y,z;
				PROCEDURE p;
				  VAR j;
				  BEGIN
				     ? x;
				     IF x = 0 THEN ! y ;
				     WHILE j < 24 DO CALL z
				  END;
				! a+b - (c/e) * 35/(3+4)
				.
				""";
        try {
            /*ILexer lexer=CompilerComponentFactory.getLexer(input);
            while(true){
                IToken token = lexer.next();
                if(token.getKind() == IToken.Kind.EOF)
                    break;
                System.out.println(token.getKind());
            }*/
            IParser parser = CompilerComponentFactory.getParser(CompilerComponentFactory.getLexer(input));
            ASTNode ast = parser.parse();
            assertThat("", ast, instanceOf(Program.class));
            Block v0 = ((Program) ast).block;
            assertThat("", v0, instanceOf(Block.class));
            List<ConstDec> v1 = ((Block) v0).constDecs;
            assertEquals(1, v1.size());
            assertThat("", v1.get(0), instanceOf(ConstDec.class));
            IToken v2 = ((ConstDec) v1.get(0)).ident;
            assertEquals("a", String.valueOf(v2.getText()));
            Integer v3 = (Integer) ((ConstDec) v1.get(0)).val;
            assertEquals(3, v3);
            List<VarDec> v4 = ((Block) v0).varDecs;
            assertEquals(3, v4.size());
            assertThat("", v4.get(0), instanceOf(VarDec.class));
            IToken v5 = ((VarDec) v4.get(0)).ident;
            assertEquals("x", String.valueOf(v5.getText()));
            assertThat("", v4.get(1), instanceOf(VarDec.class));
            IToken v6 = ((VarDec) v4.get(1)).ident;
            assertEquals("y", String.valueOf(v6.getText()));
            assertThat("", v4.get(2), instanceOf(VarDec.class));
            IToken v7 = ((VarDec) v4.get(2)).ident;
            assertEquals("z", String.valueOf(v7.getText()));
            List<ProcDec> v8 = ((Block) v0).procedureDecs;
            assertEquals(1, v8.size());
            assertThat("", v8.get(0), instanceOf(ProcDec.class));
            IToken v9 = ((ProcDec) v8.get(0)).ident;
            assertEquals("p", String.valueOf(v9.getText()));
            Block v10 = ((ProcDec) v8.get(0)).block;
            assertThat("", v10, instanceOf(Block.class));
            List<ConstDec> v11 = ((Block) v10).constDecs;
            assertEquals(0, v11.size());
            List<VarDec> v12 = ((Block) v10).varDecs;
            assertEquals(1, v12.size());
            assertThat("", v12.get(0), instanceOf(VarDec.class));
            IToken v13 = ((VarDec) v12.get(0)).ident;
            assertEquals("j", String.valueOf(v13.getText()));
            List<ProcDec> v14 = ((Block) v10).procedureDecs;
            assertEquals(0, v14.size());
            Statement v15 = ((Block) v10).statement;
            assertThat("", v15, instanceOf(StatementBlock.class));
            List<Statement> v16 = ((StatementBlock) v15).statements;
            assertThat("", v16.get(0), instanceOf(StatementInput.class));
            Ident v17 = ((StatementInput) v16.get(0)).ident;
            assertEquals("x", String.valueOf(v17.getText()));
            assertThat("", v16.get(1), instanceOf(StatementIf.class));
            Expression v18 = ((StatementIf) v16.get(1)).expression;
            assertThat("", v18, instanceOf(ExpressionBinary.class));
            Expression v19 = ((ExpressionBinary) v18).e0;
            assertThat("", v19, instanceOf(ExpressionIdent.class));
            IToken v20 = ((ExpressionIdent) v19).firstToken;
            assertEquals("x", String.valueOf(v20.getText()));
            Expression v21 = ((ExpressionBinary) v18).e1;
            assertThat("", v21, instanceOf(ExpressionNumLit.class));
            IToken v22 = ((ExpressionNumLit) v21).firstToken;
            assertEquals("0", String.valueOf(v22.getText()));
            IToken v23 = ((ExpressionBinary) v18).op;
            assertEquals("=", String.valueOf(v23.getText()));
            Statement v24 = ((StatementIf) v16.get(1)).statement;
            assertThat("", v24, instanceOf(StatementOutput.class));
            Expression v25 = ((StatementOutput) v24).expression;
            assertThat("", v25, instanceOf(ExpressionIdent.class));
            IToken v26 = ((ExpressionIdent) v25).firstToken;
            assertEquals("y", String.valueOf(v26.getText()));
            assertThat("", v16.get(2), instanceOf(StatementWhile.class));
            Expression v27 = ((StatementWhile) v16.get(2)).expression;
            assertThat("", v27, instanceOf(ExpressionBinary.class));
            Expression v28 = ((ExpressionBinary) v27).e0;
            assertThat("", v28, instanceOf(ExpressionIdent.class));
            IToken v29 = ((ExpressionIdent) v28).firstToken;
            assertEquals("j", String.valueOf(v29.getText()));
            Expression v30 = ((ExpressionBinary) v27).e1;
            assertThat("", v30, instanceOf(ExpressionNumLit.class));
            IToken v31 = ((ExpressionNumLit) v30).firstToken;
            assertEquals("24", String.valueOf(v31.getText()));
            IToken v32 = ((ExpressionBinary) v27).op;
            assertEquals("<", String.valueOf(v32.getText()));
            Statement v33 = ((StatementWhile) v16.get(2)).statement;
            assertThat("", v33, instanceOf(StatementCall.class));
            Ident v34 = ((StatementCall) v33).ident;
            assertEquals("z", String.valueOf(v34.getText()));
            Statement v35 = ((Block) v0).statement;
            assertThat("", v35, instanceOf(StatementOutput.class));
            Expression v36 = ((StatementOutput) v35).expression;
            assertThat("", v36, instanceOf(ExpressionBinary.class));
            Expression v37 = ((ExpressionBinary) v36).e0;
            assertThat("", v37, instanceOf(ExpressionBinary.class));
            Expression v38 = ((ExpressionBinary) v37).e0;
            assertThat("", v38, instanceOf(ExpressionIdent.class));
            IToken v39 = ((ExpressionIdent) v38).firstToken;
            assertEquals("a", String.valueOf(v39.getText()));
            Expression v40 = ((ExpressionBinary) v37).e1;
            assertThat("", v40, instanceOf(ExpressionIdent.class));
            IToken v41 = ((ExpressionIdent) v40).firstToken;
            assertEquals("b", String.valueOf(v41.getText()));
            IToken v42 = ((ExpressionBinary) v37).op;
            assertEquals("+", String.valueOf(v42.getText()));
            Expression v43 = ((ExpressionBinary) v36).e1;
            assertThat("", v43, instanceOf(ExpressionBinary.class));
            Expression v44 = ((ExpressionBinary) v43).e0;
            assertThat("", v44, instanceOf(ExpressionBinary.class));
            Expression v45 = ((ExpressionBinary) v44).e0;
            assertThat("", v45, instanceOf(ExpressionBinary.class));
            Expression v46 = ((ExpressionBinary) v45).e0;
            assertThat("", v46, instanceOf(ExpressionIdent.class));
            IToken v47 = ((ExpressionIdent) v46).firstToken;
            assertEquals("c", String.valueOf(v47.getText()));
            Expression v48 = ((ExpressionBinary) v45).e1;
            assertThat("", v48, instanceOf(ExpressionIdent.class));
            IToken v49 = ((ExpressionIdent) v48).firstToken;
            assertEquals("e", String.valueOf(v49.getText()));
            IToken v50 = ((ExpressionBinary) v45).op;
            assertEquals("/", String.valueOf(v50.getText()));
            Expression v51 = ((ExpressionBinary) v44).e1;
            assertThat("", v51, instanceOf(ExpressionNumLit.class));
            IToken v52 = ((ExpressionNumLit) v51).firstToken;
            assertEquals("35", String.valueOf(v52.getText()));
            IToken v53 = ((ExpressionBinary) v44).op;
            assertEquals("*", String.valueOf(v53.getText()));
            Expression v54 = ((ExpressionBinary) v43).e1;
            assertThat("", v54, instanceOf(ExpressionBinary.class));
            Expression v55 = ((ExpressionBinary) v54).e0;
            assertThat("", v55, instanceOf(ExpressionNumLit.class));
            IToken v56 = ((ExpressionNumLit) v55).firstToken;
            assertEquals("3", String.valueOf(v56.getText()));
            Expression v57 = ((ExpressionBinary) v54).e1;
            assertThat("", v57, instanceOf(ExpressionNumLit.class));
            IToken v58 = ((ExpressionNumLit) v57).firstToken;
            assertEquals("4", String.valueOf(v58.getText()));
            IToken v59 = ((ExpressionBinary) v54).op;
            assertEquals("+", String.valueOf(v59.getText()));
            IToken v60 = ((ExpressionBinary) v43).op;
            assertEquals("/", String.valueOf(v60.getText()));
            IToken v61 = ((ExpressionBinary) v36).op;
            assertEquals("-", String.valueOf(v61.getText()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
