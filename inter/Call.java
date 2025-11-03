package inter;

import java.util.List;

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

                    // create a local env for the method and bind it as current for this thread
                    symbols.Env localEnv = new symbols.Env(null);
                    // save old values from whichever env is current
                    old.put("this", symbols.Env.getStatic("this"));
                    for (String pname : m.params) {
                        old.put(pname, symbols.Env.getStatic(pname));
                    }

                    // bind local env to thread and populate parameters/this into it
                    localEnv.bindCurrent();
                    symbols.Env.putStatic("this", inst);
                    for (int i = 0; i < m.params.size(); i++) {
                        String pname = m.params.get(i);
                        Object pval = (i < args.size()) ? args.get(i).eval() : null;
                        symbols.Env.putStatic(pname, pval);
                    }

                    // execute the method body and catch ReturnException to obtain return value
                    Object ret = null;
                    try {
                        try {
                            m.body.exec(localEnv);
                        } catch (ReturnException rex) {
                            ret = rex.getValue();
                        }
                    } finally {
                        // restore old parameter values and 'this'
                        for (String pname : m.params) {
                            symbols.Env.putStatic(pname, old.get(pname));
                        }
                        symbols.Env.putStatic("this", old.get("this"));
                        // unbind the local env from the current thread
                        localEnv.unbindCurrent();
                    }
                    return ret;
                }
            }
        }

        throw new RuntimeException("Método '" + methodName + "' não encontrado na classe " + inst.getDecl().name);
    }
}
