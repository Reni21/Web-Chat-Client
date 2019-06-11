import entity.Credentials;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static String mainMenuActions =
            "<-------------------------- MAIN MENU -------------------------->" + "\n" +
                    "  in       - \"sign in\"" + "\n" +
                    "  up       - \"sign up\"" + "\n" +
                    "  skip     - \"remove progress\"" + "\n" +
                    "  help     - \"show menu commands\"" + "\n" +
                    "  q        - \"quite the app\"" + "\n" +
                    "<--------------------------------------------------------------->" + "\n";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        startApp(scanner);
        scanner.close();

    }

    public static void startApp(Scanner scanner) throws IOException {
        System.out.println(mainMenuActions);

        while (true) {
            System.out.println("Choose one of menu commands...");
            String action = scanner.nextLine();
            boolean isReqSuccess;
            String[] loginAndPass;

            switch (action) {
                case "in":
                    loginAndPass = getLoginAndPass(scanner);
                    if (loginAndPass == null){
                        break;
                    }
                    signIn(scanner, loginAndPass);
                    break;
                case "up":
                    loginAndPass = getLoginAndPass(scanner);
                    if (loginAndPass == null){
                        break;
                    }
                    isReqSuccess = signUpReq(loginAndPass[0], loginAndPass[1]);
                    if (isReqSuccess) {
                        signIn(scanner, loginAndPass);
                    }
                    break;
                case "help":
                    System.out.println(mainMenuActions);
                    break;
                case "q":
                    return;
                default:
                    System.out.println(String.format("Unknown command %s", action));
                    break;
            }
        }

    }

    private static String[] getLoginAndPass(Scanner scanner){
        System.out.println("Enter login >>");
        String login = scanner.nextLine();
        if(login.equals("skip")){
            return null;
        }
        System.out.println("Enter password >>");
        String pass = scanner.nextLine();
        if(pass.equals("skip")){
            return null;
        }
        if (login.isEmpty() || pass.isEmpty()) {
            System.out.println("Fill all fields for sign in!");
            return getLoginAndPass(scanner);
        }
        return new String[]{login, pass};
    }

    private static void signIn(Scanner scanner, String[] loginAndPass) throws IOException {
        String sessionId = signInReq(loginAndPass[0], loginAndPass[1]);
        if (sessionId != null) {
            Chat.enterCommonChat(scanner, loginAndPass[0],sessionId);
        }
        System.out.println(mainMenuActions);
    }

    private static String signInReq( String login, String pass) throws IOException {
        Credentials credentials = new Credentials(login, pass);
        CodeMessage  serverResp = Utils.sendPostReqAndGetResp(Utils.getURL() + "/signIn", credentials);

        if (serverResp.getResponseCode() == 404) {
            System.out.println("Such user does not exist!");
            return null;
        } else if (serverResp.getResponseCode() != 200) {
            System.out.println("HTTP error occured: " + serverResp);
            return null;
        }
        return serverResp.getResponseMessage();
    }

    private static boolean signUpReq(String login, String pass) throws IOException {
        Credentials credentials = new Credentials(login, pass);
        int serverResp = Utils.sendPostReq(Utils.getURL() + "/signUp", credentials);
        if (serverResp == 409) {
            System.out.println("Login is already taken!");
            return false;
        } else if (serverResp != 200) {
            System.out.println("HTTP error occured: " + serverResp);
            return false;
        }
        return true;
    }
}
