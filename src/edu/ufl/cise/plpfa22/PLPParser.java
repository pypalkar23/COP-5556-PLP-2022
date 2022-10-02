package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class PLPParser implements IParser {
    private ILexer lexer;
    private IToken token;

    public PLPParser(ILexer lexer) {
        this.lexer = lexer;
    }

    public void consume() throws LexicalException {
        this.token = this.lexer.next();
    }

    @Override
    public ASTNode parse() throws PLPException {
        return parseProgram();
    }

    private Program parseProgram() throws PLPException {
        return new Program(null, parseBlock(false));
    }


    private Block parseBlock(boolean inProceedure) throws PLPException {
        consume();
        List<ConstDec> constDeclist = new ArrayList<>();
        List<VarDec> varDecList = new ArrayList<>();
        List<ProcDec> procDecList = new ArrayList<>();
        Statement statement = null;
        boolean reachedEndOfBlock = false;

        while (this.token.getKind() != Kind.EOF) {
            if (reachedEndOfBlock)
                break;
            switch (this.token.getKind()) {
                case KW_VAR -> {
                    varDecList = parseVariableDec();
                }
                case KW_CONST -> {
                    constDeclist = parseConstantDec();
                }
                case KW_PROCEDURE -> {
                    ProcDec procDec=parseProcDec();
                    procDecList.add(procDec);
                }
                case DOT -> {
                    consume();
                    if (this.token.getKind() != Kind.EOF) {
                        throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
                    }
                    reachedEndOfBlock = true;
                }
                case KW_END -> {
                    if(inProceedure) {
                        reachedEndOfBlock = true;
                    }
                    consume();
                    getNextIfSemi();
                }
                default -> {
                    statement = parseStatement();
                }
            }
        }

        if (statement == null) {
            statement = new StatementEmpty(null);
        }
        return new Block(null, constDeclist, varDecList, procDecList, statement);
    }

    private List<ConstDec> parseConstantDec() throws PLPException {
        List<ConstDec> constDecs = new ArrayList<>();
        Boolean isSemiColonDetected = false;
        consume();
        while (this.token.getKind() != Kind.EOF) {
            if (!isIdentToken()) {
                throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
            }
            IToken ident = this.token;
            consume();
            if (!isEqualToken()) {
                throw new SyntaxException
                        (String.format(ParserUtils.WRONG_CHARACTER_DETECTED, LexerUtils.EQ),
                                token.getSourceLocation().line(),
                                token.getSourceLocation().column());
            }
            consume();
            if (!(this.token.getKind() == Kind.STRING_LIT || this.token.getKind() == Kind.NUM_LIT
                    || this.token.getKind() == Kind.BOOLEAN_LIT)) {
                throw new SyntaxException
                        (String.format(ParserUtils.WRONG_CHARACTER_DETECTED, LexerUtils.EQ),
                                token.getSourceLocation().line(),
                                token.getSourceLocation().column());
            }
            constDecs.add(new ConstDec(null, ident, ParserUtils.getConstVal(this.token)));
            consume();
            if (isCommaToken())
                consume();
            if (isSemiColonToken()){
                isSemiColonDetected = true;
                consume();
                break;
            }
        }

        if (!isSemiColonDetected)
            throw new SyntaxException(LexerUtils.ERROR_REACHED_END_OF_FILE, token.getSourceLocation().line(), token.getSourceLocation().column());

        return constDecs;
    }

    private List<VarDec> parseVariableDec() throws PLPException {
        List<VarDec> varDecs = new ArrayList<>();
        Boolean isSemiColonDetected = false;
        consume();
        while (this.token.getKind() != Kind.EOF) {
            if (!isIdentToken()) {
                throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
            }
            varDecs.add(new VarDec(null, this.token));
            consume();
            if (isCommaToken())
                consume();
            if (isSemiColonToken()){
                isSemiColonDetected = true;
                consume();
                break;
            }
        }

        if (!isSemiColonDetected)
            throw new SyntaxException(LexerUtils.ERROR_REACHED_END_OF_FILE, token.getSourceLocation().line(), token.getSourceLocation().column());

        return varDecs;
    }

    private ProcDec parseProcDec() throws PLPException {
        consume();
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        IToken ftoken = this.token;
        consume();
        if(this.token.getKind()!=Kind.SEMI)
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        Block block = parseBlock(true);

        return new ProcDec(null, ftoken, block);
    }

    //Parse Statement
    private Statement parseStatement() throws PLPException {
        Statement stmt = null;
        switch (this.token.getKind()) {

            case QUESTION -> {
                stmt = parseStatementInput();
            }
            case KW_CALL -> {
                stmt = parseStatementCall();
            }
            case BANG -> {
                stmt = parseStatementOutput();
            }
            case KW_IF -> {
                stmt = parseStatementIf();
            }
            case KW_WHILE -> {
                stmt = parseStatementWhile();
            }
            case KW_BEGIN -> {
                stmt = parseStatementBlock();
            }
            case IDENT -> {
                stmt = parseStatementAssign();
            }
        }

        return stmt;
    }

    private Statement parseStatementAssign() throws PLPException {
        Ident ident = new Ident(this.token);
        consume();
        if (!isAssignToken())
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        consume();
        Expression exp = parseExpression();

        return new StatementAssign(null, ident, exp);
    }

    private Statement parseStatementInput() throws PLPException {
        consume();
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        Ident ident = new Ident(this.token);

        return new StatementInput(null, ident);
    }

    private Statement parseStatementOutput() throws PLPException {
        consume();
        Expression exp = parseExpression();

        return new StatementOutput(null, exp);
    }

    private Statement parseStatementBlock() throws PLPException {
        List<Statement> statements = new ArrayList<>();
        consume();
        while (this.token.getKind() != Kind.EOF) {
            if (this.token.getKind() == Kind.KW_END){
                break;
            }
            Statement statement = parseStatement();
            statements.add(statement);
            consume();
            getNextIfSemi();
        }
        return new StatementBlock(null, statements);
    }

    private Statement parseStatementIf() throws PLPException {
        consume();
        Expression exp = parseExpression();
        if (this.token.getKind() != Kind.KW_THEN) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        consume();
        Statement stmt = parseStatement();


        return new StatementIf(null, exp, stmt);
    }

    private Statement parseStatementWhile() throws PLPException {
        consume();
        Expression exp = parseExpression();
        if (this.token.getKind() != Kind.KW_DO) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        consume();
        Statement stmt = parseStatement();

        return new StatementWhile(null, exp, stmt);
    }

    private Statement parseStatementCall() throws PLPException {
        consume();
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }

        return new StatementCall(null, new Ident(this.token));
    }


    private Expression parseExpression() throws PLPException {
        Expression exp1 = parseAdditiveExpression();

        while(token.getKind() == Kind.LT || token.getKind() == Kind.GT || token.getKind() == Kind.EQ ||
                token.getKind() == Kind.NEQ || token.getKind() == Kind.GE || token.getKind() == Kind.LE) {
            IToken op = token;
            consume();
            Expression exp2 = parseAdditiveExpression();
            exp1 = new ExpressionBinary(null, exp1, op, exp2);
        }
        return exp1;
    }

    private Expression parseAdditiveExpression() throws PLPException {
        Expression exp1 = parseMultiplicativeExpression();

        while(token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS) {
            IToken op = token;
            consume();
            Expression exp2 = parseMultiplicativeExpression();
            exp1 = new ExpressionBinary(null, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parseMultiplicativeExpression() throws PLPException {
        Expression exp1 = parsePrimaryExpression();
        while (token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV || token.getKind() == Kind.MOD) {
            IToken op = token;
            consume();
            Expression exp2 = parsePrimaryExpression();
            exp1 = new ExpressionBinary(null, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parsePrimaryExpression() throws PLPException {
        Expression exp;
        switch (this.token.getKind()) {
            case IDENT -> {
                exp = new ExpressionIdent(this.token);
            }
            case BOOLEAN_LIT -> {
                exp = new ExpressionBooleanLit(this.token);
            }
            case STRING_LIT -> {
                exp = new ExpressionStringLit(this.token);
            }
            case NUM_LIT -> {
                exp = new ExpressionNumLit(this.token);
            }
            case LPAREN -> {
                consume();
                exp = parseExpression();
                if (this.token.getKind() != Kind.RPAREN) {
                    throw new SyntaxException(String.format(ParserUtils.INVALID_CHARACTER_FOUND, LexerUtils.RPAREN));
                }
            }
            default -> {
                throw new SyntaxException(String.format(ParserUtils.INVALID_CHARACTER_FOUND, null));
            }
        }
        consume();
        return exp;
    }

    public boolean isEqualToken() {
        return this.token.getKind() == Kind.EQ;
    }

    public boolean isAssignToken() {
        return this.token.getKind() == Kind.ASSIGN;
    }

    public boolean isSemiColonToken() {
        return this.token.getKind() == Kind.SEMI;
    }

    public boolean isCommaToken() {
        return this.token.getKind() == Kind.COMMA;
    }

    public boolean isIdentToken() {
        return this.token.getKind() == Kind.IDENT;
    }

    public void getNextIfSemi()throws PLPException{
        if(isSemiColonToken())
            consume();
    }

}
