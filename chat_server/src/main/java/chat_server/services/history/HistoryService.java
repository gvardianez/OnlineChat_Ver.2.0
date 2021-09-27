package chat_server.services.history;

public interface HistoryService {
    void saveMessage(String nickUser,String message, String date);
    String loadMessageHistory(String nickUser, int valueOfLoadRaw);
    boolean clearMessageHistory(String nickUser);
    void start();
    void stop();
    int getValueOfSaveRaw();
    int getValueOfLoadRaw();
}
