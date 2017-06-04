import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by anyuan on 2017/6/4.
 */


public class HttpSender {

    /**
     * stringify params
     *
     * @param params url params
     * @return string of params
     */
    private static String urlParse(Map<String, String> params) {
        StringBuilder stringify_url = new StringBuilder("?");
        for (Map.Entry<String, String> entry :
                params.entrySet()) {
            stringify_url.append(entry.getKey());
            stringify_url.append("=");
            stringify_url.append(entry.getValue());
            stringify_url.append("&");
        }
        stringify_url.deleteCharAt(stringify_url.length());
        System.out.println(stringify_url);
        return stringify_url.toString();
    }

    /**
     * get data from a url
     *
     * @param url    the url to request
     * @param params url params
     * @return res the response of server
     */
    public static String sendGet(String url, Map<String, String> params, Map<String, String> Header) {
        String res = "";
        BufferedReader in = null;
        try {
            // generate url link
            String encoded_url = url + urlParse(params);
            URL realUrl = new URL(encoded_url);

            // to get url connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) realUrl.openConnection();

            // set some request headers
            for (Map.Entry<String, String> entry :
                    Header.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(),entry.getValue());
            }

            // connect(actually it doesn't send any data)
            httpURLConnection.connect();

            // define some streams
            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                res += line;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        // close the stream
        finally {
            try {
                assert in != null;
                in.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return res;
    }

    /**
     * post a data to a url
     *
     * @param url    url to post
     * @param params url params
     * @return response of post result
     */
    static String sendPost(String url, Map<String, String> params, byte[] data, Map<String, String> Header) {
        OutputStream out = null;
        BufferedReader in = null;
        String res = "";
        try {
            String encoded_url = urlParse(params);
            URL realUrl = new URL(url + encoded_url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            for (Map.Entry<String, String> entry :
                    Header.entrySet()) {
                conn.setRequestProperty(entry.getKey(),entry.getValue());
            }
            // must be set if post
            conn.setDoOutput(true); //default false
            conn.setDoInput(true); //default true
            conn.setUseCaches(false); //mostly post doesn't use cache
            conn.setRequestMethod("POST");

            out = conn.getOutputStream();
            //sending data to post(where different from get)
            out.write(data);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                res += line;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } finally {
            try {
                assert out != null;
                out.close();
                assert in != null;
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

}
