package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
public class Driver {
    public static void main(String[] args) {
        String input = """
				PROCEDURE p;
					CALL q;
				PROCEDURE q;
				    CALL p;
				.
				""";

        try {
            IParser parser = CompilerComponentFactory.getParser(CompilerComponentFactory.getLexer(input));
            ASTNode ast = parser.parse();
            ASTVisitor scopes = CompilerComponentFactory.getScopeVisitor();
            ast.visit(scopes, null);
            assertThat("", ast, instanceOf(Program.class));
            Block v0 = ((Program) ast).block;
            assertThat("", v0, instanceOf(Block.class));
            List<ConstDec> v1 = ((Block) v0).constDecs;
            assertEquals(0, v1.size());
            List<VarDec> v2 = ((Block) v0).varDecs;
            assertEquals(0, v2.size());
            List<ProcDec> v3 = ((Block) v0).procedureDecs;
            assertEquals(2, v3.size());
            assertThat("", v3.get(0), instanceOf(ProcDec.class));
            IToken v4 = ((ProcDec) v3.get(0)).ident;
            assertEquals("p", String.valueOf(v4.getText()));
            int v5 = ((ProcDec) v3.get(0)).getNest();
            assertEquals(0, v5);
            Block v6 = ((ProcDec) v3.get(0)).block;
            assertThat("", v6, instanceOf(Block.class));
            List<ConstDec> v7 = ((Block) v6).constDecs;
            assertEquals(0, v7.size());
            List<VarDec> v8 = ((Block) v6).varDecs;
            assertEquals(0, v8.size());
            List<ProcDec> v9 = ((Block) v6).procedureDecs;
            assertEquals(0, v9.size());
            Statement v10 = ((Block) v6).statement;
            assertThat("", v10, instanceOf(StatementCall.class));
            Ident v11 = ((StatementCall) v10).ident;
            assertThat("", v11, instanceOf(Ident.class));
            IToken v12 = ((Ident) v11).firstToken;
            assertEquals("q", String.valueOf(v12.getText()));
            int v13 = ((Ident) v11).getNest();
            assertEquals(1, v13);
            Declaration v14 = ((Ident) v11).getDec();
            assertThat("", v14, instanceOf(ProcDec.class));
            IToken v15 = ((ProcDec) v14).ident;
            assertEquals("q", String.valueOf(v15.getText()));
            int v16 = ((ProcDec) v14).getNest();
            assertEquals(0, v16);
            assertThat("", v3.get(1), instanceOf(ProcDec.class));
            IToken v17 = ((ProcDec) v3.get(1)).ident;
            assertEquals("q", String.valueOf(v17.getText()));
            int v18 = ((ProcDec) v3.get(1)).getNest();
            assertEquals(0, v18);
            Block v19 = ((ProcDec) v3.get(1)).block;
            assertThat("", v19, instanceOf(Block.class));
            List<ConstDec> v20 = ((Block) v19).constDecs;
            assertEquals(0, v20.size());
            List<VarDec> v21 = ((Block) v19).varDecs;
            assertEquals(0, v21.size());
            List<ProcDec> v22 = ((Block) v19).procedureDecs;
            assertEquals(0, v22.size());
            Statement v23 = ((Block) v19).statement;
            assertThat("", v23, instanceOf(StatementCall.class));
            Ident v24 = ((StatementCall) v23).ident;
            assertThat("", v24, instanceOf(Ident.class));
            IToken v25 = ((Ident) v24).firstToken;
            assertEquals("p", String.valueOf(v25.getText()));
            int v26 = ((Ident) v24).getNest();
            assertEquals(1, v26);
            Declaration v27 = ((Ident) v24).getDec();
            assertThat("", v27, instanceOf(ProcDec.class));
            IToken v28 = ((ProcDec) v27).ident;
            assertEquals("p", String.valueOf(v28.getText()));
            int v29 = ((ProcDec) v27).getNest();
            assertEquals(0, v29);
            Statement v30 = ((Block) v0).statement;
            assertThat("", v30, instanceOf(StatementEmpty.class));
        } catch (Exception e) {
            System.out.println("In exception "+e.getMessage());
            e.printStackTrace();
        }


    }
}
