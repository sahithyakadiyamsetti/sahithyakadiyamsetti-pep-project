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
import Model.Account;
import Util.ConnectionUtil;
import io.javalin.Javalin;

public class UserLoginTest {

    private SocialMediaController controller;
    private HttpClient client;
    private ObjectMapper mapper;
    private Javalin app;

    /**
     * Initializes test environment before each case.
     * Starts the API, configures the HTTP client, and ensures the database is clean.
     */
    @Before
    public void initializeTestEnvironment() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        controller = new SocialMediaController();
        app = controller.startAPI();
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // Wait to ensure server starts
    }

    /**
     * Cleans up after each test by stopping the server.
     */
    @After
    public void shutDownServer() {
        app.stop();
    }

    /**
     * Verifies login works with valid credentials.
     * Expected outcome: status code 200 and user data returned.
     */
    @Test
    public void shouldLoginSuccessfullyWithValidCredentials() throws IOException, InterruptedException {
        String requestBody = "{\"username\": \"testuser1\", \"password\": \"password\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        Account expected = new Account(1, "testuser1", "password");
        Account actual = mapper.readValue(response.body(), Account.class);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests login with a non-existent username.
     * Expected outcome: status code 401 and an empty response.
     */
    @Test
    public void shouldFailLoginWithInvalidUsername() throws IOException, InterruptedException {
        String requestBody = "{\"username\": \"nonexistentUser\", \"password\": \"password\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(401, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }

    /**
     * Tests login with an incorrect password.
     * Expected outcome: status code 401 and no content.
     */
    @Test
    public void shouldFailLoginWithIncorrectPassword() throws IOException, InterruptedException {
        String requestBody = "{\"username\": \"testuser1\", \"password\": \"wrongpass\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(401, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }
}
