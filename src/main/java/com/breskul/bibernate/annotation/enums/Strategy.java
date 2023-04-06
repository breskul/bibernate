package com.breskul.bibernate.annotation.enums;

/**
 * Defines the types of primary key generation strategies.
 */
public enum Strategy {
	/**
	 * Indicates that the persistence provider must assign
	 * primary keys for the entity using a database sequence.
	 */
	SEQUENCE,

	/**
	 * Indicates that the persistence provider must assign
	 * primary keys for the entity using a database identity column.
	 */
	IDENTITY,

	/**
	 * Indicates that the persistence provider should pick an
	 * appropriate strategy for the particular database. The
	 * <code>AUTO</code> generation strategy may expect a database
	 * resource to exist, or it may attempt to create one. A vendor
	 * may provide documentation on how to create such resources
	 * in the event that it does not support schema generation
	 * or cannot create the schema resource at runtime.
	 */
	AUTO
}
