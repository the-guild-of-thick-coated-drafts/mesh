package com.gentics.mesh.rest.node;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gentics.mesh.core.rest.common.FieldTypes;
import com.gentics.mesh.core.rest.micronode.MicronodeResponse;
import com.gentics.mesh.core.rest.node.FieldMap;
import com.gentics.mesh.core.rest.node.FieldMapJsonImpl;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.field.BinaryField;
import com.gentics.mesh.core.rest.node.field.BooleanField;
import com.gentics.mesh.core.rest.node.field.DateField;
import com.gentics.mesh.core.rest.node.field.HtmlField;
import com.gentics.mesh.core.rest.node.field.MicronodeField;
import com.gentics.mesh.core.rest.node.field.NodeField;
import com.gentics.mesh.core.rest.node.field.NumberField;
import com.gentics.mesh.core.rest.node.field.StringField;
import com.gentics.mesh.core.rest.node.field.impl.BinaryFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.BooleanFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.DateFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.HtmlFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.FieldList;
import com.gentics.mesh.core.rest.node.field.list.NodeFieldList;
import com.gentics.mesh.core.rest.node.field.list.impl.BooleanFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.DateFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.HtmlFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.MicronodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListItemImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NumberFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.StringFieldListImpl;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.json.JsonUtil;

import io.vertx.core.json.JsonObject;

public class FieldMapTest {

	@BeforeClass
	public static void setupOnce() {
		JsonUtil.debugMode = true;
	}

	@Test
	public void testJsonMapNullHandling() throws JsonParseException, JsonMappingException, IOException {
		FieldMap fieldMap = new FieldMapJsonImpl();

		fieldMap.put("stringField", new StringFieldImpl().setString("text"));
		fieldMap.put("stringFieldNull", new StringFieldImpl().setString(null));

		fieldMap.put("dateField", new DateFieldImpl().setDate(100L));
		fieldMap.put("dateFieldNull", new DateFieldImpl().setDate(null));

		fieldMap.put("htmlField", new HtmlFieldImpl().setHTML("someHtml"));
		fieldMap.put("htmlFieldNull", new HtmlFieldImpl().setHTML(null));

		fieldMap.put("numberField", new NumberFieldImpl().setNumber(42));
		fieldMap.put("numberFieldNull", new NumberFieldImpl().setNumber(null));

		BinaryFieldImpl field = new BinaryFieldImpl();
		field.setFileName("name");
		field.setDpi(42);
		fieldMap.put("binaryField", field);

		fieldMap.put("binaryFieldNull", null);

		fieldMap.put("booleanField", new BooleanFieldImpl().setValue(true));
		fieldMap.put("booleanFieldNull", new BooleanFieldImpl().setValue(null));

		fieldMap.put("micronodeField", new MicronodeResponse());
		fieldMap.put("micronodeFieldNull", null);

		fieldMap.put("nodeField", new NodeResponse());
		fieldMap.put("nodeFieldNull", null);

		// lists
		NodeFieldListImpl nodeList = new NodeFieldListImpl();
		nodeList.add(new NodeFieldListItemImpl().setUuid("blub"));
		nodeList.add(new NodeFieldListItemImpl().setUuid("blub2"));
		NodeResponse nodeResponse = new NodeResponse();
		nodeResponse.setUuid("blub3");
		nodeList.add(nodeResponse);
		fieldMap.put("nodeListField", nodeList);

		MicronodeFieldListImpl micronodeList = new MicronodeFieldListImpl();
		MicronodeResponse modelA = new MicronodeResponse();
		modelA.setUuid("blub");
		MicronodeResponse modelB = new MicronodeResponse();
		modelB.setUuid("blab");
		MicronodeResponse modelC = new MicronodeResponse();
		modelC.setUuid("blar");

		micronodeList.add(modelA);
		micronodeList.add(modelB);
		micronodeList.add(modelC);
		fieldMap.put("micronodeListField", micronodeList);

		StringFieldListImpl stringList = new StringFieldListImpl();
		stringList.add("first");
		stringList.add("second");
		stringList.add("third");
		fieldMap.put("stringListField", stringList);

		DateFieldListImpl dateList = new DateFieldListImpl();
		dateList.add(10L);
		dateList.add(42L);
		dateList.add(System.currentTimeMillis());
		fieldMap.put("dateListField", dateList);

		BooleanFieldListImpl booleanList = new BooleanFieldListImpl();
		booleanList.add(true);
		booleanList.add(true);
		booleanList.add(false);
		fieldMap.put("booleanListField", booleanList);

		HtmlFieldListImpl htmlList = new HtmlFieldListImpl();
		htmlList.add("htmlA");
		htmlList.add("htmlB");
		htmlList.add("htmlC");
		fieldMap.put("htmlListField", htmlList);

		NumberFieldListImpl numberList = new NumberFieldListImpl();
		numberList.add(10);
		numberList.add(42);
		numberList.add(12);
		fieldMap.put("numberListField", numberList);

		fieldMap.put("nulled", null);

		// Assert fieldmap fields
		assertMap(fieldMap);

		assertNull("The nulled Value should be returned with null value", fieldMap.getField("nulled", FieldTypes.STRING, null, false));

		// Validate transformation
		String json = JsonUtil.toJson(fieldMap);
		JsonObject jsonObject = new JsonObject(json);

		// Check json null values
		assertThat(jsonObject).hasNullValue("nulled");
		assertThat(jsonObject).hasNullValue("nodeFieldNull");
		assertThat(jsonObject).hasNullValue("binaryFieldNull");
		assertThat(jsonObject).hasNullValue("micronodeFieldNull");
		assertThat(jsonObject).hasNullValue("booleanFieldNull");
		assertThat(jsonObject).hasNullValue("binaryFieldNull");
		assertThat(jsonObject).hasNullValue("numberFieldNull");
		assertThat(jsonObject).hasNullValue("htmlFieldNull");
		assertThat(jsonObject).hasNullValue("dateFieldNull");
		assertThat(jsonObject).hasNullValue("stringFieldNull");

		assertNotNull(json);
		FieldMap fieldMapDeserialized = JsonUtil.readValue(json, FieldMap.class);
		assertMap(fieldMapDeserialized);

	}

	@Test
	public void testEmptyMap() {
		FieldMap map = new FieldMapJsonImpl();
		assertTrue("The map should be empty.", map.isEmpty());
		assertEquals("No field should be stored in the map.", 0, map.size());
	}

	private void assertMap(FieldMap fieldMap) {

		StringField stringField = fieldMap.getField("stringField", FieldTypes.STRING, null, false);
		assertNotNull(stringField);
		assertNotNull(fieldMap.getStringField("stringField"));

		StringField stringFieldNull = fieldMap.getField("stringFieldNull", FieldTypes.STRING, null, false);
		assertNotNull(stringFieldNull);
		assertNull("The string value of the field should be null", stringFieldNull.getString());

		HtmlField htmlField = fieldMap.getField("htmlField", FieldTypes.HTML, null, false);
		assertNotNull(htmlField);
		assertNotNull(fieldMap.getHtmlField("htmlField"));

		HtmlField htmlFieldNull = fieldMap.getField("htmlFieldNull", FieldTypes.HTML, null, false);
		assertNotNull(htmlFieldNull);
		assertNull("The html value of the field should be null", htmlFieldNull.getHTML());

		BooleanField booleanField = fieldMap.getField("booleanField", FieldTypes.BOOLEAN, null, false);
		assertNotNull(booleanField);
		assertNotNull(fieldMap.getBooleanField("booleanField"));

		BooleanField booleanFieldNull = fieldMap.getField("booleanFieldNull", FieldTypes.BOOLEAN, null, false);
		assertNotNull(booleanFieldNull);
		assertNull("The boolean value of the field should be null", booleanFieldNull.getValue());

		DateField dateField = fieldMap.getField("dateField", FieldTypes.DATE, null, false);
		assertNotNull(dateField);
		assertNotNull(fieldMap.getDateField("dateField"));

		NumberField numberFieldNull = fieldMap.getField("numberFieldNull", FieldTypes.NUMBER, null, false);
		assertNotNull(numberFieldNull);
		assertNull("The Number value of the field should be null", numberFieldNull.getNumber());

		NodeField nodeField = fieldMap.getField("nodeField", FieldTypes.NODE, null, false);
		assertNotNull(nodeField);
		assertNotNull(fieldMap.getNodeField("nodeField"));

		NodeField nodeFieldNull = fieldMap.getField("nodeFieldNull", FieldTypes.NODE, null, false);
		assertNull(nodeFieldNull);

		MicronodeField micronodeField = fieldMap.getField("micronodeField", FieldTypes.MICRONODE, null, false);
		assertNotNull(micronodeField);
		assertNotNull(fieldMap.getMicronodeField("micronodeField"));

		MicronodeField micronodeFieldNull = fieldMap.getField("micronodeFieldNull", FieldTypes.MICRONODE, null, false);
		assertNull(micronodeFieldNull);

		NumberField numberField = fieldMap.getField("numberField", FieldTypes.NUMBER, null, false);
		assertNotNull(numberField);
		assertNotNull(fieldMap.getNumberField("numberField"));

		BinaryField binaryField = fieldMap.getField("binaryField", FieldTypes.BINARY, null, false);
		assertNotNull(fieldMap.getBinaryField("binaryField"));
		assertNotNull(binaryField);

		// Lists
		NumberFieldListImpl numberList = fieldMap.getNumberFieldList("numberListField");
		assertNotNull(numberList);
		assertEquals(3, numberList.getItems().size());

		HtmlFieldListImpl htmlList = fieldMap.getHtmlFieldList("htmlListField");
		assertNotNull(htmlList);
		assertEquals(3, htmlList.getItems().size());

		DateFieldListImpl dateList = fieldMap.getDateFieldList("dateListField");
		assertNotNull(dateList);
		assertEquals(3, dateList.getItems().size());

		BooleanFieldListImpl booleanList = fieldMap.getBooleanFieldList("booleanListField");
		assertNotNull(booleanList);
		assertEquals(3, booleanList.getItems().size());

		StringFieldListImpl stringList = fieldMap.getStringFieldList("stringListField");
		assertNotNull(stringList);
		assertEquals(3, stringList.getItems().size());

		NodeFieldList nodeList = fieldMap.getNodeFieldList("nodeListField");
		assertNotNull(nodeList);
		assertEquals(3, nodeList.getItems().size());

		FieldList<MicronodeField> micronodeList = fieldMap.getMicronodeFieldList("micronodeListField");
		assertNotNull(micronodeList);
		assertEquals(3, micronodeList.getItems().size());

		assertEquals("The map did not contain the expected amount of fields.", 24, fieldMap.size());
		assertFalse("The map should not be empty.", fieldMap.isEmpty());
		assertTrue("The string field should be within the map.", fieldMap.hasField("stringField"));

		assertNotNull(fieldMap.getField("stringField", new StringFieldSchemaImpl().setName("stringField")));

		// Validate null handling
		assertNull("Null should be returned for a key that was not added to the map.", fieldMap.getField("bogus", FieldTypes.STRING, null, false));
		assertFalse("No field with key bogus should be contained within the map.", fieldMap.hasField("bogus"));
		assertTrue("The key should be stored in the map.", fieldMap.hasField("stringField"));

	}
}