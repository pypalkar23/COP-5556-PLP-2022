package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.*;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class PLPParser implements IParser {
    private ILexer lexer;
    private IToken token;
    private boolean isDotDetected;
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
        Program prog = new Program(null, parseBlock(false));
        if(!isDotDetected)
            throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
        return prog;
    }


    private Block parseBlock(boolean inProceedure) throws PLPException {
        consume();
        List<ConstDec> constDeclist = new ArrayList<>();
        List<VarDec> varDecList = new ArrayList<>();
        List<ProcDec> procDecList = new ArrayList<>();
        Statement statement = new StatementEmpty(null);
        boolean reachedEndOfBlock = false;

        while (this.token.getKind() != Kind.EOF) {
            if (reachedEndOfBlock)
                break;
            switch (this.token.getKind()) {
                case KW_VAR -> {
                    varDecList.addAll(parseVariableDec());
                }
                case KW_CONST -> {
                    constDeclist.addAll(parseConstantDec());
                }
                case KW_PROCEDURE -> {
                    ProcDec procDec=parseProcDec();
                    procDecList.add(procDec);
                }
                case DOT -> {
                    if (inProceedure) {
                        throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
                    }
                    this.isDotDetected = true;
                    consume();
                    if (this.token.getKind() != Kind.EOF) {
                        throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
                    }
                    reachedEndOfBlock = true;
                }
                case QUESTION,BANG,KW_CALL,KW_BEGIN,SEMI,KW_WHILE,KW_IF,IDENT ->
                {
                    statement = parseStatement();
                    /*if(!isCurrentTokenDOT()){
                        consume();
                    }*/
                    if(isSemiColonToken()){
                        if(inProceedure)
                            reachedEndOfBlock = true;
                        else
                            throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
                    }
                    getNextIfSemi();
                }
                default ->{
                    throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
                }
            }
        }

        return new Block(null, constDeclist, varDecList, procDecList, statement);
    }

    private List<ConstDec> parseConstantDec() throws PLPException {
        IToken firstToken = this.token;
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
            constDecs.add(new ConstDec(firstToken, ident, ParserUtils.getConstVal(this.token)));
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
        IToken ftoken = this.token;
        List<VarDec> varDecs = new ArrayList<>();
        Boolean isSemiColonDetected = false;
        consume();
        while (this.token.getKind() != Kind.EOF) {
            if (!isIdentToken()) {
                throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
            }
            varDecs.add(new VarDec(ftoken, this.token));
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
        IToken firstToken = this.token;
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        IToken procNameToken = this.token;
        consume();
        if(this.token.getKind()!=Kind.SEMI)
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());

        Block block = parseBlock(true);

        return new ProcDec(firstToken, procNameToken, block);
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
            case SEMI ->{
                stmt = new StatementEmpty(this.token);
            }
            default ->{
                throw new SyntaxException(ParserUtils.SYNTAX_ERROR,token.getSourceLocation().line(),token.getSourceLocation().column());
            }
        }

        return stmt;
    }

    private Statement parseStatementAssign() throws PLPException {
        IToken fToken = this.token;
        Ident ident = new Ident(this.token);
        consume();
        if (!isAssignToken())
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        consume();
        Expression exp = parseExpression();
        //expression at the end no need to consume the last token it happens in the expression parsing
        return new StatementAssign(fToken, ident, exp);
    }

    private Statement parseStatementInput() throws PLPException {
        consume();
        IToken fToken = this.token;
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        Ident ident = new Ident(this.token);
        consume();

        return new StatementInput(fToken, ident);
    }

    private Statement parseStatementOutput() throws PLPException {
        consume();
        IToken fToken = this.token;

        Expression exp = parseExpression();
        //expression at the end no need to consume the last token it happens in the expression parsing
        return new StatementOutput(fToken, exp);
    }

    private Statement parseStatementBlock() throws PLPException {
        List<Statement> statements = new ArrayList<>();
        consume();
        IToken fToken = this.token;
        Boolean isEndDetected = false;
        Boolean isStatementExpected = true;
        Statement statement = null;
        while (this.token.getKind() != Kind.EOF) {
            if (this.token.getKind() == Kind.KW_END){
                isEndDetected = true;
                if(statement!=null){
                    statements.add(new StatementEmpty(this.token));
                }
                consume();
                break;
            }
            if(!isStatementExpected)
                throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
            isStatementExpected = false;
            statement = parseStatement();
            statements.add(statement);
            statement = null;
            if(isSemiColonToken()){
                isStatementExpected = true;
                statement = new StatementEmpty(this.token);
                consume();
            }
        }

        if(!isEndDetected){
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }

        return new StatementBlock(fToken, statements);
    }

    private Statement parseStatementIf() throws PLPException {
        consume();
        IToken fToken = this.token;
        Expression exp = parseExpression();
        if (this.token.getKind() != Kind.KW_THEN) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        consume();
        Statement stmt = parseStatement();

        return new StatementIf(fToken, exp, stmt);
    }

    private Statement parseStatementWhile() throws PLPException {
        consume();
        IToken fToken = this.token;
        Expression exp = parseExpression();
        if (this.token.getKind() != Kind.KW_DO) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        consume();
        Statement stmt = parseStatement();

        return new StatementWhile(fToken, exp, stmt);
    }

    private Statement parseStatementCall() throws PLPException {
        consume();
        IToken fToken = this.token;
        if (!isIdentToken()) {
            throw new SyntaxException(ParserUtils.SYNTAX_ERROR, token.getSourceLocation().line(), token.getSourceLocation().column());
        }
        consume();

        return new StatementCall(fToken, new Ident(fToken));
    }


    private Expression parseExpression() throws PLPException {
        IToken ftoken = this.token;
        Expression exp1 = parseAdditiveExpression();

        while(token.getKind() == Kind.LT || token.getKind() == Kind.GT || token.getKind() == Kind.EQ ||
                token.getKind() == Kind.NEQ || token.getKind() == Kind.GE || token.getKind() == Kind.LE) {
            IToken op = token;
            consume();
            Expression exp2 = parseAdditiveExpression();
            exp1 = new ExpressionBinary(ftoken, exp1, op, exp2);
        }
        return exp1;
    }

    private Expression parseAdditiveExpression() throws PLPException {
        IToken ftoken = this.token;
        Expression exp1 = parseMultiplicativeExpression();

        while(token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS) {
            IToken op = token;
            consume();
            Expression exp2 = parseMultiplicativeExpression();
            exp1 = new ExpressionBinary(ftoken, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parseMultiplicativeExpression() throws PLPException {
        IToken ftoken = this.token;
        Expression exp1 = parsePrimaryExpression();
        while (token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV || token.getKind() == Kind.MOD) {
            IToken op = token;
            consume();
            Expression exp2 = parsePrimaryExpression();
            exp1 = new ExpressionBinary(ftoken, exp1, op, exp2);
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
                //consume left parenthesis
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

    public boolean isCurrentTokenDOT()throws PLPException{
        if(this.token.getKind() == Kind.DOT){
            this.isDotDetected = true;
        }

        return this.isDotDetected;
    }

}
