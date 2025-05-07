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

public class CreateMessageTest {
    SocialMediaController socialMediaController;
    HttpClient webClient;
    ObjectMapper objectMapper;
    Javalin app;

    /**
     * Initializes the server, database, and utilities before each test runs.
     */
    @Before
    public void setUp() throws InterruptedException {
        ConnectionUtil.resetTestDatabase(); // Reset DB to clean state
        socialMediaController = new SocialMediaController();
        app = socialMediaController.startAPI();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // Wait for the server to start
    }

    /**
     * Stops the API server after each test.
     */
    @After
    public void tearDown() {
        app.stop();
    }

    /**
     * Test case: Submitting a valid message via POST should return 200 and a JSON message response.
     */
    @Test
    public void createMessageSuccessful() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1," +
                        "\"message_text\":\"hello message\"," +
                        "\"time_posted_epoch\":1669947792}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        Message expected = new Message(2, 1, "hello message", 1669947792);
        Message actual = objectMapper.readValue(response.body(), Message.class);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test case: Sending an empty message should result in a 400 Bad Request.
     */
    @Test
    public void createMessageWithEmptyText() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1," +
                        "\"message_text\":\"\"," +
                        "\"time_posted_epoch\":1669947792}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertEquals("", response.body());
    }

    /**
     * Test case: A message exceeding 254 characters should be rejected with status code 400.
     */
    @Test
    public void createMessageTooLong() throws IOException, InterruptedException {
        String longMessage = "a".repeat(255);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1," +
                        "\"message_text\":\"" + longMessage + "\"," +
                        "\"time_posted_epoch\":1669947792}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertEquals("", response.body());
    }

    /**
     * Test case: Attempting to post a message from a non-existent user should return 400.
     */
    @Test
    public void createMessageWithInvalidUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":3," +
                        "\"message_text\":\"message test\"," +
                        "\"time_posted_epoch\":1669947792}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertEquals("", response.body());
    }
}
