package inter;

import symbols.Env;

import java.util.ArrayList;
import java.util.List;

public class Par extends Stmt {
    private final List<Stmt> stmts;

    public Par(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    @Override
    public void exec() {
        List<Thread> threads = new ArrayList<>();

        for (Stmt s : stmts) {
            if (s != null) {
                Thread t = new Thread(() -> s.exec());
                threads.add(t);
                t.start();
            }
        }

        // espera todas as threads terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Execução paralela interrompida", e);
            }
        }
    }
    public void exec(Env env) {
        List<Thread> threads = new ArrayList<>();

        for (Stmt s : stmts) {
            if (s != null) {
                Thread t = new Thread(() -> s.exec(env));
                threads.add(t);
                t.start();
            }
        }

        // espera todas as threads terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Execução paralela interrompida", e);
            }
        }
    }
}