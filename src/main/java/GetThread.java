import entity.Message;

import java.util.HashMap;
import java.util.Map;

public class GetThread implements Runnable {
    private final String sessionId;
    private final String login;
    private int commonIndex;
    private int privateIndex;
    private final Map<String, Integer> groupsIndexes = new HashMap<>();

    public GetThread(String sessionId, String login) {
        this.sessionId = sessionId;
        this.login = login;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                String jsonStr = Utils.sendGetReq(createPath(), sessionId);
                JsonMessages list = Utils.fromJSON(jsonStr, JsonMessages.class);
                printMsgAndIncrementIndex(list);
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

    private String createPath() {
        String path = Utils.getURL() +
                "/get?comFrom=" + commonIndex +
                "&privFrom=" + privateIndex;
        if(!groupsIndexes.isEmpty()) {
            for (String key : groupsIndexes.keySet()) {
                path = path + "&" + key.substring(1) + "=" + groupsIndexes.get(key);
            }
        }
        return path;
    }

    private void printMsgAndIncrementIndex(JsonMessages list) {
        if (list == null) {
            return;
        }

        for (Message msg : list.getList()) {
            if (msg.getTo().equals("All")) {
                System.out.println(msg);
                commonIndex++;
            } else if (msg.getTo().equals(login)) {
                msg.setTo("You");
                System.out.println(msg);
                privateIndex++;
            } else if (msg.getFrom().equals(login) && !msg.getTo().startsWith("#")) {
                msg.setFrom("You");
                System.out.println(msg);
                privateIndex++;
            } else {
                String groupName = msg.getTo();
                msg.setTo(msg.getTo().substring(1));
                if (groupsIndexes.containsKey(groupName)) {
                    System.out.println(msg);
                    groupsIndexes.put(groupName, (groupsIndexes.get(groupName) + 1));
                } else {
                    System.out.println(msg);
                    groupsIndexes.put(groupName, 1);
                }
            }
        }
    }
}
