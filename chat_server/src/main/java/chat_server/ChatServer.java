package chat_server;

import chat_server.services.autorization.AuthorizationService;
import chat_server.services.autorization.DataBaseAuthService;
import chat_server.services.history.DataBaseHistoryService;
import chat_server.services.history.HistoryService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatServer {

    private static final int PORT = 6000;
    private final AuthorizationService authService;
    private final HistoryService historyService;
    private final List<ClientHandler> handlers;
    private Connection dataBaseConnection;
    private final String name = "Alex";
    private final String pass = "1111";
    private final String connectionURL = "jdbc:mysql://localhost:3306/online_chat";

    public ChatServer() {
        try {
            dataBaseConnection = DriverManager.getConnection(connectionURL, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.authService = new DataBaseAuthService(dataBaseConnection);
        this.historyService = new DataBaseHistoryService(dataBaseConnection, 1000,10);
        this.handlers = new ArrayList<>();
    }

    public List<ClientHandler> getHandlers() {
        return handlers;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server start");
            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(240000);
                System.out.println("Client connected");
                new ClientHandler(socket, this).launch();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            historyService.stop();
        }
    }

    public void broadcastMessage(String senderNick, String message) {
        char splitterOne = 1000;
        char splitterTwo = 5000;
        String[] parsingMessage = message.split("" + splitterTwo);
        List<String> nicksRecipients = new ArrayList<>(Arrays.asList(parsingMessage[0].split("" + splitterOne)));
        if (nicksRecipients.size() == 1) {
            for (ClientHandler handler : handlers) {
                handler.sendMessage(senderNick + ": " + parsingMessage[1]);
            }
            return;
        }
        for (ClientHandler handler : handlers) {
            if (nicksRecipients.contains(handler.getCurrentNickUser())) {
                handler.sendMessage(senderNick + ": " + parsingMessage[1]);
            }
        }
    }

    public synchronized void removeAuthorizedClientFromList(ClientHandler handler) {
        this.handlers.remove(handler);
        sendClientsOnline();
    }

    public synchronized void addAuthorizedClientToList(ClientHandler handler) {
        this.handlers.add(handler);
        sendClientsOnline();
    }

    public AuthorizationService getAuthService() {
        return authService;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public void sendClientsOnline() {
        char symbol = 10000;
        StringBuilder sb = new StringBuilder("$.list:" + symbol + "Send to All" + symbol);
        for (ClientHandler handler : handlers) {
            sb.append(handler.getCurrentNickUser()).append(symbol);
        }
        System.out.println(sb.toString());
        for (ClientHandler handler : handlers) {
            handler.sendMessage(sb.toString());
        }
    }


}