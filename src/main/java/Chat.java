import entity.Message;

import java.io.IOException;
import java.util.Scanner;

public class Chat {
    private static String chatMenuActions =
            "<-------------------------- CHAT MENU -------------------------->" + "\n" +
                    "  ?u                      - \"show online users list\"" + "\n" +
                    "  @username + your_text   - \"send private msg\"" + "\n" +
                    "  st + your_text          - \"set status\"" + "\n" +
                    "  ?st + username          - \"show user status\"" + "\n" +
                    "  new#grupname            - \"create new group\"" + "\n" +
                    "  ?gr                     - \"show group list\"" + "\n" +
                    "  join#grupname           - \"show group list\"" + "\n" +
                    "  #grupname + your_text   - \"send msg to this group\"" + "\n" +
                    "  q#grupname              - \"quite the group" + "\n" +
                    "  del#grupname            - \"delete group (for admin)" + "\n" +
                    "  help                    - \"show menu commands\"" + "\n" +
                    "  q                       - \"quite the chat\"" + "\n" +
                    "<--------------------------------------------------------------->" + "\n";

    public static void enterCommonChat(Scanner scanner, String login, String sessionId) throws IOException {
        System.out.println(chatMenuActions);
        System.out.println(String.format("User %s entered the chat...", login));

        Thread th = new Thread(new GetThread(sessionId, login));
        th.setDaemon(true);
        th.start();

        try {
            while (true) {
                String text = scanner.nextLine();
                if (text.isEmpty()) break;

                if (text.equals("q")) { // quit chat
                    int respCode = Utils.sendDeleteReq(Utils.getURL() + "/session", sessionId);
                    Utils.checkRespCode(respCode);
                    th.interrupt();
                    System.out.println(String.format("User %s leave the chat...", login));
                    return;

                } else if (text.equals("help")) { // show menu
                    System.out.println(chatMenuActions);

                } else if (text.equals("?u")) { // shoe online users
                    String onlineUsers = Utils.sendGetReq(Utils.getURL() + "/session", sessionId);
                    System.out.println("Now online >>" + "\n" + onlineUsers);

                } else if (text.startsWith("st ")) { // set status
                    String status = text.substring(text.indexOf(" ") + 1);
                    int respCode = Utils.sendPostReq(Utils.getURL() + "/user", status, sessionId);
                    Utils.checkRespCode(respCode, login + " set status: " + status);

                } else if (text.startsWith("?st ")) { // show status
                    String userLogin = text.substring(text.indexOf(" ") + 1);
                    String status = Utils.sendGetReq(Utils.getURL() + "/user?login=" + userLogin, sessionId);
                    System.out.println(userLogin + " status: " + status);

                } else if (text.equals("?gr")) { // show group list
                    String onlineUsers = Utils.sendGetReq(Utils.getURL() + "/group", sessionId);
                    System.out.println("Private groups >>" + "\n" + onlineUsers);

                } else if (text.startsWith("new#") && text.charAt(4) != ' ') { // create group
                    createGroup(login, sessionId, text);

                } else if (text.startsWith("@") && text.charAt(1) != ' ') { // send private msg
                    String sendTo = text.substring(1, text.indexOf(" "));
                    sendMsg(login, sendTo, text.substring(text.indexOf(" ") + 1), sessionId);

                } else if (text.startsWith("#") && text.charAt(1) != ' ') { // send msg to the group
                    String sendTo = text.substring(0, text.indexOf(" "));
                    sendMsg(login, sendTo, text.substring(text.indexOf(" ") + 1), sessionId);

                } else if (text.startsWith("join#") && text.charAt(5) != ' ') { // join group
                    joinGroup(login, sessionId, text);

                } else if (text.startsWith("q#") && text.charAt(2) != ' ') {
                    quiteGroup(login, sessionId, text);

                } else if (text.startsWith("del#") && text.charAt(4) != ' ') {
                    deleteGroup(login, sessionId, text);

                } else { // send msg to common chat
                    sendMsg(login, "All", text, sessionId);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void joinGroup(String login, String sessionId, String text) throws IOException {
        String groupName = text.substring(text.indexOf("#") + 1);
        int respCode = Utils.sendPostReq(Utils.getURL() + "/access", groupName, sessionId);
        if (respCode == 404) {
            System.out.println("Group does not exist!");
        } else {
            Utils.checkRespCode(respCode, "User " + login + " join the group...");
        }

    }

    private static void quiteGroup(String login, String sessionId, String text) throws IOException {
        String groupName = text.substring(text.indexOf("#") + 1);
        int respCode = Utils.sendDeleteReq(Utils.getURL() + "/access?group=" + groupName, sessionId);
        if (respCode == 404) {
            System.out.println("Group does not exist!");
        } else {
            Utils.checkRespCode(respCode, "User " + login + " leave the group...");
        }

    }

    private static void createGroup(String login, String sessionId, String text) throws IOException {
        String groupName = text.substring(text.indexOf("#") + 1);
        int respCode = Utils.sendPostReq(Utils.getURL() + "/group", groupName, sessionId);
        if (respCode == 409) {
            System.out.println("Group name is already taken!");
        } else {
            Utils.checkRespCode(respCode, login + " create group: " + groupName);
        }
    }

    private static void deleteGroup(String login, String sessionId, String text) throws IOException {
        String groupName = text.substring(text.indexOf("#") + 1);
        int respCode = Utils.sendDeleteReq(Utils.getURL() + "/group?group=" + groupName, sessionId);
        if (respCode == 409) {
            System.out.println("Not enough rights for this action!");
        } else {
            Utils.checkRespCode(respCode, "User " + login + " delete the group...");
        }

    }

    private static void sendMsg(String login, String sendTo, String text, String sessionId) throws IOException {
        Message m = new Message(login, text);
        m.setTo(sendTo);
        int respCode = Utils.sendPostReq(Utils.getURL() + "/add", m, sessionId);
        Utils.checkRespCode(respCode);
    }
}
