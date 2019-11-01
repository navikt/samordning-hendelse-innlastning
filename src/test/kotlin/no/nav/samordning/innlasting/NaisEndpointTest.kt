package no.nav.samordning.innlasting

import org.junit.jupiter.api.Assertions.assertEquals
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal object NaisEndpointTest {

    private val client = HttpClient.newHttpClient()

    private const val LOCALHOST = "http://localhost:"
    private const val DEFAULT_PORT = "8080"
    private const val LIVENESS_ENDPOINT = "isAlive"
    private const val READINESS_ENDPOINT = "isReady"
    private const val METRICS_ENDPOINT = "metrics"
    private const val HTTP_OK = 200

    @Throws(Exception::class)
    fun isAlive_endpoint_returns_200_OK_when_application_runs() {
        val request = createRequest(LIVENESS_ENDPOINT)
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(HTTP_OK, response.statusCode())
    }

    @Throws(Exception::class)
    fun isReady_endpoint_returns_200_OK_when_application_runs() {
        val request = createRequest(READINESS_ENDPOINT)
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(HTTP_OK, response.statusCode())
    }

    @Throws(Exception::class)
    fun metrics_endpoint_returns_200_OK_when_application_runs() {
        val request = createRequest(METRICS_ENDPOINT)
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(HTTP_OK, response.statusCode())
    }

    private fun createRequest(endpoint: String) = HttpRequest.newBuilder()
            .uri(URI.create("$LOCALHOST$DEFAULT_PORT/$endpoint"))
            .GET()
            .build()

}
