package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.Utils.State;
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
        while (cursor < input.length()) {
            char currChar = input.charAt(cursor);
            switch (currState) {
                case START -> {
                    switch (String.valueOf(currChar)) {
                        case Utils.DOT, Utils.COMMA, Utils.SEMI, Utils.LPAREN, Utils.RPAREN, Utils.PLUS,
                                Utils.MINUS, Utils.TIMES, Utils.MOD, Utils.QUESTION, Utils.BANG, Utils.EQ, Utils.NEQ -> {
                            createNormalToken(currChar);
                            incrementCounterAndCursor();
                            return;
                        }
                        case Utils.DIV -> {
                            currState = State.COMMENT_START;
                            incrementCounterAndCursor();
                        }
                        case Utils.LT, Utils.GT -> {
                            currState = State.COMPARISON_DETECTED;
                            tokenStartPos = cursor;
                            sb.append(currChar);
                            incrementCounterAndCursor();
                        }
                        case Utils.COLON -> {
                            currState = State.COLON_DETECTED;
                            tokenStartPos = cursor;
                            incrementCounterAndCursor();
                        }
                        case Utils.WHITESPACE, Utils.TAB -> {
                            incrementCounterAndCursor();
                        }
                        case Utils.NEW_LINE, Utils.CARRIAGE_RETURN -> {
                            incrementCounterAndCursor();
                            column = 1;
                            line++;
                        }
                        case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                            currState = State.INT_DETECTED;
                            tokenStartPos = column;
                            sb.append(currChar);
                            incrementCounterAndCursor();
                        }
                        default -> {
                            if (Character.isJavaIdentifierStart(currChar)) {
                                currState = State.IDENTIFIER;
                                tokenStartPos = column;
                                sb.append(currChar);
                                incrementCounterAndCursor();
                            }else{
                                throwInvalidCharErrorMsg();
                                return;
                            }
                        }
                    }
                }
                case INT_DETECTED -> {
                    if (Character.isDigit(currChar)) {
                        sb.append(currChar);
                        incrementCounterAndCursor();
                    } else {
                        try {
                            createNumLitToken();
                            resetState();
                            return;
                        } catch (NumberFormatException e) {
                            lastErrorMsg = Utils.ERROR_NUM_TOO_BIG;
                            errorLoc = tokenStartPos;
                            throw new LexicalException(lastErrorMsg, line, errorLoc);
                        }
                    }
                }
                case IDENTIFIER -> {
                    if (Character.isLetter(currChar) || Character.isDigit(currChar) || Character.isJavaIdentifierPart(currChar)) {
                        sb.append(currChar);
                        incrementCounterAndCursor();
                    } else {
                        createIdentifierToken();
                        resetState();
                        return;
                    }
                }
                case STRING_LIT_DOUBLE_QUOTED -> {

                }
                case STRING_LIT_SINGLE_QUOTED -> {

                }
                case COMPARISON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        sb.append(currChar);
                        incrementCounterAndCursor();
                    }
                    createComparisonToken();
                    resetState();
                    return;
                }
                case COLON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        currToken = new PLPToken(Kind.ASSIGN, res.toCharArray(), line, tokenStartPos);
                        resetState();
                        return;
                    } else {
                        throwInvalidCharErrorMsg();
                    }
                }
                case COMMENT_START -> {
                    resetState();
                    if (currChar == '/') {
                        while (cursor < input.length() && currChar != '\n') {
                            currChar = input.charAt(cursor);
                            incrementCounterAndCursor();
                        }
                    } else {
                        currToken = new PLPToken(Kind.DIV, new char[]{'/'}, line, tokenStartPos);
                        resetState();
                        return;
                    }
                }
            }
        }

        //Leftover conditions
        if (currState != State.START) {
            State tempState = currState;
            resetState();
            switch (tempState) {
                case IDENTIFIER -> {
                    createIdentifierToken();
                    return;
                }
                case INT_DETECTED -> {
                    createNumLitToken();
                    return;
                }
                case COLON_DETECTED -> {
                    throwInvalidCharErrorMsg();
                }
                case COMMENT_START -> {
                    currToken = new PLPToken(Kind.DIV, new char[]{'/'}, line, tokenStartPos);
                    return;
                }
            }
        }
        currToken = new PLPToken(Kind.EOF, new char['0'], line, column);
    }

    private void createNormalToken(char currchar) {
        currToken = new PLPToken(Utils.KIND_MAP.get(String.valueOf(currchar)), new char[]{currchar}, line, column);
    }

    private void createIdentifierToken() {
        String res = sb.toString();
        Kind kind = Utils.KIND_MAP.getOrDefault(res, Kind.IDENT);
        currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    private void createNumLitToken() throws NumberFormatException {
        String res = sb.toString();
        try {
            int num = Integer.parseInt(res);
            currToken = new PLPToken(Kind.NUM_LIT, res.toCharArray(), line, tokenStartPos);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    public void createComparisonToken() {
        String res = sb.toString();
        Kind kind = Utils.KIND_MAP.get(res);
        currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    public void resetState() {
        sb.setLength(0);
        this.currState = State.START;
    }

    private void incrementCounterAndCursor() {
        this.cursor++;
        this.column++;
    }

    private void throwInvalidCharErrorMsg() throws LexicalException {
        errorLoc = tokenStartPos;
        lastErrorMsg = Utils.ERROR_INVALID_CHAR_DETECTED;
        throw new LexicalException(lastErrorMsg, line, tokenStartPos);
    }
}
