package chat_server.services.history;

import java.sql.*;

public class DataBaseHistoryService implements HistoryService {

    private final Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private final int valueOfSaveRaw;
    private final int valueOfLoadRaw;

    public DataBaseHistoryService(Connection connection, int valueOfSaveRaw, int valueOfLoadRaw) {
        this.connection = connection;
        this.valueOfSaveRaw = valueOfSaveRaw;
        this.valueOfLoadRaw = valueOfLoadRaw;
        start();
    }

    @Override
    public void start() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists message_history (idmessage_history int auto_increment primary key unique, user_id int not null, message mediumtext not null, foreign key (user_id) references users(idUsers));");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        close(preparedStatement, resultSet, connection);
    }

    public int getValueOfSaveRaw() {
        return valueOfSaveRaw;
    }

    public int getValueOfLoadRaw() {
        return valueOfLoadRaw;
    }

    @Override
    public void saveMessage(String nickUser, String message, String date) {
        int idUser;
        String messageForSave = "[" + date + "]" + message;
        try {
            preparedStatement = connection.prepareStatement("select idUsers from users where nickname = ?;");
            preparedStatement.setString(1, nickUser);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            idUser = resultSet.getInt("idUsers");
            preparedStatement = connection.prepareStatement("insert into message_history (user_id, message) values (?, ?);");
            preparedStatement.setInt(1, idUser);
            preparedStatement.setString(2, messageForSave);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String loadMessageHistory(String nickUser, int valueOfLoadRaw) {
        int valueForDeleteRaw;
        StringBuilder messageForSend = new StringBuilder();
        try {
            preparedStatement = connection.prepareStatement("select idmessage_history,user_id,message from message_history join users on user_id = idUsers where nickname = ?;", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, nickUser);
            resultSet = preparedStatement.executeQuery();
            resultSet.last();
            int valueOfRaw = resultSet.getRow();
            System.out.println(resultSet.getRow());
            valueForDeleteRaw = valueOfRaw - valueOfSaveRaw;
            if (valueForDeleteRaw > 0) {
                resultSet.first();
                for (int i = 0; i < valueForDeleteRaw; i++) {
                    resultSet.deleteRow();
                    resultSet.next();
                }
            }
            if ((valueOfRaw - valueOfLoadRaw) <= 0) {
                resultSet.beforeFirst();
            } else resultSet.absolute(valueOfRaw - valueOfLoadRaw);
            while (resultSet.next()) {
                messageForSend.append(resultSet.getString("message")).append(System.lineSeparator());
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageForSend.toString();
    }

    @Override
    public boolean clearMessageHistory(String nickUser) {
        try {
            preparedStatement = connection.prepareStatement("delete from message_history where user_id = (select idUsers from users where nickname = ?);");
            preparedStatement.setString(1, nickUser);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void close(PreparedStatement preparedStatement, ResultSet resultSet, Connection connection) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
