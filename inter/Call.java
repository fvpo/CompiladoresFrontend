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
        // Two modes: method call on an instance (target != null) or function call (target == null)
        if (target == null) {
            // function call: look up MethodDecl by name in the global Env
            Object f = symbols.Env.get(methodName);
            if (!(f instanceof MethodDecl)) {
                throw new RuntimeException("Função não encontrada: " + methodName);
            }
            MethodDecl m = (MethodDecl) f;

            // bind parameters in global Env temporarily
            java.util.Map<String, Object> oldVals = new java.util.HashMap<>();
            java.util.Set<String> added = new java.util.HashSet<>();
            try {
                if (m.params != null) {
                    for (int i = 0; i < m.params.size(); i++) {
                        String pname = m.params.get(i);
                        Object pval = (args != null && i < args.size()) ? args.get(i).eval() : null;
                        boolean existed = symbols.Env.containsInCurrent(pname);
                        oldVals.put(pname, symbols.Env.get(pname));
                        symbols.Env.put(pname, pval);
                        if (!existed) added.add(pname);
                    }
                }
                try {
                    m.body.exec(new symbols.Env(null));
                    return null;
                } catch (ReturnException re) {
                    return re.getValue();
                }
            } finally {
                // restore old values
                for (String n : oldVals.keySet()) {
                    if (added.contains(n)) symbols.Env.remove(n);
                    else symbols.Env.put(n, oldVals.get(n));
                }
            }
        }

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
                    // bind 'this' and parameters in global Env temporarily
                    java.util.Map<String, Object> oldVals = new java.util.HashMap<>();
                    java.util.Set<String> added = new java.util.HashSet<>();
                    try {
                        boolean existedThis = symbols.Env.containsInCurrent("this");
                        oldVals.put("this", symbols.Env.get("this"));
                        symbols.Env.put("this", inst);
                        if (!existedThis) added.add("this");

                        if (m.params != null) {
                            for (int i = 0; i < m.params.size(); i++) {
                                String pname = m.params.get(i);
                                Object pval = (args != null && i < args.size()) ? args.get(i).eval() : null;
                                boolean existed = symbols.Env.containsInCurrent(pname);
                                oldVals.put(pname, symbols.Env.get(pname));
                                symbols.Env.put(pname, pval);
                                if (!existed) added.add(pname);
                            }
                        }

                            try {
                            m.body.exec(new symbols.Env(null));
                            return null;
                        } catch (ReturnException re) {
                            return re.getValue();
                        }
                    } finally {
                        for (String n : oldVals.keySet()) {
                            if (added.contains(n)) symbols.Env.remove(n);
                            else symbols.Env.put(n, oldVals.get(n));
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Método '" + methodName + "' não encontrado na classe " + inst.getDecl().name);
    }
}
