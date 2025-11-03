package inter;

public class Continue extends Stmt {
    @Override
    public void exec(symbols.Env env) {
        throw new ContinueException();
    }
}