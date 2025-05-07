package DAO;

import java.util.List;
import java.util.Optional;

/**
 * A generic interface defining standard operations for managing
 * persistent objects of any type.
 * 
 * Classes representing database tables should implement this interface
 * to provide functionality for creating, reading, updating, and deleting
 * data records.
 */
public interface BaseDao<T> {

    /**
     * Fetches an object from the database using its unique identifier.
     *
     * @param id The primary key of the record to retrieve.
     * @return An Optional containing the object if found; otherwise, an empty Optional.
     */
    Optional<T> getById(int id);

    /**
     * Retrieves all records of the specified type from the database.
     *
     * @return A list containing all instances of type T stored in the database.
     */
    List<T> getAll();

    /**
     * Saves a new object to the database.
     *
     * @param t The object to be saved.
     * @return The saved object, including any fields updated by the database (e.g., auto-generated ID).
     */
    T insert(T t);

    /**
     * Modifies an existing object in the database.
     *
     * @param t The object to update.
     * @return true if the update was applied successfully; false if no matching record was found.
     */
    boolean update(T t);

    /**
     * Removes an object from the database.
     *
     * @param t The object to delete.
     * @return true if the deletion was successful; false otherwise.
     */
    boolean delete(T t);
}
