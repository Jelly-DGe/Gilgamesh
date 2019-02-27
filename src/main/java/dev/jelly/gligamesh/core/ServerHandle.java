package dev.jelly.gligamesh.core;

import dev.jelly.gligamesh.pojo.RequestReport;
import dev.jelly.gligamesh.pojo.ServerConfig;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerHandle implements Runnable {
    private Socket socket;

    /**
     * @param socket http请求传入的socket对象
     */
    public ServerHandle(Socket socket) {
        this.socket = socket;
    }

    /**
     * 无参构造
     */
    public ServerHandle() {
    }

    @Override
    public void run() {
        try {
            //获取http发起的socket中的输入输出流
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            //实例化请求体
            RequestReport requestReport = new RequestReport(inputStream);
            String method = requestReport.getMethod();
            String url = requestReport.getUrl();

            //对可能携带的参数进行处理
            int index = url.indexOf("?");
            String webPath = "src/main/web";
            String urlPath = webPath + (index == -1 ? url : url.substring(0, index));

            //状态码和信息
            int statusCode;
            String statusMessage;

            File file = new File(urlPath);
            if (file.exists() && !file.isDirectory()) {
                //获取最后一次修改时间
                long lastModified = file.lastModified();
                String ifModifiedSinceString = requestReport.getHeaders().get("If-Modified-Since");
                boolean useCache = false;
                if (ifModifiedSinceString != null) {
                    long l = Long.parseLong(ifModifiedSinceString);
                    //文件是否修改过
                    useCache = (lastModified == l);
                }

                //返回响应体
                String responseReport;
                if (useCache) {
                    statusCode = 304;
                    statusMessage = "Not Modified";
                    responseReport = "HTTP/1.1 304 Not Modified\r\nServer: " + ServerConfig.DISPLAY_SERVER_NAME + "\r\n\r\n";
                    outputStream.write(responseReport.getBytes());
                } else {
                    statusCode = 200;
                    statusMessage = "OK";
                    responseReport = "HTTP/1.1 200 OK\r\nLast-Modified: " + lastModified + "\r\nServer: " + ServerConfig.DISPLAY_SERVER_NAME + "\r\n\r\n";
                    outputStream.write(responseReport.getBytes());
                    outputStream.flush();
                    InputStream in = new FileInputStream(file);
                    byte[] buffer = new byte[102400];
                    int len = 0;
                    while ((len = in.read(buffer, 0, 102400)) != -1) {
                        outputStream.write(buffer, 0, len);
                        outputStream.flush();
                    }
                    in.close();
                }
            } else {
                statusCode = 404;
                statusMessage = "Not Found";
                String content = "<html><head><title>Matrix</title></head><body><p>Page Not Found!</p></body></html>";
                String responseReport = "HTTP/1.1 404 Not Found\r\nDate: " + new Date() + "\r\nServer: " + ServerConfig.DISPLAY_SERVER_NAME + "\r\nContent-Type: text/html;charset=UTF-8\r\n\r\n" + content;
                outputStream.write(responseReport.getBytes());
            }
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
