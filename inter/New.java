package inter;

import lexer.*;
import symbols.*;
import java.util.List;

public class New extends Expr {
    public final String className;
    public final List<Expr> args;

    public New(String className, List<Expr> args) {
        super(Word.newWord, Type.voidWord); // chamada ao construtor de Expr
        this.className = className;
        this.args = args;
    }

    @Override
    public Object eval() {
        // First, check if a user-declared class with this name exists in the environment
        Object clsObj = Env.get(className);
        if (clsObj instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) clsObj;
            // create a runtime instance that holds fields declared in the class
            return new ClassInstance(cd);
        }

        // fallback to built-in/runtime classes
        if ("CChannel".equals(className)) {
            return new CChannel();
        }

        throw new RuntimeException("Classe não suportada: " + className);
    }
}