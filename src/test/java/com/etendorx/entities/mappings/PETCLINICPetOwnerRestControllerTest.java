package com.etendorx.entities.mappings;

import com.etendoerp.etendorx.data.Connector;
import com.etendoerp.etendorx.data.InstanceConnector;
import com.etendoerp.integration.petclinic.Owner;
import com.etendorx.das.EtendorxDasApplication;
import com.etendorx.entities.jparepo.*;
import com.etendorx.entities.mapper.lib.DASRepository;
import com.etendorx.utils.auth.key.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/etendo",
        "spring.datasource.username=tad",
        "spring.datasource.password=tad",
        "spring.main.allow-bean-definition-overriding=true"
    },
    classes = EtendorxDasApplication.class
)
@AutoConfigureMockMvc
class PETCLINICPetOwnerRestControllerTest {
  private static final Logger log = LogManager.getLogger(PETCLINICPetOwnerRestControllerTest.class);
  public static final String AD_USER_ID = "A530AAE22C864702B7E1C22D58E7B17B";
  public static final String AD_CLIENT_ID = "23C59575B9CF467C9620760EB255B389";
  public static final String AD_ORG_ID = "19404EAD144C49A0AF37D54377CF452D";

  @Autowired
  private MockMvc mockMvc;

  @Mock
  private DASRepository<PETCLINICPet_OwnerDTORead, PETCLINICPet_OwnerDTOWrite> repository;

  @Mock
  private PETCLINICPet_OwnerJsonPathConverter converter;

  @Mock
  private Validator validator;

  @Autowired
  private Pet_OwnerRepository petOwnerRepository;

  @Autowired
  private ETRX_instance_externalidRepository instanceExternalIdRepository;

  @Autowired
  private ETRX_Instance_ConnectorRepository instanceConnectorRepository;

  @Autowired
  private ETRX_ConnectorRepository connectorRepository;

  @Autowired
  private Pet_OwnerRepository perOwnerRepository;

  @Autowired
  private ADUserRepository adUserRepository;

  @Autowired
  private UserContext userContext;

  @Autowired
  private ADClientRepository adClientRepository;

  @Autowired
  private OrganizationRepository organizationRepository;


  private PETCLINICPet_OwnerRestController controller;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    controller = new PETCLINICPet_OwnerRestController(converter, repository, validator);
  }

  @BeforeEach
  public void deleteAll() {
    petOwnerRepository.deleteAll();
  }

  @BeforeEach
  public void setupInstanceConnector() {

    Connector connector = new Connector();
    connector.setActive(true);
    connector.setUpdatedBy(adUserRepository.findById(AD_USER_ID).orElse(null));
    connector.setCreatedBy(adUserRepository.findById(AD_USER_ID).orElse(null));
    connector.setUpdated(new Date());
    connector.setCreationDate(new Date());
    connector.setClient(adClientRepository.findById(AD_CLIENT_ID).orElse(null));
    connector.setOrganization(organizationRepository.findById(AD_ORG_ID).orElse(null));
    connector.setName("petclinic");
    connectorRepository.save(connector);

    InstanceConnector instanceConnector = new InstanceConnector();
    instanceConnector.setActive(true);
    instanceConnector.setUpdatedBy(adUserRepository.findById(AD_USER_ID).orElse(null));
    instanceConnector.setCreatedBy(adUserRepository.findById(AD_USER_ID).orElse(null));
    instanceConnector.setUpdated(new Date());
    instanceConnector.setCreationDate(new Date());
    instanceConnector.setClient(adClientRepository.findById(AD_CLIENT_ID).orElse(null));
    instanceConnector.setOrganization(organizationRepository.findById(AD_ORG_ID).orElse(null));
    instanceConnector.setEtrxConnector(connector);
    instanceConnector.setAuthorizationType("Bearer");
    instanceConnector.setName("petclinic");
    instanceConnector.setURL("http://localhost:8080/petclinic");

    instanceConnectorRepository.save(instanceConnector);

    userContext.setExternalSystemId(instanceConnector.getId());
  }

  @Test
  @Transactional
  void testPost_ValidRequest_ShouldReturnCreated() throws Exception {

    log.info("Instance connector id: {}", userContext.getExternalSystemId());

    Map<String, Object> pet = new HashMap<>();
    pet.put("id", "345");
    pet.put("name", "Fido");
    pet.put("birthDate", "2021-01-01");
    pet.put("type", "Dog");

    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put("firstName", "John");
    owner.put("lastName", "Doe");
    owner.put("address", "123 Main St");
    owner.put("telephone", "1234567890");
    owner.put("city", "Springfield");
    owner.put("pets", List.of(pet));

    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post("/petclinic/Pet_Owner")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owner)));

    result.andExpect(status().isCreated());

    Iterable<Owner> owners = petOwnerRepository.findAll();

    // Lookup owner by name and compare with example
    for (Owner o : owners) {
      if (o.getFirstName().equals("John") && o.getLastName().equals("Doe")) {
        assert(o.getFirstName().equals("John"));
        assert(o.getLastName().equals("Doe"));
        assert(o.getAddress().equals("123 Main St"));
        assert(o.getTelephone().equals("1234567890"));
        assert(o.getCity().equals("Springfield"));
        assert o.getPetPetList().size() == 1;
        assert o.getPetPetList().get(0).getName().equals("Fido");

      }
    }

  }

  @Test
  void testPost_InvalidRequest_ShouldReturnBadRequest() throws Exception {
    String invalidRequestBody = "{ invalid json }";

    mockMvc.perform(post("/petclinic/Pet_Owner")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testPost_InvalidRequest_ShouldReturnBadRequestNull() throws Exception {

    mockMvc.perform(post("/petclinic/Pet_Owner")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGet_ValidId_ShouldReturnOk() throws Exception {
    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put("firstName", "John");
    owner.put("lastName", "Doe");
    owner.put("address", "123 Main St");
    owner.put("telephone", "1234567890");
    owner.put("city", "Springfield");

    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post("/petclinic/Pet_Owner")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owner)));

    result.andExpect(status().isCreated());

    String id = "123";
    PETCLINICPet_OwnerDTORead dto = new PETCLINICPet_OwnerDTORead();
    when(repository.findById(id)).thenReturn(dto);

    mockMvc.perform(get("/petclinic/Pet_Owner/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void testGet_InvalidId_ShouldReturnNotFound() throws Exception {
    String id = "999";
    when(repository.findById(id)).thenReturn(null);

    mockMvc.perform(get("/petclinic/Pet_Owner/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGet_ValidArray_ShouldReturnOk() throws Exception {
    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put("firstName", "John");
    owner.put("lastName", "Doe");
    owner.put("address", "123 Main St");
    owner.put("telephone", "1234567890");
    owner.put("city", "Springfield");

    List<Map<String, Object>> owners = List.of(owner);
    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post("/petclinic/Pet_Owner")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owners)));

    result.andExpect(status().isCreated());

    String id = "123";
    PETCLINICPet_OwnerDTORead dto = new PETCLINICPet_OwnerDTORead();
    when(repository.findById(id)).thenReturn(dto);

    mockMvc.perform(get("/petclinic/Pet_Owner/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void testPut_ValidRequest_ShouldReturnOk() throws Exception {
    String id = "123";
    Map<String, Object> updatedOwner = new HashMap<>();
    updatedOwner.put("firstName", "John Updated");
    updatedOwner.put("lastName", "Doe Updated");
    updatedOwner.put("address", "456 Updated St");
    updatedOwner.put("telephone", "9876543210");
    updatedOwner.put("city", "Updated City");

    ObjectMapper mapper = new ObjectMapper();
    String requestBody = mapper.writeValueAsString(updatedOwner);

    PETCLINICPet_OwnerDTOWrite dtoWrite = new PETCLINICPet_OwnerDTOWrite();
    dtoWrite.setId(id);
    dtoWrite.setFirstName("John Updated");
    dtoWrite.setLastName("Doe Updated");
    dtoWrite.setAddress("456 Updated St");
    dtoWrite.setTelephone("9876543210");
    dtoWrite.setCity("Updated City");

    when(converter.convert(anyString())).thenReturn(dtoWrite);
    when(repository.update(dtoWrite)).thenReturn(new PETCLINICPet_OwnerDTORead());

    mockMvc.perform(put("/petclinic/Pet_Owner/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated());
  }

  @Test
  void testPut_InvalidRequest_ShouldReturnBadRequest() throws Exception {
    String requestBody = "{ \"firstName\": \"Invalid\" }";

    mockMvc.perform(put("/petclinic/Pet_Owner/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertFalse(result.getResolvedException() instanceof ResponseStatusException));
  }

  @Test
  void testPut_UpdateFails_ShouldReturnBadRequest() throws Exception {
    String id = "123";
    Map<String, Object> updatedOwner = new HashMap<>();
    updatedOwner.put("firstName", "John Updated");
    updatedOwner.put("lastName", "Doe Updated");
    updatedOwner.put("address", "456 Updated St");
    updatedOwner.put("telephone", null);
    updatedOwner.put("city", "Updated City");

    ObjectMapper mapper = new ObjectMapper();
    String requestBody = mapper.writeValueAsString(updatedOwner);

    when(converter.convert(anyString())).thenThrow(new RuntimeException("Conversion error"));

    mockMvc.perform(put("/petclinic/Pet_Owner/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result -> assertEquals("400 BAD_REQUEST \"400 BAD_REQUEST \"Error reading fields: $.telephone in conversion\"\"", result.getResolvedException().getMessage()));
  }
}
