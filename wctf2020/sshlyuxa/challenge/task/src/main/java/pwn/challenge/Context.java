package pwn.challenge;

import java.security.Key;



public class Context {

    private Listener listener;

    public void runListener(String username) throws Exception {
        if (this.listener != null) {
            this.listener.interrupt();
        }
        this.listener = new Listener(username);
        this.listener.start();
    }
    public void stopListener() {
        this.listener.interrupt();
    }
}
