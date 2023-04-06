package com.breskul.bibernate.persistence;

/**
 * Interface used to interact with the entity manager factory for the persistence unit.
 * When the application has finished using the entity manager factory, and/or at application shutdown,
 * the application should close the entity manager factory. Once an EntityManagerFactory has been closed,
 * all its entity managers are considered to be in the closed state.
 */
public interface EntityManagerFactory {

    /**
     * Create a new application-managed <code>EntityManager</code>.
     * This method returns a new <code>EntityManager</code> instance each time
     * it is invoked.
     * @return entity manager instance
     */
    EntityManager createEntityManager();

    /**
     * Indicates whether the factory is open. Returns true
     * until the factory has been closed.
     * @return boolean indicating whether the factory is open
     */
    boolean isOpen();

    /**
     * Close the factory, releasing any resources that it holds.
     */
    void close();
}
