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
        return 0;
    }

    @Override
    public boolean getBooleanValue() {
        return false;
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
