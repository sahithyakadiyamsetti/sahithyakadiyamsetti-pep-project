package DAO;

import Model.Account;
import Util.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class handles database operations (CRUD) related to the Account table.
 */
public class AccountDao implements BaseDao<Account> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDao.class);

    // Logs and throws a custom exception when a SQL error occurs
    private void logAndThrowSQLException(SQLException e, String sqlQuery, String message) {
        LOGGER.error("SQL Exception: {}", e.getMessage());
        LOGGER.error("SQL State: {}", e.getSQLState());
        LOGGER.error("Error Code: {}", e.getErrorCode());
        LOGGER.error("Query: {}", sqlQuery);
        throw new DaoException(message, e);
    }

    /**
     * Retrieves an account based on its ID.
     */
    @Override
    public Optional<Account> getById(int id) {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            logAndThrowSQLException(e, sql, "Failed to fetch account by ID: " + id);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all accounts stored in the database.
     */
    @Override
    public List<Account> getAll() {
        List<Account> accountList = new ArrayList<>();
        String sql = "SELECT * FROM account";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accountList.add(new Account(
                        rs.getInt("account_id"),
                        rs.getString("username"),
                        rs.getString("password")));
            }
        } catch (SQLException e) {
            logAndThrowSQLException(e, sql, "Failed to retrieve all accounts.");
        }

        return accountList;
    }

    /**
     * Looks up an account by username.
     */
    public Optional<Account> findAccountByUsername(String username) {
        String sql = "SELECT * FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password")));
                }
            }
        } catch (SQLException e) {
            logAndThrowSQLException(e, sql, "Failed to find account with username: " + username);
        }

        return Optional.empty();
    }

    /**
     * Validates login credentials against records in the database.
     */
    public Optional<Account> validateLogin(String username, String password) {
        String sql = "SELECT * FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account(
                            rs.getInt("account_id"),
                            rs.getString("username"),
                            rs.getString("password"));
                    if (Objects.equals(account.getPassword(), password)) {
                        return Optional.of(account);
                    }
                }
            }
        } catch (SQLException e) {
            logAndThrowSQLException(e, sql, "Login validation failed for username: " + username);
        }

        return Optional.empty();
    }

    /**
     * Checks if a specific username already exists in the database.
     */
    public boolean doesUsernameExist(String username) {
        String sql = "SELECT COUNT(*) FROM account WHERE username = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logAndThrowSQLException(e, sql, "Failed to check if username exists: " + username);
        }

        return false;
    }

    /**
     * Adds a new account to the database and returns the saved account with its generated ID.
     */
    @Override
    public Account insert(Account account) {
        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Account(id, account.getUsername(), account.getPassword());
                } else {
                    throw new DaoException("Insert failed: No ID returned.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Insert operation failed", e);
        }
    }

    /**
     * Updates an existing account's details.
     */
    @Override
    public boolean update(Account account) {
        String sql = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.setInt(3, account.getAccount_id());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Update failed for account ID: " + account.getAccount_id(), e);
        }
    }

    /**
     * Removes an account from the database.
     */
    @Override
    public boolean delete(Account account) {
        String sql = "DELETE FROM account WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getAccount_id());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Delete operation failed for account ID: " + account.getAccount_id(), e);
        }
    }
}
