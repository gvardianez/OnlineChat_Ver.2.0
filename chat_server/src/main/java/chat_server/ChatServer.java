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

    private static final int PORT = 5000;
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
        this.historyService = new DataBaseHistoryService(dataBaseConnection, 1000, 10);
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

    public void broadcastMessageAndSave(String senderNick, String message, String date) {
        String messageForSave;
        char splitterOne = 1000;
        char splitterTwo = 5000;
        String[] parsingMessage = message.split("" + splitterTwo);
        List<String> nicksRecipientsList = new ArrayList<>(Arrays.asList(parsingMessage[0].split("" + splitterOne)));
        messageForSave = "(From " + senderNick + "): " + parsingMessage[1];
        nicksRecipientsList.remove("");
        if (nicksRecipientsList.size() == 0) {
            for (ClientHandler handler : handlers) {
                if (!senderNick.equals(handler.getCurrentNickUser())) {
                    nicksRecipientsList.add(handler.getCurrentNickUser());
                    historyService.saveMessage(handler.getCurrentNickUser(), messageForSave, date);
                    handler.sendMessage("[" + date + "] " + senderNick + ": " + parsingMessage[1]);
                }
            }
            for (ClientHandler handler : handlers) {
                if (senderNick.equals(handler.getCurrentNickUser())) {
                    handler.sendMessage("[" + date + "] Me (To " + nicksRecipientsList + "): " + parsingMessage[1]);
                }
            }
        } else {
            System.out.println(nicksRecipientsList);
            for (ClientHandler handler : handlers) {
                if (!senderNick.equals(handler.getCurrentNickUser()) && nicksRecipientsList.contains(handler.getCurrentNickUser())) {
                    historyService.saveMessage(handler.getCurrentNickUser(), messageForSave, date);
                    handler.sendMessage("[" + date + "] " + senderNick + ": " + parsingMessage[1]);
                } else if (senderNick.equals(handler.getCurrentNickUser()))
                    handler.sendMessage("[" + date + "] Me (To " + nicksRecipientsList + "): " + parsingMessage[1]);

            }
        }
        messageForSave = "(To " + nicksRecipientsList + "): " + parsingMessage[1];
        historyService.saveMessage(senderNick, messageForSave, date);
    }

    public synchronized void removeAuthorizedClientFromList(ClientHandler handler) {
        this.handlers.remove(handler);
        sendClientsOnline(handler.getCurrentNickUser());
    }

    public synchronized void addAuthorizedClientToList(ClientHandler handler) {
        this.handlers.add(handler);
        sendClientsOnline(handler.getCurrentNickUser());
    }

    public AuthorizationService getAuthService() {
        return authService;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public void sendClientsOnline(String currentNickUser) {
        char symbol = 10000;
        StringBuilder nicksOfCurrentUsers = new StringBuilder("$.list:" + symbol + "Send to All" + symbol);
        for (ClientHandler handler : handlers) {
            nicksOfCurrentUsers.append(handler.getCurrentNickUser()).append(symbol);
        }
        System.out.println(nicksOfCurrentUsers.toString());
        for (ClientHandler handler : handlers) {
            handler.sendMessage(nicksOfCurrentUsers.toString());
        }
    }


}