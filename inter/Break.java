package inter;

public class Break extends Stmt {
    @Override
    public void exec(symbols.Env env) {
        throw new BreakException();
    }
}