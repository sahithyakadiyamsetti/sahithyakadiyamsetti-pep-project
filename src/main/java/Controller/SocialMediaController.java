package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SocialMediaController {

    // Instances to handle account-related and message-related operations
    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        // Initializing accountService and messageService instances
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    /**
     * Initializes the social media application with Javalin and sets up necessary
     * endpoints.
     * 
     * @return a Javalin instance with the configured routes.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages", this::getMessagesByAccountId);

        return app;
    }

    /**
     * Handles the registration process of new users. Accepts account details and
     * registers a new account.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     * @throws JsonProcessingException if there is an issue with JSON parsing or
     *                                 serialization.
     */
    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);

            // Sends the registered account as a JSON response
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (ServiceException e) {
            // Sets response status to 400 (Bad Request) if the account registration fails
            ctx.status(400);
        }
    }

    /**
     * Handles the login process for users. Accepts account credentials and
     * attempts to log the user in.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     * @throws JsonProcessingException if there is an issue with JSON parsing or
     *                                 serialization.
     */
    private void loginAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); // Default constructor for Model.Account required by Jackson ObjectMapper
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService
                    .validateLogin(account);
            if (loggedInAccount.isPresent()) {
                // Sends the logged-in account as a JSON response
                ctx.json(mapper.writeValueAsString(loggedInAccount));
                ctx.sessionAttribute("logged_in_account", loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                // Sets response status to 401 (Unauthorized) if login fails
                ctx.status(401);
            }
        } catch (ServiceException e) {
            // Sets response status to 401 (Unauthorized) if a service error occurs during login
            ctx.status(401);
        }
    }

    /**
     * Handles the creation of new messages. Accepts message details and creates
     * a new message.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     * @throws JsonProcessingException if there is an issue with JSON parsing or
     *                                 serialization.
     */
    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService
                    .getAccountById(mappedMessage.getPosted_by());
            Message message = messageService.createMessage(mappedMessage, account);
            ctx.json(message);
        } catch (ServiceException e) {
            // Sets response status to 400 (Bad Request) in case of exception
            ctx.status(400);
        }
    }

    /**
     * Retrieves all messages. Responds with all messages in the system.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     */
    private void getAllMessages(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    /**
     * Retrieves a specific message by its ID.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     */
    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                // Sets response status to 200 (OK) even if the message is not found
                ctx.status(200);
                ctx.result(""); // Empty body for not found message
            }
        } catch (NumberFormatException e) {
            // Responds with 400 (Bad Request) if 'message_id' is invalid
            ctx.status(400);
        } catch (ServiceException e) {
            // Responds with 200 (OK) if a service error occurs while retrieving the message
            ctx.status(200);
            ctx.result(""); // Empty body for service error
        }
    }

    /**
     * Deletes a specific message by its ID.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     */
    private void deleteMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                messageService.deleteMessage(message.get());
                ctx.status(200);
                ctx.json(message.get());
            } else {
                // Sets response status to 200 (OK) when no message is found for deletion
                ctx.status(200);
            }
        } catch (ServiceException e) {
            // Handles the deletion failure gracefully with status 200 (OK)
            ctx.status(200);
        }
    }

    /**
     * Updates a specific message by its ID. Accepts new message content in the
     * request body.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     * @throws JsonProcessingException if there is an issue with JSON parsing or
     *                                 serialization.
     */
    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);

            // Updates the message and sends the updated message as a response
            Message messageUpdated = messageService.updateMessage(mappedMessage);
            ctx.json(messageUpdated);

        } catch (ServiceException e) {
            // Responds with 400 (Bad Request) if an exception occurs during update
            ctx.status(400);
        }
    }

    /**
     * Retrieves all messages associated with a specific account ID.
     * 
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response.
     */
    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
            List<Message> messages = messageService.getMessagesByAccountId(accountId);
            if (!messages.isEmpty()) {
                ctx.json(messages);
            } else {
                ctx.json(messages); // Empty response body if no messages are found
                ctx.status(200);
            }
        } catch (ServiceException e) {
            // Sets response status to 400 (Bad Request) in case of an error retrieving messages
            ctx.status(400);
        }
    }
}
