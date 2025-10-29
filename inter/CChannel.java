package inter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CChannel {
    private final BlockingQueue<Object> queue;

    public CChannel() {
        this.queue = new LinkedBlockingQueue<>();
    }

    // envia um valor para o canal
    public void send(Object value) {
        try {
            queue.put(value); // bloqueia se a fila estiver cheia (não deve acontecer no LinkedBlockingQueue sem limite)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Envio interrompido", e);
        }
    }

    // recebe um valor do canal (bloqueia se não houver valor)
    public Object receive() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Recepção interrompida", e);
        }
    }
}