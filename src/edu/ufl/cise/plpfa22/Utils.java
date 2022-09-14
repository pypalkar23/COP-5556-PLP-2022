package edu.ufl.cise.plpfa22;

import java.util.*;

import edu.ufl.cise.plpfa22.IToken.Kind;

public class Utils {
    public enum State{
        START,
        KEYWORD,
        IDENTIFIER,
        STRING_LIT_SINGLE_QUOTED,
        STRING_LIT_DOUBLE_QUOTED,
        INT_DETECTED,
        COLON_DETECTED,
        ESCAPE_SEQ,
        COMPARISON_DETECTED,
        WHITESPACE,
        COMMENT_START
    }

    public static final String DOT = ".";
    public static final String COMMA = ",";
    public static final String SEMI = ";";
    public static final String QUOTE = "\"";
    public static final String LPAREN = "(";
    public static final String RPAREN = ")";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String TIMES = "*";
    public static final String DIV = "/";
    public static final String MOD = "%";
    public static final String QUESTION = "?";
    public static final String BANG = "!";
    public static final String COLON = ":";
    public static final String ASSIGN = ":=";
    public static final String EQ = "=";
    public static final String NEQ = "#";
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String KW_CONST = "CONST";
    public static final String KW_VAR = "VAR";
    public static final String KW_PROCEEDURE = "PROCEEDURE";
    public static final String KW_CALL = "CALL";
    public static final String KW_BEGIN = "BEGIN";
    public static final String KW_END = "END";
    public static final String KW_IF = "IF";
    public static final String KW_WHILE = "WHILE";
    public static final String KW_DO = "DO";
    public static final String BOOL_TRUE = "TRUE";
    public static final String BOOL_FALSE = "FALSE";
    public static final String WHITESPACE = " ";
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";
    public static final String CARRIAGE_RETURN = "\r";
    public static final String FORM_FEED = "\f";
    public static final String BACKSPACE = "\b";


    public static final String ERROR_NUM_TOO_BIG = "Number to big to parse";
    public static final String ERROR_INVALID_CHAR_DETECTED = "Invalid Character Detected";


    static String[] symbolArr = {DOT, COMMA, SEMI, LPAREN, RPAREN, PLUS, MINUS, TIMES, DIV, MOD, QUESTION, BANG, ASSIGN, EQ, NEQ, LT, GT};
    static String[] keywordArr = {KW_CONST, KW_VAR, KW_PROCEEDURE, KW_CALL, KW_BEGIN, KW_END, KW_IF, KW_WHILE, KW_DO};
    static String[] boolean_lit = {BOOL_TRUE, BOOL_FALSE};

    public static Map<String, Kind> KIND_MAP = new HashMap<>() {
        {
            put(DOT, Kind.DOT);
            put(COMMA, Kind.COMMA);
            put(SEMI, Kind.SEMI);
            put(QUOTE, Kind.QUOTE);
            put(LPAREN, Kind.LPAREN);
            put(RPAREN, Kind.RPAREN);
            put(PLUS, Kind.PLUS);
            put(MINUS, Kind.MINUS);
            put(TIMES, Kind.TIMES);
            put(DIV, Kind.DIV);
            put(MOD, Kind.MOD);
            put(QUESTION, Kind.QUESTION);
            put(BANG, Kind.BANG);
            put(ASSIGN, Kind.ASSIGN);
            put(EQ, Kind.EQ);
            put(NEQ, Kind.NEQ);
            put(GT, Kind.GT);
            put(LT, Kind.LT);
            put(LE, Kind.LE);
            put(GE, Kind.GE);
            put(BOOL_TRUE, Kind.BOOLEAN_LIT);
            put(BOOL_FALSE, Kind.BOOLEAN_LIT);
            put(KW_CONST, Kind.KW_CONST);
            put(KW_VAR, Kind.KW_VAR);
            put(KW_PROCEEDURE, Kind.KW_PROCEDURE);
            put(KW_CALL, Kind.KW_CALL);
            put(KW_BEGIN, Kind.KW_BEGIN);
            put(KW_END, Kind.KW_END);
            put(KW_IF, Kind.KW_IF);
            put(KW_WHILE, Kind.KW_WHILE);
            put(KW_DO, Kind.KW_DO);
        }
    };


}
