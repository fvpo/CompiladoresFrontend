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
    public void exec(Env env) {
        // Registra a classe no ambiente global
        env.put(name, this);
    }

    /**
     * Retorna o construtor da classe, se houver.
     * O construtor é identificado como um MethodDecl cujo nome é igual ao nome da classe.
     */
    public MethodDecl getConstructor() {
        for (Stmt member : members) {
            if (member instanceof MethodDecl md && md.name.equals(name)) {
                return md;
            }
        }
        return null; // nenhum construtor declarado
    }
}