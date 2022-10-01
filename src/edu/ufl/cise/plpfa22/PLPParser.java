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
        consume();

        return parseProgram();
    }

    private Program parseProgram() throws PLPException {
        return new Program(null, parseBlock());
    }


    private Block parseBlock() throws PLPException{
        List<ConstDec> constDeclist = new ArrayList<>();
        List<VarDec> varDecList = new ArrayList<>();
        List<ProcDec> procDecList = new ArrayList<>();
        Statement statement = null;
        while (this.token.getKind() != Kind.EOF) {
            switch (this.token.getKind()) {
                case KW_VAR -> {
                    varDecList = parseVariableDec();
                }
                case KW_CONST -> {
                    constDeclist = parseConstantDec();
                }
                case KW_PROCEDURE -> {

                }
                default -> {
                    parseStatement();
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
        consume();
        while (this.token.getKind() == Kind.EOF) {
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
            if (this.token.getKind() != Kind.STRING_LIT || this.token.getKind() != Kind.NUM_LIT
                    || this.token.getKind() != Kind.BOOLEAN_LIT) {
                throw new SyntaxException
                        (String.format(ParserUtils.WRONG_CHARACTER_DETECTED, LexerUtils.EQ),
                                token.getSourceLocation().line(),
                                token.getSourceLocation().column());
            }
            constDecs.add(new ConstDec(ident, this.token, ParserUtils.getConstVal(this.token)));
            consume();
            if(isCommaToken())
                consume();
            if(isSemiColonToken())
                break;
        }

        return constDecs;
    }

    private List<VarDec> parseVariableDec() throws PLPException {
        List<VarDec> varDecs = new ArrayList<>();
        consume();
        while(this.token.getKind()!=Kind.EOF){
            if (!isIdentToken()) {
                throw new SyntaxException("SYNTAX ERROR", token.getSourceLocation().line(), token.getSourceLocation().column());
            }
            varDecs.add(new VarDec(null,this.token));
            consume();
            if(isCommaToken())
                consume();
            if(isSemiColonToken())
                break;
        }
        return varDecs;
    }

    private List<ProcDec> parseProcDecList() {
        return null;
    }

    private ProcDec parseProcDec() {
        return null;
    }

    //Parse Statement
    private Statement parseStatement() {
        return null;
    }

    private Statement parseStatementAssign() {
        return null;
    }

    private Statement parseStatementInput() {
        return null;
    }

    private Statement parseStatementOutput() {
        return null;
    }

    private Statement parseStatementBlock() {
        return null;
    }

    private Statement parseStatementIf() {
        return null;
    }

    private Statement parseStatementWhile() {
        return null;
    }


    private Expression parseExpression() throws PLPException {
        Expression exp1 = parseAdditiveExpression();
        consume();
        if (token.getKind() == Kind.LT || token.getKind() == Kind.GT || token.getKind() == Kind.EQ ||
                token.getKind() == Kind.NEQ || token.getKind() == Kind.GE || token.getKind() == Kind.LE) {
            IToken op = token;
            consume();
            Expression exp2 = parseAdditiveExpression();
            return new ExpressionBinary(null, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parseAdditiveExpression() throws PLPException {
        Expression exp1 = parseMultiplicativeExpression();
        consume();
        if (token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS) {
            IToken op = token;
            consume();
            Expression exp2 = parseMultiplicativeExpression();
            return new ExpressionBinary(null, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parseMultiplicativeExpression() throws PLPException {
        Expression exp1 = parsePrimaryExpression();
        consume();
        if (token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV || token.getKind() == Kind.MOD) {
            IToken op = token;
            consume();
            Expression exp2 = parsePrimaryExpression();
            return new ExpressionBinary(null, exp1, op, exp2);
        }

        return exp1;
    }

    private Expression parsePrimaryExpression() throws PLPException {
        switch (this.token.getKind()) {
            case IDENT -> {
                return new ExpressionIdent(this.token);
            }
            case BOOLEAN_LIT -> {
                return new ExpressionBooleanLit(this.token);
            }
            case STRING_LIT -> {
                return new ExpressionStringLit(this.token);
            }
            case NUM_LIT -> {
                return new ExpressionNumLit(this.token);
            }
            case LPAREN -> {
                Expression exp = parseExpression();
                consume();
                if (this.token.getKind() != Kind.RPAREN) {
                    throw new SyntaxException(String.format(ParserUtils.INVALID_CHARACTER_FOUND, LexerUtils.RPAREN));
                }
                consume();
                return exp;
            }
            default -> {
                throw new SyntaxException(String.format(ParserUtils.INVALID_CHARACTER_FOUND, null));
            }
        }
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

    public boolean isIdentToken(){
        return this.token.getKind() == Kind.IDENT;
    }


}
