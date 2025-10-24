package com.etendorx.das.connector;

import com.etendoerp.etendorx.data.ETRXEntityField;
import com.etendoerp.etendorx.data.ETRXProjectionEntity;
import com.etendoerp.etendorx.data.EntityMapping;
import com.etendoerp.etendorx.data.InstanceConnectorMapping;
import com.etendorx.entities.metadata.FieldMetadata;
import com.etendorx.entities.metadata.MetadataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbravo.model.ad.datamodel.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OBCONFieldMappingTest {

  public static final String TABLE_ID = "TABLE_ID";
  public static final String FIELD_2 = "field2";
  public static final String TEST_ENTITY = "TestEntity";
  public static final String RESULT_SHOULD_NOT_BE_NULL = "Result should not be null";
  public static final String RESULT_SHOULD_BE_A_LIST = "Result should be a list";
  @Mock
  private MetadataUtil metadataUtil;

  private OBCONFieldMapping fieldMapping;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    fieldMapping = new OBCONFieldMapping(metadataUtil);
  }

  @Test
  void testMapValidEntityShouldReturnCorrectFieldMapping() {
    // Mock TableEntity
    Table tableEntity = new Table();
    tableEntity.setId(TABLE_ID);
    tableEntity.setName(TEST_ENTITY);

    // Mock ProjectionEntity
    ETRXProjectionEntity projectionEntity = mock(ETRXProjectionEntity.class);
    when(projectionEntity.getTableEntity()).thenReturn(tableEntity);

    // Mock EntityMapping
    EntityMapping entityMapping = mock(EntityMapping.class);
    when(entityMapping.getProjectionEntity()).thenReturn(projectionEntity);

    // Mock ETRXEntityField
    ETRXEntityField field1 = new ETRXEntityField();
    field1.setName("field1");
    field1.setJsonpath("$.field1");
    field1.setFieldMapping("mapping1");
    field1.setEtrxProjectionEntity(projectionEntity);

    ETRXEntityField field2 = new ETRXEntityField();
    field2.setName(FIELD_2);
    field2.setJsonpath("$.field2");
    field2.setFieldMapping("mapping2");
    field2.setProperty("relatedEntity.field2");
    field2.setEtrxProjectionEntity(projectionEntity);

    // Mock MetadataUtil for field2
    FieldMetadata fieldMetadata = new FieldMetadata("String", FIELD_2, "RELATED_TABLE_ID",
        "RELATED_TABLE_REL_ID", false, "RelatedEntity");
    when(metadataUtil.getPropertyMetadata(TABLE_ID, "relatedEntity")).thenReturn(fieldMetadata);

    // Mock InstanceConnectorMapping
    InstanceConnectorMapping instanceConnectorMapping = mock(InstanceConnectorMapping.class);
    when(instanceConnectorMapping.getEtrxEntityMapping()).thenReturn(entityMapping);
    List<ETRXEntityField> fields = new ArrayList<>();
    fields.add(field1);
    fields.add(field2);
    when(projectionEntity.getETRXEntityFieldList()).thenReturn(fields);

    // Act
    Object result = fieldMapping.map(instanceConnectorMapping);

    // Assert
    assertNotNull(result, RESULT_SHOULD_NOT_BE_NULL);
    assertInstanceOf(List.class, result, RESULT_SHOULD_BE_A_LIST);

    List<Map<String, Object>> fieldMappingList = (List<Map<String, Object>>) result;
    assertEquals(2, fieldMappingList.size(), "Field mapping list should contain 2 entries");

    // Validate field1
    Map<String, Object> field1Map = fieldMappingList.get(0);
    assertEquals("field1", field1Map.get("name"));
    assertEquals("$.field1", field1Map.get("jsonpath"));
    assertEquals("mapping1", field1Map.get("fieldMapping"));
    assertFalse((Boolean) field1Map.get("isExternalIdentifier"));

    // Validate field2
    Map<String, Object> field2Map = fieldMappingList.get(1);
    assertEquals(FIELD_2, field2Map.get("name"));
    assertEquals("$.field2", field2Map.get("jsonpath"));
    assertEquals("mapping2", field2Map.get("fieldMapping"));
    assertTrue((Boolean) field2Map.get("isExternalIdentifier"));
    assertEquals("RELATED_TABLE_REL_ID", field2Map.get("ad_table_id"));
    assertEquals("RelatedEntity", field2Map.get("entityName"));
  }

  @Test
  void testMapEmptyEntityShouldReturnEmptyList() {
    // Mock TableEntity
    Table tableEntity = new Table();
    tableEntity.setId(TABLE_ID);
    tableEntity.setName(TEST_ENTITY);

    // Mock ProjectionEntity
    ETRXProjectionEntity projectionEntity = mock(ETRXProjectionEntity.class);
    when(projectionEntity.getTableEntity()).thenReturn(tableEntity);
    when(projectionEntity.getETRXEntityFieldList()).thenReturn(new ArrayList<>());

    // Mock EntityMapping
    EntityMapping entityMapping = mock(EntityMapping.class);
    when(entityMapping.getProjectionEntity()).thenReturn(projectionEntity);

    // Mock InstanceConnectorMapping
    InstanceConnectorMapping instanceConnectorMapping = mock(InstanceConnectorMapping.class);
    when(instanceConnectorMapping.getEtrxEntityMapping()).thenReturn(entityMapping);

    // Act
    Object result = fieldMapping.map(instanceConnectorMapping);

    // Assert
    assertNotNull(result, RESULT_SHOULD_NOT_BE_NULL);
    assertTrue(result instanceof List, RESULT_SHOULD_BE_A_LIST);
    assertTrue(((List<?>) result).isEmpty(), "Result list should be empty");
  }

  @Test
  void testMapNullMetadataShouldHandleGracefully() {
    // Mock TableEntity
    Table tableEntity = new Table();
    tableEntity.setId(TABLE_ID);
    tableEntity.setName(TEST_ENTITY);

    // Mock ProjectionEntity
    ETRXProjectionEntity projectionEntity = mock(ETRXProjectionEntity.class);
    when(projectionEntity.getTableEntity()).thenReturn(tableEntity);

    // Mock EntityMapping
    EntityMapping entityMapping = mock(EntityMapping.class);
    when(entityMapping.getProjectionEntity()).thenReturn(projectionEntity);

    // Mock ETRXEntityField
    ETRXEntityField field = new ETRXEntityField();
    field.setName("field");
    field.setJsonpath("$.field");
    field.setFieldMapping("mapping");
    field.setProperty("relatedEntity.field");
    field.setEtrxProjectionEntity(projectionEntity);

    // Mock MetadataUtil to return null
    when(metadataUtil.getPropertyMetadata(TABLE_ID, "relatedEntity")).thenReturn(null);

    // Mock InstanceConnectorMapping
    InstanceConnectorMapping instanceConnectorMapping = mock(InstanceConnectorMapping.class);
    when(instanceConnectorMapping.getEtrxEntityMapping()).thenReturn(entityMapping);
    List<ETRXEntityField> fields = new ArrayList<>();
    fields.add(field);
    when(projectionEntity.getETRXEntityFieldList()).thenReturn(fields);

    // Act
    Object result = fieldMapping.map(instanceConnectorMapping);

    // Assert
    assertNotNull(result, RESULT_SHOULD_NOT_BE_NULL);
    assertInstanceOf(List.class, result, RESULT_SHOULD_BE_A_LIST);
    assertEquals(1, ((List<?>) result).size(), "Field mapping list should contain 1 entry");
  }
}
