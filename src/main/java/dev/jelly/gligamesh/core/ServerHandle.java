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
     * @param socket 传入服务器Socket侦测连接
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
            //获取请求方法
            String method = requestReport.getMethod();
            String url = requestReport.getUrl();

            //对可能携带的参数进行处理
            int index = url.indexOf("?");
            String webPath = "src/main/web";
            String urlPath = webPath + (index == -1 ? url : url.substring(0, index));
            //处理默认访问,设为index.html
            if (urlPath.equals(webPath + "/"))
                urlPath = webPath + "/index.html";

            File file = new File(urlPath);
            if (file.exists() && !file.isDirectory()) {
                //获取最后一次修改时间
                long lastModified = file.lastModified();
                String ifModifiedSinceString = requestReport.getHeaders().get("If-Modified-Since");
                //是否使用浏览器缓存中的数据
                boolean useCache = false;
                if (ifModifiedSinceString != null) {
                    long l = Long.parseLong(ifModifiedSinceString);
                    //文件被修改,重新获取
                    useCache = (lastModified == l);
                }

                //返回响应体
                String responseReport;
                if (useCache) {
                    responseReport = "HTTP/1.1 304 Not Modified\r\nServer: " + ServerConfig.DISPLAY_SERVER_NAME + "\r\n\r\n";
                    outputStream.write(responseReport.getBytes());
                } else {
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
                String content = "<html><head><title>Gligemesh-Server</title></head><body><p>404!</p></body></html>";
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
