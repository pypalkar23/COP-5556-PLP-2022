package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
public class Driver {
    public static void main(String[] args) {
        String input = """
                VAR x,y,z;
                BEGIN
                    z := "hello";
                    ! z;
                    z := x;
                    ! z
                END
                .
                """;
        try {
            IParser parser = CompilerComponentFactory.getParser(CompilerComponentFactory.getLexer(input));
            ASTNode ast = parser.parse();
            ASTVisitor scopes = CompilerComponentFactory.getScopeVisitor();
            ast.visit(scopes, null);
            ASTVisitor types = CompilerComponentFactory.getTypeInferenceVisitor();
            //ast.visit(types, null);
            System.out.println(PrettyPrintVisitor.AST2String(ast));
            /*assertThat("", ast, instanceOf(Program.class));
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
            Block v5 = ((ProcDec) v3.get(0)).block;
            assertThat("", v5, instanceOf(Block.class));
            List<ConstDec> v6 = ((Block) v5).constDecs;
            assertEquals(0, v6.size());
            List<VarDec> v7 = ((Block) v5).varDecs;
            assertEquals(0, v7.size());
            List<ProcDec> v8 = ((Block) v5).procedureDecs;
            assertEquals(1, v8.size());
            assertThat("", v8.get(0), instanceOf(ProcDec.class));
            IToken v9 = ((ProcDec) v8.get(0)).ident;
            assertEquals("q", String.valueOf(v9.getText()));
            Block v10 = ((ProcDec) v8.get(0)).block;
            assertThat("", v10, instanceOf(Block.class));
            List<ConstDec> v11 = ((Block) v10).constDecs;
            assertEquals(0, v11.size());
            List<VarDec> v12 = ((Block) v10).varDecs;
            assertEquals(0, v12.size());
            List<ProcDec> v13 = ((Block) v10).procedureDecs;
            assertEquals(0, v13.size());
            Statement v14 = ((Block) v10).statement;
            assertThat("", v14, instanceOf(StatementEmpty.class));
            Statement v15 = ((Block) v5).statement;
            assertThat("", v15, instanceOf(StatementEmpty.class));
            assertThat("", v3.get(1), instanceOf(ProcDec.class));
            IToken v16 = ((ProcDec) v3.get(1)).ident;
            assertEquals("q", String.valueOf(v16.getText()));
            Block v17 = ((ProcDec) v3.get(1)).block;
            assertThat("", v17, instanceOf(Block.class));
            List<ConstDec> v18 = ((Block) v17).constDecs;
            assertEquals(0, v18.size());
            List<VarDec> v19 = ((Block) v17).varDecs;
            assertEquals(0, v19.size());
            List<ProcDec> v20 = ((Block) v17).procedureDecs;
            assertEquals(1, v20.size());
            assertThat("", v20.get(0), instanceOf(ProcDec.class));
            IToken v21 = ((ProcDec) v20.get(0)).ident;
            assertEquals("p", String.valueOf(v21.getText()));
            Block v22 = ((ProcDec) v20.get(0)).block;
            assertThat("", v22, instanceOf(Block.class));
            List<ConstDec> v23 = ((Block) v22).constDecs;
            assertEquals(0, v23.size());
            List<VarDec> v24 = ((Block) v22).varDecs;
            assertEquals(0, v24.size());
            List<ProcDec> v25 = ((Block) v22).procedureDecs;
            assertEquals(0, v25.size());
            Statement v26 = ((Block) v22).statement;
            assertThat("", v26, instanceOf(StatementEmpty.class));
            Statement v27 = ((Block) v17).statement;
            assertThat("", v27, instanceOf(StatementEmpty.class));
            Statement v28 = ((Block) v0).statement;
            assertThat("", v28, instanceOf(StatementEmpty.class));*/
            System.out.println("Mandar");

        } catch (Exception e) {
            System.out.println("In exception "+e.getMessage());
            e.printStackTrace();
        }


    }
}
