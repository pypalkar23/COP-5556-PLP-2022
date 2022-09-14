package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.LexerUtils.State;
import edu.ufl.cise.plpfa22.IToken.Kind;

public class PLPLexer implements ILexer {
    String input;
    int line;
    int column;
    int cursor;
    int tokenStartPos;
    int errorLoc;
    boolean isError;
    String lastErrorMsg;
    PLPToken currToken;
    State currState;
    StringBuffer sb;


    public PLPLexer(String input) {
        this.input = input;
        this.line = 1;
        this.column = 1;
        this.cursor = 0;
        this.currState = State.START;
        this.isError = false;
        sb = new StringBuffer();
    }

    @Override
    public IToken<IToken.SourceLocation> next() throws LexicalException {
        if (isError) {
            throw new LexicalException(lastErrorMsg, line, errorLoc);
        }
        try {
            scanForToken();
            return currToken;
        } catch (LexicalException e) {
            isError = true;
            throw e;
        }
    }

    @Override
    public IToken peek() throws LexicalException {
        if (isError) {
            throw new LexicalException(lastErrorMsg, line, errorLoc);
        }
        return this.currToken;

    }

    private void scanForToken() throws LexicalException {
        if (input == null) {
            this.createEOFToken();
            return;
        }

        while (cursor < input.length()) {
            char currChar = input.charAt(cursor);
            switch (currState) {
                case START -> {
                    switch (String.valueOf(currChar)) {
                        case LexerUtils.DOT, LexerUtils.COMMA, LexerUtils.SEMI, LexerUtils.LPAREN, LexerUtils.RPAREN, LexerUtils.PLUS,
                                LexerUtils.MINUS, LexerUtils.TIMES, LexerUtils.MOD, LexerUtils.QUESTION, LexerUtils.BANG, LexerUtils.EQ, LexerUtils.NEQ -> {
                            this.tokenStartPos = column;
                            this.createNormalToken(currChar);
                            this.incrementCounterAndCursor();
                            return;
                        }
                        case LexerUtils.DIV -> {
                            this.currState = State.COMMENT_START;
                            this.incrementCounterAndCursor();
                        }
                        case LexerUtils.LT, LexerUtils.GT -> {
                            this.currState = State.COMPARISON_DETECTED;
                            this.tokenStartPos = cursor;
                            sb.append(currChar);
                            this.incrementCounterAndCursor();
                        }
                        case LexerUtils.COLON -> {
                            this.currState = State.COLON_DETECTED;
                            this.tokenStartPos = cursor;
                            this.incrementCounterAndCursor();
                        }
                        case LexerUtils.WHITESPACE, LexerUtils.TAB -> {
                            this.incrementCounterAndCursor();
                        }
                        case LexerUtils.NEW_LINE, LexerUtils.CARRIAGE_RETURN -> {
                            this.incrementCounterAndCursor();
                            this.moveToNextLine();
                        }
                        case "0" -> {
                            this.currToken = new PLPToken(Kind.NUM_LIT, new char[]{'0'}, this.line, this.column);
                            this.incrementCounterAndCursor();
                        }
                        case "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                            this.currState = State.INT_DETECTED;
                            this.tokenStartPos = column;
                            this.sb.append(currChar);
                            this.incrementCounterAndCursor();
                        }
                        case LexerUtils.QUOTE -> {
                            this.currState = State.STRING_LIT_DOUBLE_QUOTED;
                            this.tokenStartPos = column;
                            this.sb.append(currChar);
                            this.incrementCounterAndCursor();
                        }
                        default -> {
                            if (Character.isJavaIdentifierStart(currChar)) {
                                this.currState = State.IDENTIFIER;
                                this.tokenStartPos = column;
                                this.sb.append(currChar);
                                this.incrementCounterAndCursor();
                            } else {
                                this.throwInvalidCharErrorMsg();
                                return;
                            }
                        }
                    }
                }
                case INT_DETECTED -> {
                    if (Character.isDigit(currChar)) {
                        this.sb.append(currChar);
                        this.incrementCounterAndCursor();
                    } else {
                        try {
                            this.createNumLitToken();
                            this.resetState();
                            return;
                        } catch (NumberFormatException e) {
                            this.lastErrorMsg = LexerUtils.ERROR_NUM_TOO_BIG;
                            this.errorLoc = tokenStartPos;
                            throw new LexicalException(lastErrorMsg, line, errorLoc);
                        }
                    }
                }
                case IDENTIFIER -> {
                    if (Character.isLetter(currChar) || Character.isDigit(currChar) || Character.isJavaIdentifierPart(currChar)) {
                        this.sb.append(currChar);
                        this.incrementCounterAndCursor();
                    } else {
                        this.createIdentifierToken();
                        this.resetState();
                        return;
                    }
                }
                case STRING_LIT_DOUBLE_QUOTED -> {
                    this.handleString('\"');
                    return;
                }
                case COMPARISON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        this.sb.append(currChar);
                        this.incrementCounterAndCursor();
                    }
                    this.createComparisonToken();
                    this.resetState();
                    return;
                }
                case COLON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        this.currToken = new PLPToken(Kind.ASSIGN, res.toCharArray(), line, tokenStartPos);
                        this.resetState();
                        return;
                    } else {
                        throwInvalidCharErrorMsg();
                    }
                }
                case COMMENT_START -> {
                    this.resetState();
                    if (currChar == '/') {
                        while (this.cursor < input.length()) {
                            char tempchar = input.charAt(cursor);
                            if (tempchar == '\n') {
                                this.resetState();
                                break;
                            }
                            incrementCounterAndCursor();
                        }
                    } else {
                        this.createNormalToken(input.charAt(cursor - 1));
                        this.resetState();
                        return;
                    }
                }
            }
        }

        //Input has ended but last token is yet to be returned
        if (currState != State.START) {
            State tempState = currState;
            resetState();
            switch (tempState) {
                case IDENTIFIER -> {
                    this.createIdentifierToken();
                    return;
                }
                case INT_DETECTED -> {
                    this.createNumLitToken();
                    return;
                }
                case COLON_DETECTED -> {
                    throwInvalidCharErrorMsg();
                }
                case COMMENT_START -> {
                    this.createNormalToken(input.charAt(cursor - 1));
                    //currToken = new PLPToken(Kind.DIV, new char[]{'/'}, line, tokenStartPos);
                    return;
                }
            }
        }
        createEOFToken();
    }

    private void createNormalToken(char currchar) {
        this.currToken = new PLPToken(LexerUtils.KIND_MAP.get(String.valueOf(currchar)), new char[]{currchar}, line, tokenStartPos);
    }

    private void createIdentifierToken() {
        String res = sb.toString();
        Kind kind = LexerUtils.KIND_MAP.getOrDefault(res, Kind.IDENT);
        this.currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    private void createNumLitToken() throws NumberFormatException {
        String res = sb.toString();
        try {
            int num = Integer.parseInt(res);
            this.currToken = new PLPToken(Kind.NUM_LIT, res.toCharArray(), line, tokenStartPos);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    public void createComparisonToken() {
        String res = sb.toString();
        Kind kind = LexerUtils.KIND_MAP.get(res);
        this.currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    public void resetState() {
        this.sb.setLength(0);
        this.currState = State.START;
    }

    private void incrementCounterAndCursor() {
        this.cursor++;
        this.column++;
    }

    private void throwInvalidCharErrorMsg() throws LexicalException {
        this.errorLoc = tokenStartPos;
        this.lastErrorMsg = LexerUtils.ERROR_INVALID_CHAR_DETECTED;
        throw new LexicalException(lastErrorMsg, line, tokenStartPos);
    }

    private void createEOFToken() {
        this.currToken = new PLPToken(Kind.EOF, new char['0'], line, column);
    }

    private void moveToNextLine(){
        this.column = 1;
        this.line++;
    }

    private void handleString(char endchar) throws LexicalException {
        boolean isStringComplete = false;
        while (cursor < input.length()) {
            char tempChar = input.charAt(cursor);
            this.sb.append(tempChar);
            this.incrementCounterAndCursor();
            if (tempChar == endchar) {
                isStringComplete = true;
                break;
            }
        }
        if (isStringComplete) {
            this.currToken = new PLPToken(Kind.STRING_LIT, sb.toString().toCharArray(), line, tokenStartPos);
            this.resetState();
        } else {
            this.lastErrorMsg = LexerUtils.ERROR_REACHED_END_OF_FILE;
            throw new LexicalException(this.lastErrorMsg, line, tokenStartPos);
        }

    }

}
