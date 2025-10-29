package inter;

import symbols.Env;

public abstract class Stmt extends Node {
    public abstract void exec(Env env);
}
