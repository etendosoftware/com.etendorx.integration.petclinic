package com.etendorx.entities.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PetOwnerMetadataTest {

  private Pet_Owner_Metadata_ metadata;

  @BeforeEach
  void setUp() {
    metadata = new Pet_Owner_Metadata_();
  }

  @Test
  void testEntityMetadataInitialization() {
    assertEquals("pet_owner", metadata.getTableName(), "Table name should be 'pet_owner'");
    assertEquals("Owner", metadata.getEntityName(), "Entity name should be 'Owner'");
    assertEquals("A7855702774649CC9C2AC53BDBD01AB3", metadata.getAdTableId(), "AD Table ID should match");
  }

  @Test
  void testFieldsMetadata() {
    Map<String, FieldMetadata> fields = metadata.getFields();

    assertTrue(fields.containsKey("id"), "Field 'id' should exist");
    assertTrue(fields.containsKey("firstName"), "Field 'firstName' should exist");
    assertTrue(fields.containsKey("lastName"), "Field 'lastName' should exist");
    assertTrue(fields.containsKey("address"), "Field 'address' should exist");
    assertTrue(fields.containsKey("city"), "Field 'city' should exist");
    assertTrue(fields.containsKey("telephone"), "Field 'telephone' should exist");

    FieldMetadata idField = fields.get("id");
    assertEquals("String", idField.getType(), "Field 'id' should have type 'String'");
    assertEquals("pet_owner_id", idField.getDbColumn(), "Field 'id' should map to column 'pet_owner_id'");
    assertEquals("72E81ACE390443C5A5E4856E4C55EDBA", idField.getAdColumnId(), "Field 'id' should have correct field ID");

    FieldMetadata activeField = fields.get("active");
    assertEquals("Boolean", activeField.getType(), "Field 'active' should have type 'Boolean'");
    assertEquals("isactive", activeField.getDbColumn(), "Field 'active' should map to column 'isactive'");
  }

  @Test
  void testRelationshipsMetadata() {
    Map<String, FieldMetadata> fields = metadata.getFields();

    FieldMetadata petListField = fields.get("petPetList");
    assertNotNull(petListField, "Field 'petPetList' should exist");
    assertEquals("com.etendoerp.integration.petclinic.Pet", petListField.getType(),
        "Field 'petPetList' should have type 'com.etendoerp.integration.petclinic.Pet'");
    assertTrue(petListField.isArray(), "Field 'petPetList' should be a collection");
  }
}
