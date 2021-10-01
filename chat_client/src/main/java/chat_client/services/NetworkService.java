package chat_client.services;

import java.io.*;
import java.net.Socket;

public class NetworkService {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 6000;
    private final Socket socket;
    private final BufferedWriter out;
    private final BufferedReader in;
    private final ChatMessageService chatMessageService;

    public NetworkService(ChatMessageService chatMessageService) throws IOException {
        this.chatMessageService = chatMessageService;
        this.socket = new Socket(HOST, PORT);
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void readMessages() {
        Thread read = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()&&!socket.isClosed()) {
                try {
                    String message = in.readLine();
                    chatMessageService.receive(message);
                } catch (IOException e) {
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
            out.write(message + System.lineSeparator());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}