import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anyuan on 2017/6/4.
 */
public class HttpExample {

    private static String getRandomString(int n) {
        String alp = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            str.append(alp.charAt(random.nextInt(61)));
        }
        return str.toString();
    }

    private static String isFileData(String value) {
        Pattern r = Pattern.compile("^f'(.*)'$");
        Matcher m = r.matcher(value);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private static String getFileType(String file_path) {
        File f = new File(file_path);
        String contentType = new MimetypesFileTypeMap().getContentType(f);
        if (contentType == null || contentType.equals("")) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    private static byte[] readFileContent(String file_path) {
        FileChannel fc = null;
        byte[] result = null;
        try {
            fc = new RandomAccessFile(file_path, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.PRIVATE, 0, fc.size()).load();
            result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fc != null;
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static byte[] bytesArrayJoinWith(List<byte[]> list, byte[] crlf) {
        int length = 0;
        for (byte[] bytes :
                list) {
            length += bytes.length;
        }
        length += crlf.length * (list.size() - 1);
        byte[] ret = new byte[length];
        int pos = 0;
        int tail = 0;
        for (byte[] bytes :
                list) {
            tail = pos + bytes.length + crlf.length;
            System.arraycopy(bytes, 0, ret, pos, bytes.length);
            if (tail >= length) {
                System.arraycopy(crlf, 0, ret, pos + crlf.length, crlf.length);
            }
            pos = tail;
        }
        return ret;
    }

    private static Map<String, byte[]> multiEncode(Map<String, String> params) {
        Map<String, byte[]> ret = new HashMap<>();
        String BOUNDARY = "--------" + getRandomString(7);
        byte[] CRLF = "\r\n".getBytes();
        List<byte[]> L = new ArrayList<>();
        String file_path;
        for (Map.Entry<String, String> entry :
                params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            file_path = isFileData(value);
            if (file_path != null) {
                L.add(("--" + BOUNDARY).getBytes());
                L.add(("Content-Disposition: form-data; name=" + key + "; filename=" + file_path).getBytes());
                L.add(("Content-Type:" + getFileType(file_path)).getBytes());
                L.add("".getBytes());
                L.add(readFileContent(file_path));
            } else {
                L.add(("--" + BOUNDARY).getBytes());
                L.add(("Content-Disposition: form-data; name=" + key).getBytes());
                L.add("".getBytes());
                L.add(value.getBytes());
            }
            L.add(("--" + BOUNDARY + "--").getBytes());
            L.add("".getBytes());
        }
        byte[] body = bytesArrayJoinWith(L, CRLF);
        byte[] content_type = ("'Content-Type':'multipart/form-data; boundary=" + BOUNDARY + "'").getBytes();
        ret.put("body", body);
        ret.put("content_type", content_type);
        return ret;
    }


    public static void main(String[] args) {
        String url = "http://example.com";
        String file_name = "f'poster.jpg'";
        Map<String, String> params = new HashMap<>();
        Map<String, String> header = new HashMap<>();
        params.put("file", file_name);
        Map<String, byte[]> data = multiEncode(params);
        byte[] body = data.get("body");
        byte[] content_type = data.get("content_type");


        params.clear();
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Connection", "keep-alive");
        header.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        header.put("Content-Type", Arrays.toString(content_type));

        HttpSender.sendPost(url, params, body, header);

    }
}
