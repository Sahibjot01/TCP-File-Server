import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import cache.LRUCache;

public class Server {
    private final ExecutorService threadPool;
    private static final LRUCache cache = new LRUCache(3);

    public Server(int poolSize) {

        AtomicInteger clientCount = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("Client-Thread-" + clientCount.getAndIncrement());
            return thread;
        };
        this.threadPool = Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    public void readFile(PrintWriter toClient, String path) {
        try {
            // check cache first
            byte[] cachedData = cache.get(path);
            if (cachedData != null) {
                // System.out.println("Cache HIT for " + path + " on " +
                // Thread.currentThread().getName());
                toClient.println("Cache HIT for " + path);
                toClient.println(new String(cachedData)); // send cached file content
                return;
            }
            // System.out.println("Cache MISS for " + path + " on " +
            // Thread.currentThread().getName());
            toClient.println("Cache MISS for " + path);

            // read from file
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            // convert to byte[] and store to cache
            byte[] fileBytes = sb.toString().getBytes();
            cache.put(path, fileBytes);
            // send file to client
            toClient.println(sb.toString());
        } catch (IOException ex) {
            toClient.println("Error reading file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void handleClient(Socket clientSocket) {
        Random random = new Random();

        try (Socket socket = clientSocket;
                PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true)) {

            // 🔹 Pick a random file between 1–10
            int fileId = random.nextInt(10) + 1; // 1 to 10
            String filePath = "files/file_" + fileId + ".txt";

            System.out.println(Thread.currentThread().getName() + " requested " + filePath);
            readFile(toSocket, filePath); // Use cache + file logic here

            toSocket.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // cache.printState();
            System.out.println("Socket closed for client: " + clientSocket);
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int poolSize = 5; // Adjust the pool size as needed
        Server server = new Server(poolSize);

        try (
                ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(70000);
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Use the thread pool to handle the client
                server.threadPool.execute(() -> server.handleClient(clientSocket));
            }
        } catch (

        IOException ex) {
            ex.printStackTrace();
        } finally {
            // Shutdown the thread pool when the server exits
            System.out.println("shtting down server");
            server.threadPool.shutdown();
        }
    }
}