package dev.jelly.gligamesh.pojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RequestReport {
    private BufferedReader reader;
    private String version;
    private String url;
    private String method;
    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * @param inputStream http请求输入流
     */
    public RequestReport(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String temp = reader.readLine();
            //第一行空格间隔
            String[] firstLine = temp.split(" ");
            this.method = firstLine[0];
            this.url = firstLine[1];
            this.version = firstLine[2];
            stringBuilder.append(temp + "\r\n");
            while (!(temp = reader.readLine()).equals("")) {
                int index;
                if ((index = temp.indexOf(':')) != -1) {
                    String key = temp.substring(0, index);
                    String value = temp.substring(index + 1).trim();
                    headers.put(key, value);
                }
                stringBuilder.append(temp + "\r\n");
            }
            System.out.println(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 无参构造
     * @param i
     * @param abc
     */
    public RequestReport(int i, String abc) {
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
