package edu.ufl.cise.plpfa22.implementations;

import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.Types.Type;

import java.util.*;

public class SymbolTable {
    static class SymbolTableRecord {
        int scope;
        Declaration dec;
        SymbolTableRecord next;

        Type type;

        public SymbolTableRecord(int scope, Declaration dec, SymbolTableRecord next) {
            this.scope = scope;
            this.dec = dec;
            this.next = next;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }
    }

    Deque<Integer> scopeStack;
    HashMap<String, SymbolTableRecord> table;
    int currScope;
    int nestLevel;
    int maxScope;

    public SymbolTable() {
        /*this.nestLevel = -1;
        this.maxScope = -1;
        this.currScope = maxScope;

        this.scopeStack = new ArrayDeque<>();
        this.scopeStack.push(currScope);*/
        this.resetScopeCounters();
        table = new HashMap<>();
    }

    public void enterNestLevel() {
        this.nestLevel++;
    }

    public int getNestLevel() {
        return this.nestLevel;
    }

    public int leaveNestLevel() {
        return this.nestLevel--;
    }


    public void resetScopeCounters(){
        this.nestLevel = -1;
        this.maxScope = -1;
        this.currScope = maxScope;
        this.scopeStack = new ArrayDeque<>();
        this.scopeStack.push(currScope);
    }

    public void enterScope() {
        maxScope++;
        currScope = maxScope;
        scopeStack.push(maxScope);
    }

    public void updateScope() {
        currScope = scopeStack.peek();
    }

    public void leaveScope() {
        scopeStack.pop();
        currScope = scopeStack.peek();
    }

    public boolean insert(String name, Declaration dec) {
        SymbolTableRecord record = this.table.getOrDefault(name, null);

        while (record != null) {
            if (record.scope == this.currScope) {
                return false;
            }
            record = record.next;
        }
        table.put(name, new SymbolTableRecord(this.currScope, dec, table.get(name)));
        return true;
    }

    public Declaration findDeclaration(String name) {
        SymbolTableRecord record = findRecord(name);
        if (record != null)
            return record.dec;

        return null;
    }


    public SymbolTableRecord findRecord(String name) {
        SymbolTableRecord result = null;
        SymbolTableRecord scanner = this.table.get(name);
        while (scanner != null && scanner.scope > this.currScope) {
            scanner = scanner.next;
        }
        result = scanner;
        return result;
    }

}
