import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

public class RetrieveAllMessagesTest {

    private SocialMediaController socialMediaController;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private Javalin app;

    @Before
    public void initializeServer() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        socialMediaController = new SocialMediaController();
        app = socialMediaController.startAPI();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // allow server to fully initialize
    }

    @After
    public void shutdownServer() {
        app.stop();
    }

    /**
     * Test for retrieving all messages when messages exist in the database.
     * Expects HTTP 200 and a list containing the expected message(s).
     */
    @Test
    public void shouldReturnAllMessagesWhenPresent() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .GET()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        List<Message> expected = new ArrayList<>();
        expected.add(new Message(1, 1, "test message 1", 1669947792));

        List<Message> actual = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for retrieving all messages when database is empty.
     * Deletes initial data and expects an empty list in the response.
     */
    @Test
    public void shouldReturnEmptyListWhenNoMessagesExist() throws IOException, InterruptedException {
        clearDefaultMessage();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .GET()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        List<Message> messages = objectMapper.readValue(response.body(), new TypeReference<>() {});
        Assert.assertTrue(messages.isEmpty());
    }

    private void clearDefaultMessage() {
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM message WHERE message_id = ?")) {
            stmt.setInt(1, 1);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
