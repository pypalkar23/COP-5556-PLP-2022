package edu.ufl.cise.plpfa22;

public class PLPToken implements  IToken{
    Kind kind;
    char[] text;
    SourceLocation srcLocation;

    public PLPToken(Kind kind, char[] text, int line, int column){
        super();
        this.kind = kind;
        this.text = text;
        this.srcLocation = new SourceLocation(line,column);
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
        if (this.kind == Kind.NUM_LIT){
            return Integer.parseInt(String.valueOf(this.text));
        }
        return 0;
    }

    @Override
    public boolean getBooleanValue() {
        if (this.kind == Kind.BOOLEAN_LIT){
            return String.valueOf(this.text).equals("TRUE");
        }
        return false;
    }

    @Override
    public String getStringValue() {
        if (this.kind == Kind.STRING_LIT){
            return String.valueOf(this.text);
        }
        return null;
    }
}
