package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
public class Driver {
    public static void main(String[] args) {
        String input = """
						CONST a=1, b=TRUE;
						CONST c="a";
						VAR d;
						PROCEDURE p1;
							CONST d="n", e=5;
							VAR b;
							PROCEDURE p2;
								a := b+c/(a-4)/e+d
							;
							a := b+c/(a-4)/d
						;
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
            assertEquals(3, v1.size());
            ConstDec v2 = v1.get(0);
            assertThat("", v2, instanceOf(ConstDec.class));
            IToken v3 = ((ConstDec)v2).ident;
            assertEquals("a", String.valueOf(v3.getText()));
            Integer v4 = (Integer)((ConstDec)v2).val;
            assertEquals(1, v4);
            assertEquals(0, v2.getNest());
            ConstDec v6 = v1.get(1);
            assertThat("", v6, instanceOf(ConstDec.class));
            IToken v7 = ((ConstDec)v6).ident;
            assertEquals("b", String.valueOf(v7.getText()));
            Boolean v8 = (Boolean)((ConstDec)v6).val;
            assertEquals(true, v8);
            assertEquals(0, v6.getNest());
            ConstDec v10 = v1.get(2);
            assertThat("", v10, instanceOf(ConstDec.class));
            IToken v11 = ((ConstDec)v10).ident;
            assertEquals("c", String.valueOf(v11.getText()));
            String v12 = (String)((ConstDec)v10).val;
            assertEquals("a", v12);
            assertEquals(0, v10.getNest());
            List<VarDec> v14 = ((Block) v0).varDecs;
            assertEquals(1, v14.size());
            VarDec v15 = v14.get(0);
            assertThat("", v15, instanceOf(VarDec.class));
            IToken v16 = ((VarDec)v15).ident;
            assertEquals("d", String.valueOf(v16.getText()));
            assertEquals(0, v15.getNest());
            List<ProcDec> v17 = ((Block) v0).procedureDecs;
            assertEquals(1, v17.size());
            ProcDec v18 = v17.get(0);
            assertThat("", v18, instanceOf(ProcDec.class));
            IToken v19 = v18.ident;
            assertEquals("p1", String.valueOf(v19.getText()));
            assertEquals(0, v18.getNest());
            Block v20 = v18.block;
            assertThat("", v20, instanceOf(Block.class));
            List<ConstDec> v21 = ((Block) v20).constDecs;
            assertEquals(2, v21.size());
            ConstDec v22 = v21.get(0);
            assertThat("", v22, instanceOf(ConstDec.class));
            IToken v23 = ((ConstDec)v22).ident;
            assertEquals("d", String.valueOf(v23.getText()));
            String v24 = (String)((ConstDec)v22).val;
            assertEquals("n", v24);
            assertEquals(1, v22.getNest());
            ConstDec v26 = v21.get(1);
            assertThat("", v26, instanceOf(ConstDec.class));
            IToken v27 = ((ConstDec)v26).ident;
            assertEquals("e", String.valueOf(v27.getText()));
            Integer v28 = (Integer)((ConstDec)v26).val;
            assertEquals(5, v28);
            assertEquals(1, v26.getNest());
            List<VarDec> v30 = ((Block) v20).varDecs;
            assertEquals(1, v30.size());
            VarDec v31 = v30.get(0);
            assertThat("", v31, instanceOf(VarDec.class));
            IToken v32 = ((VarDec)v31).ident;
            assertEquals("b", String.valueOf(v32.getText()));
            assertEquals(1, v31.getNest());
            List<ProcDec> v33 = ((Block) v20).procedureDecs;
            assertEquals(1, v33.size());
            ProcDec v34 = v33.get(0);
            assertThat("", v34, instanceOf(ProcDec.class));
            IToken v35 = v34.ident;
            assertEquals("p2", String.valueOf(v35.getText()));
            assertEquals(1, v34.getNest());
            Block v36 = v34.block;
            assertThat("", v36, instanceOf(Block.class));
            List<ConstDec> v37 = ((Block) v36).constDecs;
            assertEquals(0, v37.size());
            List<VarDec> v38 = ((Block) v36).varDecs;
            assertEquals(0, v38.size());
            List<ProcDec> v39 = ((Block) v36).procedureDecs;
            assertEquals(0, v39.size());
            Statement v40 = ((Block) v36).statement;
            assertThat("", v40, instanceOf(StatementAssign.class));
            Ident v41 = ((StatementAssign) v40).ident;
            assertThat("", v41, instanceOf(Ident.class));
            assertEquals("a", String.valueOf(v41.firstToken.getText()));
            assertEquals(2, ((Ident) v41).getNest());
            Declaration v42 = ((Ident) v41).getDec();
            IToken v43 = ((ConstDec)v42).ident;
            assertEquals("a", String.valueOf(v43.getText()));
            Integer v44 = (Integer)((ConstDec)v42).val;
            assertEquals(1, v44);
            assertEquals(0, v42.getNest());
            Expression v46 = ((StatementAssign) v40).expression;
            Expression v47 = ((ExpressionBinary) v46).e0;
            Expression v48 = ((ExpressionBinary) v47).e0;
            assertThat("", v48, instanceOf(ExpressionIdent.class));
            assertEquals("b", String.valueOf(v48.firstToken.getText()));
            assertEquals(2, ((ExpressionIdent) v48).getNest());
            Declaration v49 = ((ExpressionIdent) v48).getDec();
            IToken v50 = ((VarDec)v49).ident;
            assertEquals("b", String.valueOf(v50.getText()));
            assertEquals(1, v49.getNest());
            assertEquals("+", String.valueOf(((ExpressionBinary) v47).op.getText()));
            Expression v51 = ((ExpressionBinary) v47).e1;
            Expression v52 = ((ExpressionBinary) v51).e0;
            Expression v53 = ((ExpressionBinary) v52).e0;
            assertThat("", v53, instanceOf(ExpressionIdent.class));
            assertEquals("c", String.valueOf(v53.firstToken.getText()));
            assertEquals(2, ((ExpressionIdent) v53).getNest());
            Declaration v54 = ((ExpressionIdent) v53).getDec();
            IToken v55 = ((ConstDec)v54).ident;
            assertEquals("c", String.valueOf(v55.getText()));
            String v56 = (String)((ConstDec)v54).val;
            assertEquals("a", v56);
            assertEquals(0, v54.getNest());
            assertEquals("/", String.valueOf(((ExpressionBinary) v52).op.getText()));
            Expression v58 = ((ExpressionBinary) v52).e1;
            Expression v59 = ((ExpressionBinary) v58).e0;
            assertThat("", v59, instanceOf(ExpressionIdent.class));
            assertEquals("a", String.valueOf(v59.firstToken.getText()));
            assertEquals(2, ((ExpressionIdent) v59).getNest());
            Declaration v60 = ((ExpressionIdent) v59).getDec();
            IToken v61 = ((ConstDec)v60).ident;
            assertEquals("a", String.valueOf(v61.getText()));
            Integer v62 = (Integer)((ConstDec)v60).val;
            assertEquals(1, v62);
            assertEquals(0, v60.getNest());
            assertEquals("-", String.valueOf(((ExpressionBinary) v58).op.getText()));
            Expression v64 = ((ExpressionBinary) v58).e1;
            assertThat("", v64, instanceOf(ExpressionNumLit.class));
            assertEquals("4", String.valueOf(v64.firstToken.getText()));
            assertEquals("/", String.valueOf(((ExpressionBinary) v51).op.getText()));
            Expression v65 = ((ExpressionBinary) v51).e1;
            assertThat("", v65, instanceOf(ExpressionIdent.class));
            assertEquals("e", String.valueOf(v65.firstToken.getText()));
            assertEquals(2, ((ExpressionIdent) v65).getNest());
            Declaration v66 = ((ExpressionIdent) v65).getDec();
            IToken v67 = ((ConstDec)v66).ident;
            assertEquals("e", String.valueOf(v67.getText()));
            Integer v68 = (Integer)((ConstDec)v66).val;
            assertEquals(5, v68);
            assertEquals(1, v66.getNest());
            assertEquals("+", String.valueOf(((ExpressionBinary) v46).op.getText()));
            Expression v70 = ((ExpressionBinary) v46).e1;
            assertThat("", v70, instanceOf(ExpressionIdent.class));
            assertEquals("d", String.valueOf(v70.firstToken.getText()));
            assertEquals(2, ((ExpressionIdent) v70).getNest());
            Declaration v71 = ((ExpressionIdent) v70).getDec();
            IToken v72 = ((ConstDec)v71).ident;
            assertEquals("d", String.valueOf(v72.getText()));
            String v73 = (String)((ConstDec)v71).val;
            assertEquals("n", v73);
            assertEquals(1, v71.getNest());
            Statement v75 = ((Block) v20).statement;
            assertThat("", v75, instanceOf(StatementAssign.class));
            Ident v76 = ((StatementAssign) v75).ident;
            assertThat("", v76, instanceOf(Ident.class));
            assertEquals("a", String.valueOf(v76.firstToken.getText()));
            assertEquals(1, ((Ident) v76).getNest());
            Declaration v77 = ((Ident) v76).getDec();
            IToken v78 = ((ConstDec)v77).ident;
            assertEquals("a", String.valueOf(v78.getText()));
            Integer v79 = (Integer)((ConstDec)v77).val;
            assertEquals(1, v79);
            assertEquals(0, v77.getNest());
            Expression v81 = ((StatementAssign) v75).expression;
            Expression v82 = ((ExpressionBinary) v81).e0;
            assertThat("", v82, instanceOf(ExpressionIdent.class));
            assertEquals("b", String.valueOf(v82.firstToken.getText()));
            assertEquals(1, ((ExpressionIdent) v82).getNest());
            Declaration v83 = ((ExpressionIdent) v82).getDec();
            IToken v84 = ((VarDec)v83).ident;
            assertEquals("b", String.valueOf(v84.getText()));
            assertEquals(1, v83.getNest());
            assertEquals("+", String.valueOf(((ExpressionBinary) v81).op.getText()));
            Expression v85 = ((ExpressionBinary) v81).e1;
            Expression v86 = ((ExpressionBinary) v85).e0;
            Expression v87 = ((ExpressionBinary) v86).e0;
            assertThat("", v87, instanceOf(ExpressionIdent.class));
            assertEquals("c", String.valueOf(v87.firstToken.getText()));
            assertEquals(1, ((ExpressionIdent) v87).getNest());
            Declaration v88 = ((ExpressionIdent) v87).getDec();
            IToken v89 = ((ConstDec)v88).ident;
            assertEquals("c", String.valueOf(v89.getText()));
            String v90 = (String)((ConstDec)v88).val;
            assertEquals("a", v90);
            assertEquals(0, v88.getNest());
            assertEquals("/", String.valueOf(((ExpressionBinary) v86).op.getText()));
            Expression v92 = ((ExpressionBinary) v86).e1;
            Expression v93 = ((ExpressionBinary) v92).e0;
            assertThat("", v93, instanceOf(ExpressionIdent.class));
            assertEquals("a", String.valueOf(v93.firstToken.getText()));
            assertEquals(1, ((ExpressionIdent) v93).getNest());
            Declaration v94 = ((ExpressionIdent) v93).getDec();
            IToken v95 = ((ConstDec)v94).ident;
            assertEquals("a", String.valueOf(v95.getText()));
            Integer v96 = (Integer)((ConstDec)v94).val;
            assertEquals(1, v96);
            assertEquals(0, v94.getNest());
            assertEquals("-", String.valueOf(((ExpressionBinary) v92).op.getText()));
            Expression v98 = ((ExpressionBinary) v92).e1;
            assertThat("", v98, instanceOf(ExpressionNumLit.class));
            assertEquals("4", String.valueOf(v98.firstToken.getText()));
            assertEquals("/", String.valueOf(((ExpressionBinary) v85).op.getText()));
            Expression v99 = ((ExpressionBinary) v85).e1;
            assertThat("", v99, instanceOf(ExpressionIdent.class));
            assertEquals("d", String.valueOf(v99.firstToken.getText()));
            assertEquals(1, ((ExpressionIdent) v99).getNest());
            Declaration v100 = ((ExpressionIdent) v99).getDec();
            IToken v101 = ((ConstDec)v100).ident;
            assertEquals("d", String.valueOf(v101.getText()));
            String v102 = (String)((ConstDec)v100).val;
            assertEquals("n", v102);
            assertEquals(1, v100.getNest());
            Statement v104 = ((Block) v0).statement;
            assertThat("", v104, instanceOf(StatementEmpty.class));
        } catch (Exception e) {
            System.out.println("In exception "+e.getMessage());
            e.printStackTrace();
        }


    }
}
