package chat_server;

import chat_server.errors.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatServer server;
    private String currentNickUser;
    private final char symbol = 10000;

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launch() {
        Thread handlerThread = new Thread(() -> {
            authorize();
            System.out.println("launch");
            try {
                while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                    socket.setSoTimeout(1000 * 60 * 30);
                    String message = in.readUTF();
                    String[] parseMessageArray = message.split("" + symbol);
                    System.out.println(parseMessageArray[0]);
                    switch (parseMessageArray[0]) {
                        case ("changePass:"): {
                            changePassword(parseMessageArray);
                            break;
                        }
                        case ("changeNick:"): {
                            changeNick(parseMessageArray);
                            break;
                        }
                        case ("del:"): {
                            deleteAccount(parseMessageArray[1]);
                            break;
                        }
                        case ("sendMessage:"): {
                            processMessage(currentNickUser, parseMessageArray[1]);
                            break;
                        }
                        case ("viewAllHistory:"): {
                            sendMessage(server.getHistoryService().loadMessageHistory(currentNickUser, server.getHistoryService().getValueOfSaveRaw()));
                        }
                        case ("clearAllHistory:"): {
                            if (server.getHistoryService().clearMessageHistory(currentNickUser))
                                sendMessage("Message History Clear");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection("Close");
                System.out.println("finally");
                server.removeAuthorizedClientFromList(this);
            }
        });
        handlerThread.start();
    }

    private void processMessage(String currentNickUser, String parseMessageArray) {
        server.getHistoryService().saveMessage(currentNickUser, parseMessageArray);
        server.broadcastMessage(currentNickUser, parseMessageArray);
    }

    private void authorize() {
        while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
            try {
                String message = in.readUTF();
                String[] parseMessageArray = message.split("" + symbol);
                String parseMessage = parseMessageArray[0];
                if (parseMessage.equals("auth:")) {
                    try {
                        this.currentNickUser = server.getAuthService().getNicknameByLoginAndPassword(parseMessageArray[1], parseMessageArray[2]);
                        checkAlreadyAuthorize();
                        this.server.addAuthorizedClientToList(this);
                        sendMessage("authok:" + symbol + this.currentNickUser);
                        sendMessage(server.getHistoryService().loadMessageHistory(currentNickUser, server.getHistoryService().getValueOfLoadRaw()));
                        return;
                    } catch (AlreadyAuthorizeException e) {
                        sendMessage("ERROR:" + symbol + "You are already is authorized");
                        continue;
                    } catch (WrongCredentialsException e) {
                        sendMessage("ERROR:" + symbol + " Wrong credentials");
                        continue;
                    } catch (UserNotFoundException e) {
                        sendMessage("ERROR:" + symbol + " User not found!");
                        continue;
                    }
                }
                if (parseMessage.equals("reg:")) {
                    try {
                        server.getAuthService().createNewUser(parseMessageArray[1], parseMessageArray[2], parseMessageArray[3]);
                        sendMessage("regok:" + symbol);
                    } catch (LoginIsNotAvailableException e) {
                        sendMessage("ERROR:" + symbol + "Login is not available");
                    } catch (NicknameIsNotAvailableException e) {
                        sendMessage("ERROR:" + symbol + "Nickname is not available");
                    }
                }
            } catch (IOException e) {
                closeConnection("Server Time Out");
                e.printStackTrace();
                break;
            }
        }
    }

    private void checkAlreadyAuthorize() {
        for (ClientHandler clientHandler : this.server.getHandlers()) {
            if (clientHandler.getCurrentNickUser().equals(this.currentNickUser)) {
                throw new AlreadyAuthorizeException("");
            }
        }
    }

    private void closeConnection(String message) {
        sendMessage("Connection broke" + symbol + message);
        Thread.currentThread().interrupt();
        try {
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void changeNick(String[] messageArray) {
        try {
            server.getAuthService().changeNickname(currentNickUser, messageArray[1], messageArray[2]);
            System.out.println("changed nickname");
            currentNickUser = messageArray[2];
            server.sendClientsOnline();
            sendMessage("changeNickOk:" + symbol);
        } catch (NicknameIsNotAvailableException e) {
            sendMessage("ERROR:" + symbol + "Nickname is not available");
        } catch (WrongCredentialsException e) {
            sendMessage("ERROR:" + symbol + "Wrong credentials");
        }
    }

    private void changePassword(String[] messageArray) {
        try {
            server.getAuthService().changePassword(currentNickUser, messageArray[1], messageArray[2]);
            System.out.println("changed password");
            sendMessage("changePasswordOk:" + symbol);
        } catch (WrongCredentialsException e) {
            sendMessage("ERROR:" + symbol + "Wrong credentials");
        } catch (UserNotFoundException e) {
            sendMessage("ERROR:" + symbol + "User not found!");
        }
    }

    private void deleteAccount(String password) {
        try {
            server.getAuthService().deleteUser(currentNickUser, password);
            System.out.println("delete user ok");
            sendMessage("deleteAccountOk:" + symbol);
            closeConnection("Account Delete Successfully");
        } catch (WrongCredentialsException e) {
            sendMessage("ERROR:" + symbol + "Wrong credentials");
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentNickUser() {
        return currentNickUser;
    }
}