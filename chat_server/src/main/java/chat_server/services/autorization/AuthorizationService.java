package chat_server.services.autorization;

import java.sql.SQLException;

public interface AuthorizationService {
    void start();
    void stop();
    String getNicknameByLoginAndPassword(String login, String password) throws SQLException;
    boolean changeNickname(String login, String password, String newNickname);
    boolean changePassword(String login, String oldPassword, String newPassword);
    void createNewUser(String login, String password, String nickname);
    void deleteUser(String login, String password);
}
