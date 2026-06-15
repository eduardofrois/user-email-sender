package dev.java10x.email.helpers;

import org.springframework.stereotype.Component;

@Component
public class Delay {
    public void simulateDelay() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operacao interrompida durante o delay", exception);
        }
    }
}
