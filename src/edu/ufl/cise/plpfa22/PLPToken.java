package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

public class PLPToken implements IToken {
    Kind kind;
    char[] text;
    SourceLocation srcLocation;

    public PLPToken(Kind kind, char[] text, int line, int column) {
        super();
        this.kind = kind;
        this.text = text;
        this.srcLocation = new SourceLocation(line, column);
    }

    @Override
    public Kind getKind() {
        return this.kind;
    }

    @Override
    public char[] getText() {
        return text;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return this.srcLocation;
    }

    @Override
    public int getIntValue() {
        if (this.kind == Kind.NUM_LIT) {
            return Integer.parseInt(String.valueOf(this.text));
        }
        return 0;
    }

    @Override
    public boolean getBooleanValue() {
        if (this.kind == Kind.BOOLEAN_LIT) {
            return String.valueOf(this.text).equals("TRUE");
        }
        return false;
    }

    @Override
    public String getStringValue() {
        if(this.kind!=Kind.STRING_LIT)
            return String.valueOf(text);

        StringBuffer sb = new StringBuffer();
        for(int i=1;i<this.text.length-1;i++){
            char currChar = this.text[i];
            if(currChar=='\\'){
                i++;
                if(i==this.text.length-1){
                    break;
                }
                currChar = this.text[i];
                switch(currChar){
                    case 'b'-> sb.append('\b');
                    case 't'-> sb.append('\t');
                    case 'f'-> sb.append('\f');
                    case 'n'-> sb.append('\n');
                    case 'r'-> sb.append('\r');
                    case '\"','\'','\\'-> sb.append(currChar);
                }
            }else{
                sb.append(currChar);
            }
        }
        return sb.toString();
    }

    private char[] getCharArrayForString() {
        StringBuffer sb = new StringBuffer();
        for (char c : this.text) {
            switch (String.valueOf(c)) {
                case LexerUtils.QUOTE, LexerUtils.SINGLE_QUOTE, LexerUtils.BACKSLASH, LexerUtils.TAB,
                        LexerUtils.NEW_LINE, LexerUtils.FORM_FEED , LexerUtils.CARRIAGE_RETURN-> {
                    sb.append(LexerUtils.BACKSLASH);
                }
            }
            sb.append(c);
        }
        return sb.toString().toCharArray();
    }
}
