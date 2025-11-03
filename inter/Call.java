package inter;

import java.util.List;
import symbols.Env;
import inter.ReturnException;

/**
 * Method call expression: target.method(args...)
 * Currently supports zero-argument methods on user-declared classes.
 */
public class Call extends Expr {
    public final Expr target;
    public final String methodName;
    public final List<Expr> args;

    public Call(Expr target, String methodName, List<Expr> args) {
        super(null, null);
        this.target = target;
        this.methodName = methodName;
        this.args = args;
    }

    @Override
    public Object eval() {
        Object obj = target.eval();
        if (!(obj instanceof ClassInstance)) {
            throw new RuntimeException("Chamada inválida: alvo não é uma instância de classe");
        }

        ClassInstance inst = (ClassInstance) obj;
        // find method in class declaration
        if (inst.getDecl() == null || inst.getDecl().members == null) {
            throw new RuntimeException("Classe não possui métodos: " + inst);
        }

        for (Stmt s : inst.getDecl().members) {
            if (s instanceof MethodDecl) {
                MethodDecl m = (MethodDecl) s;
                if (m.name.equals(methodName)) {
                    // bind 'this' and parameters (save old values to restore later)
                    java.util.Map<String, Object> old = new java.util.HashMap<>();

                    // save old 'this'
                    old.put("this", Env.get("this"));
                    Env.put("this", inst);

                    // bind parameters by name
                    for (int i = 0; i < m.params.size(); i++) {
                        String pname = m.params.get(i);
                        Object pval = null;
                        if (i < args.size()) {
                            pval = args.get(i).eval();
                        }
                        old.put(pname, Env.get(pname));
                        Env.put(pname, pval);
                    }

                    // execute the method body and catch ReturnException to obtain return value
                    Object ret = null;
                    try {
                        try {
                            m.body.exec(new Env(null));
                        } catch (ReturnException rex) {
                            ret = rex.getValue();
                        }
                    } finally {
                        // restore old parameter values and 'this'
                        for (String pname : m.params) {
                            Env.put(pname, old.get(pname));
                        }
                        Env.put("this", old.get("this"));
                    }
                    return ret;
                }
            }
        }

        throw new RuntimeException("Método '" + methodName + "' não encontrado na classe " + inst.getDecl().name);
    }
}
