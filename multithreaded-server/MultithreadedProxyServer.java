import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// MINI PROJECT 5TH SEM - 1RF22CS019, 1RF22CS025, 1RF22CS035, 1RF22CS035 - AY=2024-2025
// proxy server driver class
public class MultithreadedProxyServer {
    
    private static final int PORT = 8888;  // Proxy server port no - used in comp
    private static final int THREAD_POOL_SIZE = 10;  // Number of threads in ready pool 
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>(100);  // improv - conc hashmap cache to store responses
    // key ---> value = http req ---> response

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);  // util , helps maintain task queue for pool

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Proxy server running on port " + PORT + "...");

            while (true) {
                // Accept client connection
                Socket clientSocket = serverSocket.accept();
                // Submit client handling to the thread pool - add to queue
                threadPool.submit(new ClientHandler(clientSocket)); // client handler-- custom class see below
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    // improv - Client handler class (Runnable for multithreading) - interface better than class 
    static class ClientHandler implements Runnable {
        private Socket clientSocket; 

        public ClientHandler(Socket clientSocket) { // construictor - confirm  
            this.clientSocket = clientSocket;
            System.out.println("Accepted connection from client: " + clientSocket.getInetAddress()); 
        }

        @Override
public void run() {
    try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true)) {

        // Read client request
        String requestLine = inFromClient.readLine();
        System.out.println("Received request: " + requestLine);

        if (requestLine == null || !requestLine.startsWith("GET")) {
            return;  // Handle only GET requests
        }

        String url = extractURL(requestLine);

        // Check the cache
        String cachedResponse = cache.get(url);
        if (cachedResponse != null) {
            System.out.println("Cache hit for: " + url);

            // major improv --- Send cached response with proper HTTP/1.1 headers
            outToClient.println("HTTP/1.1 200 OK");
            outToClient.println("Content-Type: text/html");
            outToClient.println("Content-Length: " + cachedResponse.length());
            outToClient.println();  // Empty line between headers and body
            outToClient.println(cachedResponse);  // Send cached response
        } else {
            // Forward request to target server and get the response
            String serverResponse = forwardRequestToServer(url);
            if (serverResponse != null) {
                // Send response to client with HTTP/1.1 headers
                outToClient.println("HTTP/1.1 200 OK");
                outToClient.println("Content-Type: text/html");
                outToClient.println("Content-Length: " + serverResponse.length());
                outToClient.println();  // Empty line between headers and body
                outToClient.println(serverResponse);  // Send response to client
                // Cache the response
                synchronized (cache) {
                    cache.put(url, serverResponse);
                }
            }
        }

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


        // improv - Extract URL from the GET request line
        private String extractURL(String requestLine) {
            String[] parts = requestLine.split(" ");
            return parts[1];  // Return the requested URL
        }

        // Forward the client request to the target server 
        private String forwardRequestToServer(String url) {
            try {
                // Create a connection to the target server - uri coz of HTTP 1.1
                URI uri = URI.create(url);  // Create URI from string --- uri- uniform resource identifier ; url specify location of resource 
                URL targetUrl = uri.toURL();  // Convert URI to URL

                HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
                connection.setRequestMethod("GET");

                // Get response from the server 
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = inFromServer.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                inFromServer.close();

                return response.toString();  // Return the server response

            } catch (IOException e) {
                System.err.println("Error forwarding request to server: " + e.getMessage());
                return null;
            }
        }
    }
}