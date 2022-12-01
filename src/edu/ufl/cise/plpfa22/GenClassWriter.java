package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.Declaration;
import org.objectweb.asm.ClassWriter;

public class GenClassWriter extends ClassWriter {
    String className;
    Declaration dec;

    GenClassWriter(int params) {
        super(params);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}