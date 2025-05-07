import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Controller.SocialMediaController;
import Model.Message;
import Util.ConnectionUtil;
import io.javalin.Javalin;

public class RetrieveAllMessagesForUserTest {

    private SocialMediaController socialMediaController;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private Javalin app;

    @Before
    public void initialize() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        socialMediaController = new SocialMediaController();
        app = socialMediaController.startAPI();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // ensure server has time to fully start
    }

    @After
    public void cleanUp() {
        app.stop();
    }

    /**
     * Verifies retrieval of messages for a user with existing messages.
     * Makes a GET request to /accounts/1/messages
     * Expected: HTTP 200 and a JSON list with one message.
     */
    @Test
    public void shouldReturnMessagesWhenUserHasMessages() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/1/messages"))
                .GET()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        List<Message> expectedMessages = new ArrayList<>();
        expectedMessages.add(new Message(1, 1, "test message 1", 1669947792));

        List<Message> actualMessages = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Assert.assertEquals(expectedMessages, actualMessages);
    }

    /**
     * Verifies response when requesting messages for a user with no messages.
     * Makes a GET request to /accounts/2/messages
     * Expected: HTTP 200 and an empty list in the response.
     */
    @Test
    public void shouldReturnEmptyListWhenNoMessagesForUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/2/messages"))
                .GET()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        List<Message> actualMessages = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Assert.assertTrue(actualMessages.isEmpty());
    }
}
