package edu.ufl.cise.plpfa22;

public class PLPLexer implements  ILexer {
    String input;
    int lineCounter;
    int charCounter;
    int cursor;
    PLPToken currToken;
    public PLPLexer(String input){
        this.input = input;
        this.lineCounter = 1;
        this.charCounter = 1;
        this.cursor = 0;
    }
    @Override
    public IToken next() throws LexicalException {
        return null;
    }

    @Override
    public IToken peek() throws LexicalException {
        return this.currToken;
    }

    void scanForToken() throws LexicalException{

    }
}
