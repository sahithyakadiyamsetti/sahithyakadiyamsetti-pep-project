import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import Controller.SocialMediaController;
import Model.Message;
import Util.ConnectionUtil;
import io.javalin.Javalin;

public class UpdateMessageTextTest {

    private SocialMediaController controller;
    private HttpClient httpClient;
    private ObjectMapper jsonMapper;
    private Javalin serverApp;

    @Before
    public void initialize() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        controller = new SocialMediaController();
        serverApp = controller.startAPI();
        httpClient = HttpClient.newHttpClient();
        jsonMapper = new ObjectMapper();
        serverApp.start(8080);
        Thread.sleep(1000); // Allow server startup time
    }

    @After
    public void cleanUp() {
        serverApp.stop();
    }

    /**
     * Test updating a message that exists.
     * Should return updated message with HTTP status 200.
     */
    @Test
    public void shouldUpdateMessageSuccessfully() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"message_text\": \"updated message\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        Message expected = new Message(1, 1, "updated message", 1669947792);
        Message actual = jsonMapper.readValue(response.body(), Message.class);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test updating a non-existent message.
     * Should return HTTP 400 with no response body.
     */
    @Test
    public void shouldReturnBadRequestIfMessageNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/2"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"message_text\": \"updated message\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }

    /**
     * Test updating a message with an empty string.
     * Should result in HTTP 400 error and no body.
     */
    @Test
    public void shouldRejectEmptyMessageText() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"message_text\": \"\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }

    /**
     * Test updating a message with more than allowed character limit.
     * Should return HTTP 400 and an empty response.
     */
    @Test
    public void shouldRejectMessageExceedingCharacterLimit() throws IOException, InterruptedException {
        String longMessage = "a".repeat(256); // exceeds typical 255-char limit

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"message_text\": \"" + longMessage + "\"}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }
}
