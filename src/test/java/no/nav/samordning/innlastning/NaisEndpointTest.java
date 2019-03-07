package no.nav.samordning.innlastning;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NaisEndpointTest {

    private static final HttpClient client = HttpClient.newHttpClient();

    private static final String LOCALHOST = "http://localhost:";
    private static final String DEFAULT_PORT = "8080";
    private static final String LIVENESS_ENDPOINT = "isAlive";
    private static final String READINESS_ENDPOINT = "isReady";
    private static final String METRICS_ENDPOINT = "metrics";
    private static final int HTTP_OK = 200;

    static void isAlive_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(LIVENESS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HTTP_OK, response.statusCode());
    }

    static void isReady_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(READINESS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HTTP_OK, response.statusCode());
    }

    static void metrics_endpoint_returns_200_OK_when_application_runs() throws Exception {
        HttpRequest request = createRequest(METRICS_ENDPOINT);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HTTP_OK, response.statusCode());
    }

    private static HttpRequest createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(LOCALHOST + DEFAULT_PORT + "/" + endpoint))
                .GET()
                .build();
    }

}
