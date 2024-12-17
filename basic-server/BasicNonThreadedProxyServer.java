import java.io.*;
import java.net.*;

// MINI PROJECT 5TH SEM - 1RF22CS019, 1RF22CS025, 1RF22CS035, 1RF22CS035 - AY=2024-2025
// basic proxy server driver class
// 4 methods - main, handle client, extract url, forward request 
public class BasicNonThreadedProxyServer {

    private static final int PORT = 8888;  // running the Proxy server on which port no

    // METHOD 1:  MAIN METHOD
    // open server and handle client connections 
    // RETURNS - void - catches and prints io exception
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { //listen on port 8888 as assigned abovee
            System.out.println("Proxy server running on port " + PORT + "...");

            while (true) {
                // Accept client connection
                Socket clientSocket = serverSocket.accept();
                // handles client request directly without multithreading or caching
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // METHOD 2: HANDLE CLIENT 
    // to handle each client connection
    // 1. read http request and 2. then forwaerd to external server and 3. then send back response
    // example response-  GET http://example.com HTTP/1.1 ---> gotta parse it and forward to server and send back response to client 
    // RETURNS void
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read client request
            String requestLine = inFromClient.readLine();
            System.out.println("Received request: " + requestLine);

            if (requestLine == null || !requestLine.startsWith("GET")) {    // only GET responses
                return; 
            }

            String url = extractURL(requestLine);

            // Forward request to target server and get the response
            String serverResponse = forwardRequestToServer(url);
            if (serverResponse != null) {
                // Send response to client with HTTP/1.1 headers
                outToClient.println("HTTP/1.1 200 OK");
                outToClient.println("Content-Type: text/html");
                outToClient.println("Content-Length: " + serverResponse.length());
                outToClient.println();  // Empty line between headers and body
                outToClient.println(serverResponse);  // Send response to client
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

    // METHOD 3: EXTRACT URL
    // improv - Extract URL from the GET request line
    // parts[0] = GET ---- parts[1] = <URL> ----- parts[2] = HTTP/1.1
    // RETURNS URL part of response 
    private static String extractURL(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts[1];  // Return the requested URL
    }

    // METHOD 4: FORWARD REQUEST TO SERVER
    // Forward the client request to the target server
    // also retrieves the response 
    // RETURNS response from server - or io exception if url is invalid 
    private static String forwardRequestToServer(String url) {
        try {
            // Create a connection to the target server
            // improv -  uri 
            URI uri = URI.create(url);  // Create URI from string --- uri- uniform resource identifier ; url specify location of resource 
            URL targetUrl = uri.toURL();  // Convert URI to URL
            System.out.println("Connecting to target server: " + url);

            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection(); // io exception
            connection.setRequestMethod("GET");

            //improv - to prevent infinite waiting from unreliable connections
            connection.setConnectTimeout(5000);  // Set connection timeout to 5 seconds
            connection.setReadTimeout(5000);     // Set read timeout to 5 seconds

            // Get response from the server
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = inFromServer.readLine()) != null) {
                response.append(inputLine).append("\n");          //line by line 
            }
            inFromServer.close();

            return response.toString();  // Return the server response

        } catch (IOException e) {
            System.err.println("Error forwarding request to server: " + e.getMessage());
            return null;
        }
    }
}

