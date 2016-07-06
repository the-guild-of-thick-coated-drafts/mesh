package com.gentics.mesh.core.field;

import java.io.IOException;

/**
 * List of common verticle specific test cases for fields.
 */
public interface FieldVerticleTestcases {

	/**
	 * Read a node that already contains a filled field. Make sure the response contains the expected field data.
	 * 
	 * @throws IOException
	 */
	void testReadNodeWithExistingField() throws IOException;

	/**
	 * Update a node with a currently filled field. Change the field and make sure the changes were applied correctly.
	 */
	void testUpdateNodeFieldWithField();

	/**
	 * Update a node with a currently filled field with the same value. No new version should be generated.
	 */
	void testUpdateSameValue();

	/**
	 * Update a node with a currently filled field using a null value. Assert that the field will be deleted and is no longer listed in the response.
	 */
	void testUpdateSetNull();

	/**
	 * Update a node with a currently filled field using a empty field value. Assert that the field will still be listed in the response. It should not be
	 * deleted. Some basic field types can't be set to empty (e.g: Number, Date, Boolean..) Assert that the field will be set to null in those cases.
	 */
	void testUpdateSetEmpty();

	/**
	 * Create a new node and set field values. Make sure the node was correctly created and the field was populated with the correct data.
	 */
	void testCreateNodeWithField();

	/**
	 * Create a new node and set no field value for the field. Make sure the node was correctly loaded and that the field was set to an empty value. Basic
	 * fields must be set to null.
	 */
	void testCreateNodeWithNoField();

	// TODO testcases for mandatory fields? deletion testcases? We can use explicit null values to delete a field.

}