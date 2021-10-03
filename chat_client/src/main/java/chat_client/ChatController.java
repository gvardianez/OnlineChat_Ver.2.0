package chat_client;

import chat_client.services.ChatMessageService;
import chat_client.services.FileHistoryService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ChatController implements Initializable, MessageProcessor {

    public VBox mainChatPanel, loginPanel, registrationPanel, changeNicknamePanel, changePasswordPanel, deletePanel;
    public TextArea mainChatArea;
    public ListView<String> contactList;
    public TextField inputField, loginField, loginFieldReg, nickFieldReg, changeNickname;
    public Button btnSendMessage;
    public PasswordField passwordField, passwordFieldReg, enterPassword, enterOldPassword, enterNewPassword, passwordFieldForDelete;
    private ChatMessageService chatMessageService;
    private FileHistoryService historyService;
    private String nickName;
    private final char symbol = 10000;

    private void parseMessage(String message) {
        String[] parseMessageArray = message.split("" + symbol);
        String parseMessage = parseMessageArray[0];
        System.out.println(parseMessage);
        switch (parseMessage) {
            case "authok:": {
                this.nickName = parseMessageArray[1];
                historyService = new FileHistoryService(nickName, "chat_client/src/history", 10);
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                mainChatArea.appendText(historyService.loadHistory(5) + System.lineSeparator());
                break;
            }
            case "ERROR:": {
                showError(parseMessageArray[1]);
                break;
            }
            case "$.list:": {
                ObservableList<String> list = FXCollections.observableArrayList(message.substring(8).split("" + symbol));
                list.remove(nickName);
                contactList.setItems(list);
                MultipleSelectionModel<String> nameModel = contactList.getSelectionModel();
                nameModel.setSelectionMode(SelectionMode.MULTIPLE);
                break;
            }
            case "regok:": {
                registrationPanel.setVisible(false);
                loginPanel.setVisible(true);
                break;
            }
            case "changeNickOk:": {
                changeNicknamePanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            }
            case "changePasswordOk:": {
                changePasswordPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            }
            case "deleteAccountOk:": {
                deletePanel.setVisible(false);
                loginPanel.setVisible(true);
                break;
            }
            case "Connection broke": {
                try {
                    chatMessageService.getNetworkService().getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loginPanel.setVisible(true);
                mainChatPanel.setVisible(false);
                deletePanel.setVisible(false);
                registrationPanel.setVisible(false);
                changePasswordPanel.setVisible(false);
                changeNicknamePanel.setVisible(false);
                showError(parseMessageArray[1]);
                break;
            }
            default:
                mainChatArea.appendText(message + System.lineSeparator());
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
            char splitterOne = 1000;
            char splitterTwo = 5000;
            String resultMessage = "sendMessage:" + symbol;
            String message = inputField.getText();
            if (message.trim().isEmpty()) return;
            ObservableList<String> selected = contactList.getSelectionModel().getSelectedItems();
            List<String> recipients = new ArrayList<>(selected);
            if (!recipients.contains("Send to All") && recipients.size() != 0) {
                for (String nick : recipients) {
                    resultMessage = resultMessage.concat(nick).concat("" + splitterOne);
                }
            }
            chatMessageService.send(resultMessage + splitterTwo + message);
            String[] array = (resultMessage + splitterTwo + message).split("" + symbol);
            historyService.saveMessageAsLine(array[1]);
        inputField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chatMessageService = new ChatMessageService(this);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public void sendAuth(ActionEvent actionEvent) {
        if (loginField.getText().isBlank() || passwordField.getText().isBlank()) return;
        chatMessageService.connect();
        chatMessageService.send("auth:" + symbol + loginField.getText() + symbol + passwordField.getText());
    }

    public void registration(ActionEvent actionEvent) {
        registrationPanel.setVisible(true);
        loginPanel.setVisible(false);
    }

    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseMessage(message));
    }

    public void registrationAuth(ActionEvent actionEvent) {
        if (loginFieldReg.getText().isBlank() || passwordFieldReg.getText().isBlank() || nickFieldReg.getText().isBlank())
            return;
        chatMessageService.connect();
        chatMessageService.send("reg:" + symbol + loginFieldReg.getText() + symbol + passwordFieldReg.getText() + symbol + nickFieldReg.getText());
    }

    public void changeNickname(ActionEvent actionEvent) {
        changeNicknamePanel.setVisible(true);
        mainChatPanel.setVisible(false);
    }

    public void changePassword(ActionEvent actionEvent) {
        changePasswordPanel.setVisible(true);
        mainChatPanel.setVisible(false);
        loginPanel.setVisible(false);
    }

    public void changeNicknameAuth(ActionEvent actionEvent) {
        if (enterPassword.getText().isBlank() || changeNickname.getText().isBlank())
            return;
        chatMessageService.send("changeNick:" + symbol + enterPassword.getText() + symbol + changeNickname.getText());
    }

    public void changePasswordAuth(ActionEvent actionEvent) {
        if (enterOldPassword.getText().isBlank() || enterNewPassword.getText().isBlank())
            return;
        chatMessageService.send("changePass:" + symbol + enterOldPassword.getText() + symbol + enterNewPassword.getText());
    }

    public void deleteAccount(ActionEvent actionEvent) {
        deletePanel.setVisible(true);
        mainChatPanel.setVisible(false);
    }

    public void deleteAccountAuth(ActionEvent actionEvent) {
        if (passwordFieldForDelete.getText().isBlank()) return;
        chatMessageService.send("del:" + symbol + passwordFieldForDelete.getText());
    }

    public void goBackOnMainChatPanel(ActionEvent actionEvent) {
        changePasswordPanel.setVisible(false);
        deletePanel.setVisible(false);
        changeNicknamePanel.setVisible(false);
        mainChatPanel.setVisible(true);
    }

    public void goBackOnLoginPanel(ActionEvent actionEvent) {
        registrationPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void viewAllMessageHistory(ActionEvent actionEvent) {
        chatMessageService.send("viewAllHistory:" + symbol);
    }

    public void clearMessageHistory(ActionEvent actionEvent) {
        chatMessageService.send("clearAllHistory:" + symbol);
    }
}