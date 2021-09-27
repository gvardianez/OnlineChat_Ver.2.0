package chat_server.services.autorization;

import chat_server.errors.LoginIsNotAvailableException;
import chat_server.errors.NicknameIsNotAvailableException;
import chat_server.errors.UserNotFoundException;
import chat_server.errors.WrongCredentialsException;
import chat_server.services.history.DataBaseHistoryService;

import java.sql.*;

public class DataBaseAuthService implements AuthorizationService {
    private final Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public DataBaseAuthService(Connection connection) {
        this.connection = connection;
        start();
    }

    @Override
    public void start() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists users (idUsers int auto_increment primary key, nickname varchar(45) not null unique , login varchar(45) not null unique , password varchar(45) not null);");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DataBaseHistoryService.close(preparedStatement, resultSet, connection);
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            preparedStatement = connection.prepareStatement("select nickname from users where login = ? and password = ?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            } else throw new WrongCredentialsException("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new UserNotFoundException("");
    }

    @Override
    public boolean changeNickname(String nickname, String password, String newNickname) {
        try {
            preparedStatement = connection.prepareStatement("select nickname from users where nickname = ?;", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, newNickname);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                throw new NicknameIsNotAvailableException("");
            }
            preparedStatement = connection.prepareStatement("select idUsers,password,nickname from users where nickname = ?;", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, nickname);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("password").equals(password)) {
                    resultSet.updateString("nickname", newNickname);
                    resultSet.updateRow();
                    return true;
                } else throw new WrongCredentialsException("");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean changePassword(String nickname, String oldPassword, String newPassword) {
        try {
            preparedStatement = connection.prepareStatement("select idUsers,password from users where nickname = ?;", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, nickname);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if (resultSet.getString("password").equals(oldPassword)) {
                resultSet.updateString("password", newPassword);
                resultSet.updateRow();
                return true;
            } else throw new WrongCredentialsException("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void createNewUser(String login, String password, String nickname) {
        try {
            preparedStatement = connection.prepareStatement("select login from users where login = ?;");
            preparedStatement.setString(1, login);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) throw new LoginIsNotAvailableException("");
            preparedStatement = connection.prepareStatement("select nickname from users where nickname = ?;");
            preparedStatement.setString(1, nickname);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) throw new NicknameIsNotAvailableException("");
            preparedStatement = connection.prepareStatement("INSERT INTO `online_chat`.`users` (`nickname`, `login`, `password`) VALUES (?, ?, ?);");
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, login);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(String nickname, String password) {
        try {
            preparedStatement = connection.prepareStatement("select idUsers,password from users where nickname = ?;", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, nickname);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("password").equals(password)) {
                    resultSet.deleteRow();
                    return;
                }
            }
            throw new WrongCredentialsException("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
