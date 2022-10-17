package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.Declaration;

import java.util.*;

public class SymbolTable {
    static class SymbolTableRecord {
        int scope;
        Declaration dec;
        SymbolTableRecord next;

        public SymbolTableRecord(int scope, Declaration dec, SymbolTableRecord next) {
            this.scope = scope;
            this.dec = dec;
            this.next = next;

        }
    }

    Deque<Integer> scopeStack;
    HashMap<String, SymbolTableRecord> table;
    int currScope;
    int nestLevel;
    int maxScope;

    public SymbolTable(){
        this.nestLevel = -1;
        this.maxScope = -1;
        this.currScope = maxScope;
        table = new HashMap<>();
        scopeStack = new ArrayDeque<>();
        scopeStack.push(currScope);
    }

    public void enterNestLevel(){
        this.nestLevel++;
    }

    public int getNestLevel(){
        return this.nestLevel;
    }

    public int leaveNestLevel(){
        return this.nestLevel--;
    }


    public void enterScope() {
        maxScope++;
        scopeStack.push(maxScope);
    }

    public void updateScope(){
        currScope = scopeStack.peek();
    }

    public void leaveScope() {
        scopeStack.pop();
        currScope = scopeStack.peek();
    }

    public boolean insert(String name, Declaration dec) {
        SymbolTableRecord record = this.table.getOrDefault(name, null);

        while (record != null) {
            if (record.scope == currScope) {
                return false;
            }
            record = record.next;
        }
        table.put(name, new SymbolTableRecord(currScope, dec, table.get(name)));
        return true;
    }

    public Declaration findDeclaration(String name) {
        SymbolTableRecord record = findRecord(name);
        if (record != null)
            return record.dec;

        return null;
    }


    public SymbolTableRecord findRecord(String name) {
        SymbolTableRecord record = this.table.get(name);
        if (record != null) {
            Iterator<Integer> itr = scopeStack.iterator();
            //while(itr.hasNext()) {
                //int scopeId = itr.next();

                SymbolTableRecord scanner = record;
                while (scanner != null && scanner.scope > this.currScope) {
                    scanner = scanner.next;
                }
                if (scanner != null)
                {
                    record = scanner;
                    //break;
                }

            //}
        }
        return record;
    }

}
