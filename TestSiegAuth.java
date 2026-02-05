import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TestSiegAuth {
    public static void main(String[] args) {
        String clientId = args.length > 0 ? args[0] : "YOUR_CLIENT_ID";
        String secretKey = args.length > 1 ? args[1] : "YOUR_SECRET_KEY";

        System.out.println("=== Testing SIEG API Authentication ===");
        System.out.println("Client ID: " + clientId);
        System.out.println("Secret Key: " + secretKey.substring(0, Math.min(10, secretKey.length())) + "...");

        // Test 1: Original approach with POST empty string
        System.out.println("\n--- Test 1: POST with ofString(\"\") and explicit Content-Length: 0 ---");
        testAuth1(clientId, secretKey);

        // Test 2: POST with noBody()
        System.out.println("\n--- Test 2: POST with noBody() ---");
        testAuth2(clientId, secretKey);

        // Test 3: POST without setting any body
        System.out.println("\n--- Test 3: POST with ofByteArray(new byte[0]) ---");
        testAuth3(clientId, secretKey);
    }

    private static void testAuth1(String clientId, String secretKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sieg.com/api/v1/create-jwt"))
                    .header("X-Client-Id", clientId)
                    .header("X-Secret-Key", secretKey)
                    .header("Content-Length", "0")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
            System.out.println("Headers: " + response.headers().map());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testAuth2(String clientId, String secretKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sieg.com/api/v1/create-jwt"))
                    .header("X-Client-Id", clientId)
                    .header("X-Secret-Key", secretKey)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testAuth3(String clientId, String secretKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sieg.com/api/v1/create-jwt"))
                    .header("X-Client-Id", clientId)
                    .header("X-Secret-Key", secretKey)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(new byte[0]))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
