package pwn.challenge;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Main {
    public static void main(String[] args) throws Exception {
        Logger.getLogger("io.netty").setLevel(Level.OFF);
        Console console = new Console();
        console.run();
    }
}
