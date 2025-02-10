package edu.ufl.cise.plpfa22.implementations;

import edu.ufl.cise.plpfa22.interfaces.ILexer;
import edu.ufl.cise.plpfa22.interfaces.IToken;
import edu.ufl.cise.plpfa22.interfaces.IToken.Kind;
import edu.ufl.cise.plpfa22.exceptions.LexicalException;
import edu.ufl.cise.plpfa22.utils.CodeGenUtils;
import edu.ufl.cise.plpfa22.utils.CodeGenUtils.*;



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
    CodeGenUtils.LexerUtils.State currState;
    StringBuffer sb;


    public PLPLexer(String input) {
        this.input = input;
        this.line = 1;
        this.column = 1;
        this.cursor = 0;
        this.currState = LexerUtils.State.START;
        this.isError = false;
        sb = new StringBuffer();
    }

    @Override
    public IToken next() throws LexicalException {
        if (this.isError) {
            throw new LexicalException(this.lastErrorMsg, this.line, this.errorLoc);
        }
        try {
            this.scanForToken();
            return this.currToken;
        } catch (LexicalException e) {
            this.isError = true;
            throw e;
        }
    }

    @Override
    public IToken peek() throws LexicalException {
        if (this.isError) {
            throw new LexicalException(this.lastErrorMsg, this.line, this.errorLoc);
        }
        return this.currToken;

    }

    private void scanForToken() throws LexicalException {
        if (input == null) {
            this.createEOFToken();
            return;
        }

        while (this.cursor < input.length()) {
            char currChar = input.charAt(this.cursor);
            switch (this.currState) {
                case START -> {
                    switch (String.valueOf(currChar)) {
                        case CodeGenUtils.LexerUtils.DOT, CodeGenUtils.LexerUtils.COMMA, CodeGenUtils.LexerUtils.SEMI, CodeGenUtils.LexerUtils.LPAREN, CodeGenUtils.LexerUtils.RPAREN, CodeGenUtils.LexerUtils.PLUS,
                                CodeGenUtils.LexerUtils.MINUS, CodeGenUtils.LexerUtils.TIMES, CodeGenUtils.LexerUtils.MOD, CodeGenUtils.LexerUtils.QUESTION, CodeGenUtils.LexerUtils.BANG, CodeGenUtils.LexerUtils.EQ, CodeGenUtils.LexerUtils.NEQ -> {
                            this.tokenStartPos = this.column;
                            this.createNormalToken(currChar);
                            this.incrementColumnAndCursor();
                            return;
                        }
                        case CodeGenUtils.LexerUtils.DIV -> {
                            this.currState = LexerUtils.State.COMMENT_START;
                            this.incrementColumnAndCursor();
                        }
                        case CodeGenUtils.LexerUtils.LT, CodeGenUtils.LexerUtils.GT -> {
                            this.currState = LexerUtils.State.COMPARISON_DETECTED;
                            this.tokenStartPos = this.column;
                            sb.append(currChar);
                            this.incrementColumnAndCursor();
                        }
                        case CodeGenUtils.LexerUtils.COLON -> {
                            this.currState = LexerUtils.State.COLON_DETECTED;
                            this.tokenStartPos = this.column;
                            this.incrementColumnAndCursor();
                        }
                        case CodeGenUtils.LexerUtils.WHITESPACE, CodeGenUtils.LexerUtils.TAB -> {
                            this.incrementColumnAndCursor();
                        }
                        case CodeGenUtils.LexerUtils.NEW_LINE, CodeGenUtils.LexerUtils.CARRIAGE_RETURN,"" -> {
                            this.resetState();
                            this.incrementColumnAndCursor();
                            this.moveToNextLine();
                        }
                        case "0" -> {
                            this.currToken = new PLPToken(Kind.NUM_LIT, new char[]{'0'}, this.line, this.column);
                            this.incrementColumnAndCursor();
                            return;
                        }
                        case "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                            this.currState = LexerUtils.State.INT_DETECTED;
                            this.tokenStartPos = column;
                            this.sb.append(currChar);
                            this.incrementColumnAndCursor();
                        }
                        case CodeGenUtils.LexerUtils.QUOTE -> {
                            this.currState = LexerUtils.State.STRING_LIT_DOUBLE_QUOTED;
                            this.tokenStartPos = column;
                            this.sb.append(currChar);
                            this.incrementColumnAndCursor();
                        }
                        default -> {
                            if (Character.isJavaIdentifierStart(currChar)) {
                                this.currState = LexerUtils.State.IDENTIFIER;
                                this.tokenStartPos = column;
                                this.sb.append(currChar);
                                this.incrementColumnAndCursor();
                            } else {
                                this.throwInvalidCharErrorMsg();
                                return;
                            }
                        }
                    }
                }
                case INT_DETECTED -> {
                    if (!Character.isWhitespace(currChar) && Character.isDigit(currChar)) {
                        this.sb.append(currChar);
                        this.incrementColumnAndCursor();
                    } else {
                        try {
                            this.createNumLitToken();
                            this.resetState();
                            return;
                        } catch (NumberFormatException e) {
                            this.lastErrorMsg = CodeGenUtils.LexerUtils.ERROR_NUM_TOO_BIG;
                            this.errorLoc = tokenStartPos;
                            throw new LexicalException(lastErrorMsg, line, errorLoc);
                        }
                    }
                }
                case IDENTIFIER -> {
                    if (Character.isLetter(currChar) || Character.isDigit(currChar) || Character.isJavaIdentifierPart(currChar)) {
                        this.sb.append(currChar);
                        this.incrementColumnAndCursor();
                    } else {
                        this.createIdentifierToken();
                        this.resetState();
                        return;
                    }
                }
                case STRING_LIT_DOUBLE_QUOTED -> {
                    this.handleString();
                    return;
                }
                case COMPARISON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        this.sb.append(currChar);
                        this.incrementColumnAndCursor();
                    }
                    this.createComparisonToken();
                    this.resetState();
                    return;
                }
                case COLON_DETECTED -> {
                    String res = "";
                    if (currChar == '=') {
                        this.currToken = new PLPToken(Kind.ASSIGN, res.toCharArray(), line, tokenStartPos);
                        incrementColumnAndCursor();
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
                            char tempChar = input.charAt(cursor);
                            if (tempChar == '\n') {
                                this.resetState();
                                break;
                            }
                            incrementColumnAndCursor();
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
        if (this.currState != LexerUtils.State.START) {
            LexerUtils.State tempState = currState;
            this.currState = LexerUtils.State.START;
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
                    return;
                }
            }
        }
        createEOFToken();
    }

    private void createNormalToken(char currchar) {
        this.currToken = new PLPToken(CodeGenUtils.LexerUtils.KIND_MAP.get(String.valueOf(currchar)), new char[]{currchar}, line, tokenStartPos);
    }

    private void createIdentifierToken() {
        String res = sb.toString();
        Kind kind = CodeGenUtils.LexerUtils.KIND_MAP.getOrDefault(res, Kind.IDENT);
        this.currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    private void createNumLitToken() throws NumberFormatException {
        String res = sb.toString().trim();
        try {
            int num = Integer.parseInt(res);
            this.currToken = new PLPToken(Kind.NUM_LIT, res.toCharArray(), line, tokenStartPos);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    public void createComparisonToken() {
        String res = sb.toString();
        Kind kind = CodeGenUtils.LexerUtils.KIND_MAP.get(res);
        this.currToken = new PLPToken(kind, res.toCharArray(), line, tokenStartPos);
    }

    public void resetState() {
        this.sb.setLength(0);
        this.currState = LexerUtils.State.START;
    }

    private void incrementColumnAndCursor() {
        this.cursor++;
        this.column++;
    }

    private void throwInvalidCharErrorMsg() throws LexicalException {
        this.errorLoc = tokenStartPos;
        this.lastErrorMsg = CodeGenUtils.LexerUtils.ERROR_INVALID_CHAR_DETECTED;
        throw new LexicalException(lastErrorMsg, line, tokenStartPos);
    }

    private void createEOFToken() {
        this.currToken = new PLPToken(Kind.EOF, new char['0'], line, column);
    }

    private void moveToNextLine() {
        this.column = 1;
        this.line++;
    }

    private void handleString() throws LexicalException {
        boolean isStringComplete = false;
        while (cursor < input.length()) {
            char tempChar = input.charAt(cursor);
            this.sb.append(tempChar);
            this.incrementColumnAndCursor();
            if (tempChar == '\"' && input.charAt(cursor-2)!='\\') {
                isStringComplete = true;
                break;
            }
        }
        if (isStringComplete) {
            this.currToken = new PLPToken(Kind.STRING_LIT, sb.toString().toCharArray(), line, tokenStartPos);
            this.resetState();
        } else {
            this.lastErrorMsg = CodeGenUtils.LexerUtils.ERROR_REACHED_END_OF_FILE;
            throw new LexicalException(this.lastErrorMsg, line, tokenStartPos);
        }
    }


}
