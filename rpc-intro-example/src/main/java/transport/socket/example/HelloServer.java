package transport.socket.example;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/17 15:39
 */
@Slf4j
public class HelloServer {

    public void start(int port){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket;
            while ((socket = serverSocket.accept())!= null){
                log.info("client connect");
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                try {
                    //通过输入流读取客户端发送的请求信息
                    Message message = (Message) objectInputStream.readObject();
                    log.info("server receive message:" + message.getContent());
                    //通过输出流发送相应信息
                    message.setContent("new content"+System.currentTimeMillis());
                    objectOutputStream.writeObject(message);
                    objectOutputStream.flush();
                } catch (IOException|ClassNotFoundException e )  {
                    log.error("occur exception:", e);
                }
            }
        } catch (IOException e) {
            log.error("occur exception:", e);
        }
    }

    public static void main(String[] args) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService threadPool = new ThreadPoolExecutor(
                10,
                100,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(100),
                threadFactory
        );
        threadPool.execute(() -> {
            HelloServer helloServer = new HelloServer();
            helloServer.start(6666);
        });

    }
}
