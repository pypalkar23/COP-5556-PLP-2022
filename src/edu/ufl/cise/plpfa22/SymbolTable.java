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


    public SymbolTable(){
        this.currScope = -1;
        table = new HashMap<>();
        scopeStack = new ArrayDeque<>();
        scopeStack.push(currScope);
    }

    public void enterScope() {
        currScope++;
        scopeStack.push(currScope);
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
            while(itr.hasNext()) {
                int scopeId = itr.next();
                SymbolTableRecord scanner = record;
                while (scanner != null && scanner.scope != scopeId) {
                    scanner = scanner.next;
                }
                if (scanner != null)
                {
                    record = scanner;
                    break;
                }

            }
        }
        return record;
    }

}
