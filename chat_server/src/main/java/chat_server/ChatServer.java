package chat_server;

import chat_server.services.autorization.AuthorizationService;
import chat_server.services.autorization.DataBaseAuthService;
import chat_server.services.history.DataBaseHistoryService;
import chat_server.services.history.HistoryService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final Logger logger = LogManager.getLogger("serverLogs");
    private static final int PORT = 5000;
    private final AuthorizationService authService;
    private final HistoryService historyService;
    private final List<ClientHandler> handlers;
    private ExecutorService executorService;
    private Connection dataBaseConnection;
    private final String name = "Alex";
    private final String pass = "1111";
    private final String connectionURL = "jdbc:mysql://localhost:3306/online_chat?useUnicode=true&serverTimezone=UTC";

    public ChatServer() {
        try {
            dataBaseConnection = DriverManager.getConnection(connectionURL, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.authService = new DataBaseAuthService(dataBaseConnection);
        this.historyService = new DataBaseHistoryService(dataBaseConnection, 1000, 10);
        this.handlers = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<ClientHandler> getHandlers() {
        return handlers;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.debug("Server Start");
            while (true) {
                logger.debug("Waiting for connection");
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(240000);
                logger.debug("Client connected");
                executorService.execute(new ClientHandler(socket, this)::launch);
//              new ClientHandler(socket, this).launch();
            }
        } catch (IOException e) {
            logger.throwing(Level.ERROR, e);
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
            for (ClientHandler handler : handlers) {
                if (!senderNick.equals(handler.getCurrentNickUser()) && nicksRecipientsList.contains(handler.getCurrentNickUser())) {
                    historyService.saveMessage(handler.getCurrentNickUser(), messageForSave, date);
                    handler.sendMessage("[" + date + "] " + senderNick + ": " + parsingMessage[1]);
                } else if (senderNick.equals(handler.getCurrentNickUser()))
                    handler.sendMessage("[" + date + "] Me (To " + nicksRecipientsList + "): " + parsingMessage[1]);
            }
        }
        logger.info("Nicks of recipients: {}", nicksRecipientsList);
        messageForSave = "(To " + nicksRecipientsList + "): " + parsingMessage[1];
        historyService.saveMessage(senderNick, messageForSave, date);
    }

    public synchronized void removeAuthorizedClientFromList(ClientHandler handler) {
        this.handlers.remove(handler);
        logger.debug("Client exit: {}", handler.getCurrentNickUser());
        sendClientsOnline();
    }

    public synchronized void addAuthorizedClientToList(ClientHandler handler) {
        this.handlers.add(handler);
        logger.debug("Client enter: {}", handler.getCurrentNickUser());
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
        StringBuilder nicksOfCurrentUsers = new StringBuilder("$.list:" + symbol + "Send to All" + symbol);
        for (ClientHandler handler : handlers) {
            nicksOfCurrentUsers.append(handler.getCurrentNickUser()).append(symbol);
        }
        logger.info("List of members in chat {}", nicksOfCurrentUsers.toString());
        for (ClientHandler handler : handlers) {
            handler.sendMessage(nicksOfCurrentUsers.toString());
        }
    }


}