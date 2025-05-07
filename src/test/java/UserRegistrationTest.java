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

public class UserRegistrationTest {

    private SocialMediaController controller;
    private HttpClient client;
    private ObjectMapper jsonMapper;
    private Javalin app;

    /**
     * This setup method resets the test DB, initializes the API and HTTP client,
     * and starts the server before each test.
     */
    @Before
    public void setupEnvironment() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        controller = new SocialMediaController();
        app = controller.startAPI();
        client = HttpClient.newHttpClient();
        jsonMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000); // ensure the server is up before tests run
    }

    /**
     * Stops the API server after each test case.
     */
    @After
    public void cleanupEnvironment() {
        app.stop();
    }

    /**
     * Test case: Register a new user with a unique username.
     * Expected result: HTTP 200 and the created user account returned.
     */
    @Test
    public void shouldRegisterNewUserSuccessfully() throws IOException, InterruptedException {
        String payload = "{\"username\": \"user\", \"password\": \"password\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, response.statusCode());

        Account expected = new Account(2, "user", "password");
        Account actual = jsonMapper.readValue(response.body(), Account.class);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test case: Attempt to register a user with a username that already exists.
     * Expected result: First registration returns 200; second attempt returns 400.
     */
    @Test
    public void shouldFailForDuplicateUsername() throws IOException, InterruptedException {
        String payload = "{\"username\": \"user\", \"password\": \"password\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> firstAttempt = client.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> secondAttempt = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, firstAttempt.statusCode());
        Assert.assertEquals(400, secondAttempt.statusCode());
        Assert.assertTrue(secondAttempt.body().isEmpty());
    }

    /**
     * Test case: Attempt registration without providing a username.
     * Expected result: HTTP 400 and empty body.
     */
    @Test
    public void shouldRejectBlankUsername() throws IOException, InterruptedException {
        String payload = "{\"username\": \"\", \"password\": \"password\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }

    /**
     * Test case: Attempt registration with a password shorter than 4 characters.
     * Expected result: HTTP 400 and empty response.
     */
    @Test
    public void shouldRejectShortPassword() throws IOException, InterruptedException {
        String payload = "{\"username\": \"username\", \"password\": \"pas\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(400, response.statusCode());
        Assert.assertTrue(response.body().isEmpty());
    }
}
