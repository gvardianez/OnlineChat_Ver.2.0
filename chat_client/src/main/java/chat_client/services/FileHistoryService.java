package chat_client.services;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHistoryService {
    private final String path;
    private final File history;
    private final StringBuilder stringHistory;
    private final int valueOfSaveMessages;
    private List<String> allMessages;
    private final char symbol = 3000;

    public FileHistoryService(String currentNick, String pathToSaveFile, int valueOfSaveRaw) {
        this.path = pathToSaveFile + "/history_[" + currentNick + "].txt";
        history = new File(path);
        stringHistory = new StringBuilder();
        this.valueOfSaveMessages = valueOfSaveRaw;
        allMessages = new ArrayList<>();
        checkFile();
    }

    public void checkFile() {
        if (!history.exists()) {
            try {
                history.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMessageRaw(String message) {
        String messageForSave = prepareMessage(message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(messageForSave + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMessageAsLine(String message) {
        String messageForSave = prepareMessage(message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(messageForSave + symbol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prepareMessage(String message) {
        StringBuilder messageForSave = new StringBuilder();
        char splitterOne = 1000;
        char splitterTwo = 5000;
        String[] parsingMessage = message.split("" + splitterTwo);
        String[] nicksRecipients = parsingMessage[0].split("" + splitterOne);
        if (nicksRecipients.length == 0) {
            messageForSave.append("(To All): ");
        } else {
            messageForSave.append("(To: ");
            for (int i = 1; i < nicksRecipients.length; i++) {
                if (i == nicksRecipients.length - 1) {
                    messageForSave.append(nicksRecipients[i]).append("): ");
                    continue;
                }
                messageForSave.append(nicksRecipients[i]).append(" ,");
            }
        }
        messageForSave.append(parsingMessage[1]);
        return messageForSave.toString();
    }

    private void checkHistory() {               //проверка не хранится ли сообщений, больше чем задано и заодно обновляем лист сообщений
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            allMessages = new ArrayList<>(Arrays.asList(reader.readLine().split("" + symbol)));
            if (allMessages.size() > valueOfSaveMessages) {
                allMessages.subList(0, allMessages.size() - valueOfSaveMessages).clear();
                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                StringBuilder message = new StringBuilder();
                for (String allMessage : allMessages) {
                    message.append(allMessage).append(symbol);
                }
                writer.write(message.toString());
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadHistory(int valueOfLoadRaw) {
        checkHistory();
        int valueOfRaw = allMessages.size();
        if (valueOfRaw < valueOfLoadRaw) {
            for (String allMessage : allMessages) {
                stringHistory.append(allMessage).append(System.lineSeparator());
            }
        } else
            for (int i = (valueOfRaw - valueOfLoadRaw); i < valueOfRaw; i++) {
                stringHistory.append(allMessages.get(i)).append(System.lineSeparator());
            }
        return stringHistory.toString();
    }

}
