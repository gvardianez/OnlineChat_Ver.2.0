package chat_client.services;

import chat_client.MessageProcessor;

import java.io.IOException;

public class ChatMessageService {
    private final MessageProcessor messageProcessor;
    private NetworkService networkService;

    public ChatMessageService(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public void connect() {
        if (isConnected()) return;
        try {
            this.networkService = new NetworkService(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        networkService.readMessages();
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public boolean isConnected() {
        return this.networkService != null && !this.networkService.getSocket().isClosed();
    }

    public void send(String message) {
        this.networkService.sendMessage(message);
    }

    public void receive(String message) {
        messageProcessor.processMessage(message);
    }
}