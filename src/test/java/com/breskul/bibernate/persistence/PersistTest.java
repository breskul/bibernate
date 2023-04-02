package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.test_model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class PersistTest extends AbstractDataSourceTest {

	public static final long ID = 1L;
	public static final String FIRST_NAME = "Serhii";
	public static final String LAST_NAME = "Yevtushok";
	public static final LocalDate BIRTHDAY = LocalDate.of(2023, Month.JANUARY, 1);
	public static final String NOTE_BODY = "WOW, my brain is steaming!";
	public static final String TABLE_NOT_FOUND_MESSAGE = "Table PersonWithoutTable not found - Use @Table annotation to specify table's name";
	public static final String NO_SEQUENCE_MESSAGE = "Can't find sequence SELECT nextval('users_seq') - Make sure that sequence match the pattern 'tableName_seq'";
	public static final String NO_ENTITY_MESSAGE = "com.breskul.bibernate.persistence.test_model.PersonWithoutEntity is not a valid entity class - @Entity annotation should be present";
	public static final String ID_AND_STRATEGY_MESSAGE = "detached entity passed to persist: com.breskul.bibernate.persistence.test_model.PersonWithIdAndStrategy - Make sure that you don't set id manually when using @GeneratedValue";
	public static final String WITHOUT_ID_AND_STRATEGY = "No id present for com.breskul.bibernate.persistence.test_model.PersonWithoutIdAndStrategy - ids for this class must be manually assigned before calling save(): com.breskul.bibernate.persistence.test_model.PersonWithoutIdAndStrategy";
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		entityManager = new EntityManagerImpl(dataSource);
	}

	@AfterEach
	void destroy() {
		doInConnection(connection -> {
			try {
				PreparedStatement notes = connection.prepareStatement(CLEAN_NOTE_TABLE);
				notes.execute();

				PreparedStatement persons = connection.prepareStatement(CLEAN_PERSON_TABLE);
				persons.execute();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
		entityManager.close();
	}

	@Test
	void insertOnlyPerson() {
		Person person = new Person();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		entityManager.persist(person);
		entityTransaction.commit();
		validatePerson();
	}

	@Test()
	void insertOnlyPersonWithoutTableShouldThrowException() {
		PersonWithoutTable person = new PersonWithoutTable();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
		entityTransaction.rollback();
		assertEquals(TABLE_NOT_FOUND_MESSAGE, jdbcDaoException.getMessage());
	}

	@Test()
	void insertOnlyPersonWithoutSequenceShouldThrowException() {
		PersonSequence person = new PersonSequence();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
		entityTransaction.commit();
		assertEquals(NO_SEQUENCE_MESSAGE, jdbcDaoException.getMessage());
	}

	@Test()
	void insertOnlyPersonWithoutEntityShouldThrowException() {
		PersonWithoutEntity person = new PersonWithoutEntity();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
		assertEquals(NO_ENTITY_MESSAGE, jdbcDaoException.getMessage());
	}

	@Test()
	void insertOnlyPersonWithIdAndStrategyShouldThrowException() {
		PersonWithIdAndStrategy person = new PersonWithIdAndStrategy();
		person.setId(1L);
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
		assertEquals(ID_AND_STRATEGY_MESSAGE, jdbcDaoException.getMessage());
	}

	@Test()
	void insertOnlyPersonPersonWithoutIdAndStrategyIdAndStrategyShouldThrowException() {
		PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.setBirthday(BIRTHDAY);
		JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
		assertEquals(WITHOUT_ID_AND_STRATEGY, jdbcDaoException.getMessage());
	}

	@Test
	void insertPersonWithNotes() {

		NoteComplex note = new NoteComplex();
		note.setBody(NOTE_BODY);

		Person person = new Person();
		person.setFirstName(FIRST_NAME);
		person.setLastName(LAST_NAME);
		person.addNote(note);
		person.setBirthday(BIRTHDAY);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		entityManager.persist(person);
		entityTransaction.commit();
		validatePerson();
		validateNote();
	}

	@Test
	void insertNoteWithoutPerson() {
		NoteWithoutGeneratedValue note = new NoteWithoutGeneratedValue();
		note.setId(22L);
		note.setBody(NOTE_BODY);
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		entityManager.persist(note);
		entityTransaction.commit();

		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		NoteComplex selectedNote = entityManager.find(NoteComplex.class, 22L);
		assertNotNull(selectedNote);
		assertEquals(note.getBody(), selectedNote.getBody());
		assertNull(selectedNote.getPerson());

		entityManager.remove(selectedNote);
		entityTransaction.commit();
	}

	private void validateNote() {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM notes");
			ResultSet resultSet = preparedStatement.executeQuery();

			assertTrue(resultSet.next());
			assertEquals(ID, resultSet.getLong(1));
			assertEquals(NOTE_BODY, resultSet.getString(2));
			assertEquals(ID, resultSet.getLong(4));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void validatePerson() {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users");
			ResultSet resultSet = preparedStatement.executeQuery();

			assertTrue(resultSet.next());
			assertNotNull(resultSet.getLong(1));
			assertEquals(FIRST_NAME, resultSet.getString(2));
			assertEquals(LAST_NAME, resultSet.getString(3));
			assertEquals(BIRTHDAY, resultSet.getDate(4).toLocalDate());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
