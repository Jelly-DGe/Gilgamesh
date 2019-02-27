import dev.jelly.gligamesh.core.ServerHandle;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartServer {
    private static int SERVER_PORT = 8080;
    private static int THREAD_NUMBERS = 100;

    public static void main(String[] args) {
        try {
            new StartServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动服务
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBERS);
        while (true) {
            //始终保持侦测连接
            Socket socket = serverSocket.accept();
            ServerHandle serverHandle = new ServerHandle(socket);
            executor.execute(serverHandle);
        }
    }
}
