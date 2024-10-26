import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

// Proxy server class
public class NonThreadedProxyServer {

    private static final int PORT = 8888;  // Proxy server port
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();  // Cache to store responses

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Proxy server running on port " + PORT + "...");

            while (true) {
                // Accept client connection
                Socket clientSocket = serverSocket.accept();
                // Handle client request directly without multithreading
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to handle each client connection
    private static void handleClient(Socket clientSocket) {
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
                // Send cached response with proper HTTP/1.1 headers
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

    // Extract URL from the GET request line
    private static String extractURL(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts[1];  // Return the requested URL
    }

    // Forward the client request to the target server
    // Forward the client request to the target server
    private static String forwardRequestToServer(String url) {
        try {
            // Create a connection to the target server
            URI uri = URI.create(url);  // Create URI from string
            URL targetUrl = uri.toURL();  // Convert URI to URL
            System.out.println("Connecting to target server: " + url);

            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);  // Set connection timeout to 5 seconds
            connection.setReadTimeout(5000);     // Set read timeout to 5 seconds

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
