package pwn.challenge;

import java.util.Arrays;


public class Console {

    private final static int KEY_BITS = 4096;

    private Context context;


    public Console() {
        this.context = new Context();
    }

    public void help() {
        System.out.println("Usage:");
        System.out.println("\thelp - Print help page");
        System.out.println("\tregister <USERNAME> - Register");
        System.out.println("\tauth <USERNAME> - Login");
        System.out.println("\tmsgto <USERNAME> - Send message to user");
        System.out.println("\tquit - Exit the SSHluyxa");
        System.out.println("");
    }

    public void auth(String username) throws Exception {
        if (Utils.userExists(username)) {
            final int CHALLENGE_LEN = 16;

            byte[] challenge = Utils.generateRandom(CHALLENGE_LEN);
            System.out.println("To login please execute following command");
            System.out.println("and enter the result");
            System.out.println(String.format("\techo %s | xxd -r -p | openssl rsautl -sign -inkey %s.key | xxd -p -c2000\n",
                        Utils.bytesToHex(challenge) ,username));

            byte[] response = Utils.hexToBytes(Utils.ask("[resp]>"));
            byte[] buffer = Utils.decrypt(Utils.getUserKey(username), response);

            if (!java.util.Arrays.equals(challenge, buffer)) {
                System.out.println("Invalid challenge response!");
                return;
            } 

            System.out.println(String.format("Welcome, %s!", username));
            this.context.runListener(username);
        } else {
            System.out.println(String.format("Cannot find user %s. Maybe you want to register?", username));
        }

    }

    public void register(String username) throws Exception {
        if (Utils.userExists(username)) {
            System.out.println(String.format("User %s already exists. Maybe you want to login?", username));
            return;
        }

        System.out.println("To register please specify your public key");
        System.out.println("If you dont have public key, generate key pair using following command and enter the result");
        System.out.println(String.format("\topenssl genrsa -out %s.key %d &>/dev/null; openssl rsa -in %s.key -pubout -outform DER | xxd -p -c2000\n",
                    username, KEY_BITS>>1, username));
        
        byte[] response = Utils.hexToBytes(Utils.ask("[resp]>"));
        Utils.saveUserKey(username, Arrays.copyOf(response, KEY_BITS/8));
        System.out.println(String.format("User %s has been created", username));
    }

    public void msgto(String username) throws Exception {
        if (!Utils.userExists(username)) {
            System.out.println(String.format("Cannot find user with userername `%s`", username));
            return;
        }
        byte[] key = Utils.getUserKey(username);
        System.out.println(String.format("\techo YOURMESSAGE | openssl rsautl -inkey <(echo %s | xxd -r -p) -keyform DER -encrypt | xxd -p -c 99999 \n",
                                             Utils.bytesToHex(key)));
        byte[] response = Utils.hexToBytes(Utils.ask("[msg]>"));
        Sender sender = new Sender(username);
        sender.send(response);
        System.out.println("Done!");
    }


    public Boolean process(String[] args) throws Exception {
        switch(args[0]) {
            case "help":
                help();
                break;
            case "register":
                register(args[1]);
                break;
            case "auth":
                auth(args[1]);
                break;
            case "msgto":
                msgto(args[1]);
                break;
            default:
                System.out.println("Unknown command. Try 'help' to see the command list.");
        }
        return true;
    }

    public void run() throws Exception {
        System.out.println("Welcome to SSHlyuxa Messenger!");
        try {
            while (true) {
                String input = Utils.ask(">>>");
                if (input == null)
                    break;

                String[] args = input.split(" ");
                if (args[0].equals("quit"))
                    break;
                process(args);
            }
        } finally {
            this.context.stopListener();
        }
    }


}
