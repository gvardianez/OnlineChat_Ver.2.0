package chat_client.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkService {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 6000;
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ChatMessageService chatMessageService;

    public NetworkService(ChatMessageService chatMessageService) throws IOException {
        this.chatMessageService = chatMessageService;
        this.socket = new Socket(HOST, PORT);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public void readMessages() {
        Thread read = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = in.readUTF();
                    chatMessageService.receive(message);
                }
                catch (IOException e) {
                    System.out.println("break");
                    Thread.currentThread().interrupt();
                }
            }
        });
        read.start();
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}