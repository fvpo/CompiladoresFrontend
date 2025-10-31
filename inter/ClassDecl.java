package inter;
import symbols.Env;

import java.util.List;

public class ClassDecl extends Stmt {
    public final String name;
    public final String superName;   // null se não houver extends
    public final List<Stmt> members;

    public ClassDecl(String name, String superName, List<Stmt> members) {
        this.name = name;
        this.superName = superName;
        this.members = members;
    }

    @Override
    public void exec() {
        throw new UnsupportedOperationException("Use exec(Env env) instead.");
    }
    public void exec(Env env) {
        env.put(name, this);
    }
}
