package chat_server.services.autorization;

import chat_server.errors.LoginIsNotAvailableException;
import chat_server.errors.NicknameIsNotAvailableException;
import chat_server.errors.UserNotFoundException;
import chat_server.errors.WrongCredentialsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InnerAuthService implements AuthorizationService {
    private final String name = "Alex";
    private final String pass = "1111";
    private final String connectionURL = "jdbc:mysql://localhost:3306/online_chat";
    private final List<User> users;

    public InnerAuthService() {
        this.users = new ArrayList<>(Arrays.asList(
                new User("log1", "pass", "nick1"),
                new User("log2", "pass", "nick2"),
                new User("log3", "pass", "nick3"),
                new User("log4", "pass", "nick4")
        )
        );
    }

    @Override
    public void start() {
//        try (Connection connection = DriverManager.getConnection(connectionURL, name, pass)) {
//            System.out.println("Database connected!");
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
//            while (resultSet.next()) {
//                users.add(new User(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("nickname")));
//            }
//            System.out.println(users);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void stop() {
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (login.equals(user.getLogin())) {
                if (password.equals(user.getPassword())) return user.getNickname();
                else throw new WrongCredentialsException("");
            }
        }
        throw new UserNotFoundException("User not found");
    }

    @Override
    public boolean changeNickname(String login, String password, String newNickname) {
        for (User user : users) {
            if (user.getNickname().equals(newNickname))
                throw new NicknameIsNotAvailableException("Nickname is not available");
        }
        for (User user : users) {
            if (login.equals(user.getLogin())) {
                if (password.equals(user.getPassword())) {
                    user.setNickname(newNickname);
                    return true;
                } else throw new WrongCredentialsException("");
            }
        }
        throw new UserNotFoundException("User not found");
    }

    @Override
    public boolean changePassword(String nickname, String oldPassword, String newPassword) {
        for (User user : users) {
            if (nickname.equals(user.getNickname())) {
                if (oldPassword.equals(user.getPassword())) {
                    user.setPassword(newPassword);
                    return true;
                } else throw new WrongCredentialsException("");
            }
        }
        throw new UserNotFoundException("User not found");
    }

    @Override
    public void createNewUser(String login, String password, String nickname) {
        for (User user : users) {
            if (user.getLogin().equals(login)) throw new LoginIsNotAvailableException("");
            if (user.getNickname().equals(nickname)) throw new NicknameIsNotAvailableException("");
        }
        users.add(new User(login, password, nickname));
    }

    @Override
    public void deleteUser(String login,String password) {
        for (User user : users) {
            if (login.equals(user.getLogin())) {
                if (password.equals(user.getPassword())) {
                    users.remove(user);
                    return;
                }
                else throw new WrongCredentialsException("");
            }
        }
        throw new UserNotFoundException("User not found");
    }
}