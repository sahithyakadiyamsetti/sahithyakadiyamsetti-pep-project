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

public class DeleteMessageByMessageIdTest {
    SocialMediaController socialMediaController;
    HttpClient webClient;
    ObjectMapper objectMapper;
    Javalin app;

    /**
     * Runs before each test: resets the DB, starts the Javalin server,
     * and sets up HTTP client and JSON mapper.
     */
    @Before
    public void setUp() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        socialMediaController = new SocialMediaController();
        app = socialMediaController.startAPI();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // Allow server to initialize
    }

    /**
     * Shuts down the server after each test case.
     */
    @After
    public void tearDown() {
        app.stop();
    }

    /**
     * Test case: When deleting a message by ID and the message exists,
     * the response should return 200 and the deleted message in JSON format.
     */
    @Test
    public void deleteExistingMessageById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        Message expected = new Message(1, 1, "test message 1", 1669947792);
        Message actual = objectMapper.readValue(response.body(), Message.class);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test case: Attempting to delete a message that does not exist
     * should still return 200 with an empty response body.
     */
    @Test
    public void deleteNonExistentMessageById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/100"))
                .DELETE()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }
}
