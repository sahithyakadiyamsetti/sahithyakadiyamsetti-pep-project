package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.AccountDao;
import DAO.DaoException;
import Model.Account;

/**
 * Service class to manage business logic for account operations, interacting with the AccountDao for CRUD actions.
 * Handles validations and calls the DAO layer for database transactions.
 */
public class AccountService {
    private AccountDao accountDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    // Default constructor that initializes the DAO
    public AccountService() {
        accountDao = new AccountDao();
    }

    // Constructor for custom DAO usage, primarily for testing
    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Fetches an account by its ID.
     * 
     * @param id The account ID.
     * @return Optional containing the account if found, else empty.
     */
    public Optional<Account> getAccountById(int id) {
        LOGGER.info("Fetching account with ID: {}", id);
        try {
            Optional<Account> account = accountDao.getById(id);
            LOGGER.info("Fetched account: {}", account.orElse(null));
            return account;
        } catch (DaoException e) {
            throw new ServiceException("Error while fetching account", e);
        }
    }

    /**
     * Retrieves all accounts.
     * 
     * @return List of all accounts.
     */
    public List<Account> getAllAccounts() {
        LOGGER.info("Fetching all accounts");
        try {
            List<Account> accounts = accountDao.getAll();
            LOGGER.info("Total accounts fetched: {}", accounts.size());
            return accounts;
        } catch (DaoException e) {
            throw new ServiceException("Error while fetching all accounts", e);
        }
    }

    /**
     * Finds an account by its username.
     * 
     * @param username The account username.
     * @return Optional containing the account if found, else empty.
     */
    public Optional<Account> findAccountByUsername(String username) {
        LOGGER.info("Searching account by username: {}", username);
        try {
            Optional<Account> account = accountDao.findAccountByUsername(username);
            LOGGER.info("Account found: {}", account.orElse(null));
            return account;
        } catch (DaoException e) {
            throw new ServiceException("Error while finding account by username", e);
        }
    }

    /**
     * Validates login credentials for an account.
     * 
     * @param account Account object containing login data.
     * @return Optional containing the validated account if credentials are correct.
     */
    public Optional<Account> validateLogin(Account account) {
        LOGGER.info("Validating login for user: {}", account.getUsername());
        try {
            Optional<Account> validatedAccount = accountDao.validateLogin(account.getUsername(), account.getPassword());
            LOGGER.info("Login validation result: {}", validatedAccount.isPresent());
            return validatedAccount;
        } catch (DaoException e) {
            throw new ServiceException("Error during login validation", e);
        }
    }

    /**
     * Creates a new account after validating input.
     * 
     * @param account The account to create.
     * @return The newly created account.
     */
    public Account createAccount(Account account) {
        LOGGER.info("Creating account for: {}", account);
        try {
            validateAccount(account);
            Optional<Account> existingAccount = findAccountByUsername(account.getUsername());
            if (existingAccount.isPresent()) {
                throw new ServiceException("Account with this username already exists.");
            }
            Account createdAccount = accountDao.insert(account);
            LOGGER.info("Account created: {}", createdAccount);
            return createdAccount;
        } catch (DaoException e) {
            throw new ServiceException("Error during account creation", e);
        }
    }

    /**
     * Updates an existing account.
     * 
     * @param account The account to update.
     * @return true if the update is successful, false otherwise.
     */
    public boolean updateAccount(Account account) {
        LOGGER.info("Updating account for: {}", account);
        try {
            boolean updated = accountDao.update(account);
            LOGGER.info("Account updated: {}. Update successful: {}", account, updated);
            return updated;
        } catch (DaoException e) {
            throw new ServiceException("Error while updating account", e);
        }
    }

    /**
     * Deletes an account.
     * 
     * @param account The account to delete.
     * @return true if the account was deleted successfully, false otherwise.
     */
    public boolean deleteAccount(Account account) {
        LOGGER.info("Deleting account: {}", account);
        if (account.getAccount_id() == 0) {
            throw new IllegalArgumentException("Account ID cannot be null.");
        }
        try {
            boolean deleted = accountDao.delete(account);
            LOGGER.info("Account deleted: {}. Deletion successful: {}", account, deleted);
            return deleted;
        } catch (DaoException e) {
            throw new ServiceException("Error while deleting account", e);
        }
    }

    /**
     * Validates the account details.
     * 
     * @param account The account to validate.
     * @throws ServiceException if the validation fails.
     */
    private void validateAccount(Account account) {
        LOGGER.info("Validating account: {}", account);
        String username = account.getUsername().trim();
        String password = account.getPassword().trim();

        if (username.isEmpty()) {
            throw new ServiceException("Username cannot be blank.");
        }
        if (password.isEmpty()) {
            throw new ServiceException("Password cannot be empty.");
        }
        if (password.length() < 4) {
            throw new ServiceException("Password must be at least 4 characters long.");
        }
        if (accountDao.doesUsernameExist(account.getUsername())) {
            throw new ServiceException("Username must be unique.");
        }
    }

    /**
     * Checks if an account exists by its ID.
     * 
     * @param accountId The account ID.
     * @return true if the account exists, false otherwise.
     */
    public boolean accountExists(int accountId) {
        LOGGER.info("Checking if account exists with ID: {}", accountId);
        try {
            Optional<Account> account = accountDao.getById(accountId);
            boolean exists = account.isPresent();
            LOGGER.info("Account existence check: {}", exists);
            return exists;
        } catch (DaoException e) {
            throw new ServiceException("Error while checking account existence", e);
        }
    }
}
