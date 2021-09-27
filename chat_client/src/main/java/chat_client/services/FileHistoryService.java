package chat_client.services;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHistoryService {
    private final String path;
    private final File history;
    private final StringBuilder stringHistory;
    private final String pathDirectory;
    private final int valueOfSaveRaw;
    private final int valueOfLoadRaw;

    public FileHistoryService(String currentNick, String pathToSaveFile, int valueOfSaveRaw, int valueOfLoadRaw) {
        this.pathDirectory = pathToSaveFile;
        this.path = pathToSaveFile + "/history_[" + currentNick + "].txt";
        history = new File(path);
        stringHistory = new StringBuilder();
        this.valueOfSaveRaw = valueOfSaveRaw;
        this.valueOfLoadRaw = valueOfLoadRaw;
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

    public void saveMessage(String message) {
        String messageForSave = prepareMessage(message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path,true))) {
            writer.write(messageForSave + System.lineSeparator());
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
        if (nicksRecipients.length == 1) {
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

    public String loadHistory(int valueOfLoadRaw) {
        List<String> allMessages = new ArrayList<>();
        List<String> saveMessages = new ArrayList<>();
        String raw;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while ((raw = reader.readLine()) != null) {
                allMessages.add(raw);
            }
            int valueOfRaw = allMessages.size();
            if (valueOfRaw > valueOfSaveRaw) {
                File tmp = File.createTempFile("tmp", "", new File(pathDirectory));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
                for (int i = 0; i < valueOfRaw; i++) {
                    if (i >= (valueOfRaw - valueOfSaveRaw)) {
                        writer.write(allMessages.get(i) + System.lineSeparator());
                        saveMessages.add(allMessages.get(i));
                    }
                }
                writer.close();
                reader.close();
                valueOfRaw = valueOfSaveRaw;
                if (history.delete()) tmp.renameTo(history);
            } else saveMessages = allMessages;
            if (valueOfRaw < valueOfLoadRaw) {
                for (int i = 0; i < valueOfRaw; i++) {
                    stringHistory.append(saveMessages.get(i)).append(System.lineSeparator());
                }
            } else
                for (int i = (valueOfRaw - valueOfLoadRaw); i < saveMessages.size(); i++) {
                    stringHistory.append(saveMessages.get(i)).append(System.lineSeparator());
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringHistory.toString();
    }

}
