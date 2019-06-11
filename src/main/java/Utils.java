import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Utils { // формирование url
    private static final String URL = "http://127.0.0.1";
    private static final int PORT = 8080;

    public static String getURL() {
        return URL + ":" + PORT;
    }

    public static String sendGetReq(String path, String sessionId) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cookie", sessionId);

        try (InputStream is = conn.getInputStream()) {
            byte[] buf = requestBodyToArray(is);
            return new String(buf, StandardCharsets.UTF_8);
        }
    }


    public static <T> int sendDeleteReq(String url, String sessId) throws IOException {
        java.net.URL objUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) objUrl.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Cookie", sessId);
        return conn.getResponseCode();
    }


    public static <T> int sendPostReq(String url, T object) throws IOException {
        return sendPostReq(url, object, null);
    }

    public static <T> int sendPostReq(String url, T object, String sessId) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        if (sessId != null) {
            conn.setRequestProperty("Cookie", sessId);
        }

        try (OutputStream os = conn.getOutputStream()) {
            String objToString;
            if (object.getClass() == String.class) {
                objToString = (String) object;
            } else {
                objToString = Utils.toJSON(object);
            }
            os.write(objToString.getBytes(StandardCharsets.UTF_8));
            return conn.getResponseCode();
        }
    }

    public static CodeMessage sendPostReqAndGetResp(String url, Object object) throws IOException {
        String json = toJSON(object);
        URL objUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) objUrl.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String respMsg = new String(requestBodyToArray(conn.getErrorStream()));
            return new CodeMessage(responseCode, respMsg);

        } else {
            String respMsg = new String(requestBodyToArray(conn.getInputStream()));
            return new CodeMessage(responseCode, respMsg);
        }
    }

    public static void checkRespCode(int respCode) {
        checkRespCode(respCode, null);
    }

    public static void checkRespCode(int respCode, String msgToOutput) {
        if (respCode != 200) {
            System.err.println("HTTP error occured: " + respCode);
        } else{
            if(msgToOutput != null) {
                System.out.println(msgToOutput);
            }
        }
    }

    public static <T> T fromJSON(String s, Class<T> clazz) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(s, clazz);
    }

    private static byte[] requestBodyToArray(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];
        int r;

        do {
            r = is.read(buf);
            if (r > 0) bos.write(buf, 0, r);
        } while (r != -1);

        return bos.toByteArray();
    }

    private static <T> String toJSON(T object) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(object);
    }


}
