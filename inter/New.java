package inter;

import lexer.*;
import symbols.*;
import java.util.List;

public class New extends Expr {
    private final String className;
    private final List<Expr> args;
    private final Type type;  // type of object being created

    public New(String className, List<Expr> args) {
        super(Word.newWord, Type.voidWord); // chamada ao construtor de Expr
        this.className = className;
        this.args = args;
        this.type = Type.voidWord;
    }
    
    public New(String className, Type type) {
        super(Word.newWord, type); // chamada ao construtor de Expr
        this.className = className;
        this.args = null;
        this.type = type;
    }

    @Override
    public Object eval() {
        // Special handling for c_channel creation
        if ("c_channel".equals(className) || type == Type.cchannelWord) {
            return new CChannel();
        }
        
    // For classes, look up class declaration and create instance
    Object clsObj = symbols.Env.getStatic(className);

        if (clsObj instanceof ClassDecl cd) {
            // cria a instância da classe
            ClassInstance instance = new ClassInstance(cd);

            // inicializa campos declarados
            for (Stmt member : cd.members) {
                if (member instanceof Decl decl) {
                    Object initVal = null;
                    if (decl.init != null) {
                        initVal = decl.init.eval();
                    }
                    instance.setField(decl.id.op.toString(), initVal);
                }
            }

            // executa o construtor, se houver
            MethodDecl constructor = cd.getConstructor();
            if (constructor != null) {
                // cria um ambiente local para a execução do construtor
                java.util.Map<String, Object> old = new java.util.HashMap<>();
                symbols.Env localEnv = new symbols.Env(null);

                // save old 'this' and parameter values
                old.put("this", symbols.Env.getStatic("this"));
                for (String pname : constructor.params) {
                    old.put(pname, symbols.Env.getStatic(pname));
                }

                // bind the local env and populate 'this' and params
                localEnv.bindCurrent();
                symbols.Env.putStatic("this", instance);
                for (int i = 0; i < constructor.params.size(); i++) {
                    String paramName = constructor.params.get(i);
                    Object argValue = (i < args.size()) ? args.get(i).eval() : null;
                    symbols.Env.putStatic(paramName, argValue);
                }

                // executa o corpo do construtor
                try {
                    constructor.body.exec(localEnv);
                } finally {
                    // restore old values and unbind
                    for (String pname : constructor.params) {
                        symbols.Env.putStatic(pname, old.get(pname));
                    }
                    symbols.Env.putStatic("this", old.get("this"));
                    localEnv.unbindCurrent();
                }
            }

            return instance;
        }

        // classes internas do runtime (como CChannel)
        if ("CChannel".equals(className)) {
            return new CChannel();
        }

        throw new RuntimeException("Classe não suportada: " + className);
    }
}