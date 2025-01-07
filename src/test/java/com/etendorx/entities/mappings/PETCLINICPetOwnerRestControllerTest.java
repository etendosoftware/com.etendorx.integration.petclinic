package com.etendorx.entities.mappings;

import com.etendoerp.etendorx.data.Connector;
import com.etendoerp.etendorx.data.InstanceConnector;
import com.etendoerp.integration.petclinic.Owner;
import com.etendorx.das.EtendorxDasApplication;
import com.etendorx.entities.jparepo.ADClientRepository;
import com.etendorx.entities.jparepo.ADUserRepository;
import com.etendorx.entities.jparepo.ETRX_ConnectorRepository;
import com.etendorx.entities.jparepo.ETRX_Instance_ConnectorRepository;
import com.etendorx.entities.jparepo.ETRX_instance_externalidRepository;
import com.etendorx.entities.jparepo.OrganizationRepository;
import com.etendorx.entities.jparepo.Pet_OwnerRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
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
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String ADDRESS = "address";
  public static final String TELEPHONE = "telephone";
  public static final String CITY = "city";

  @Value("${petclinic.path-owner}")
  private String pathOwner;

  public String getPathOwnerWithId() {
    return pathOwner + "/{id}";
  }

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

  /**
   * Setup the controller with the mock repository
   */
  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Delete all pet owners before each test
   */
  @BeforeEach
  public void deleteAll() {
    petOwnerRepository.deleteAll();
  }

  /**
   * Setup the instance connector for the user context
   */
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
  void testPostValidRequestShouldReturnCreated() throws Exception {

    log.info("Instance connector id: {}", userContext.getExternalSystemId());

    Map<String, Object> pet = new HashMap<>();
    pet.put("id", "345");
    pet.put("name", "Fido");
    pet.put("birthDate", "2021-01-01");
    pet.put("type", "Dog");

    String springfield = "Springfield 123";

    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put(FIRST_NAME, "John");
    owner.put(LAST_NAME, "Doe");
    owner.put(ADDRESS, "124 Main St");
    owner.put(TELEPHONE, "1234567892");
    owner.put(CITY, springfield);
    owner.put("pets", List.of(pet));

    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post(pathOwner)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owner)));

    result.andExpect(status().isCreated());

    Iterable<Owner> owners = petOwnerRepository.findAll();

    // Lookup owner by name and compare with example
    for (Owner o : owners) {
      if (o.getFirstName().equals("John") && o.getLastName().equals("Doe")) {
        assert(o.getAddress().equals("124 Main St"));
        assert(o.getTelephone().equals("1234567892"));
        assert(o.getCity().equals(springfield));
        assert o.getPetPetList().size() == 1;
        assert o.getPetPetList().get(0).getName().equals("Fido");
      }
    }

  }

  @Test
  void testPostInvalidRequestShouldReturnBadRequestNull() throws Exception {

    mockMvc.perform(post(pathOwner)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetValidIdShouldReturnOk() throws Exception {
    String springfield = "Springfield 456";

    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put(FIRST_NAME, "John");
    owner.put(LAST_NAME, "Doe");
    owner.put(ADDRESS, "123 Main St");
    owner.put(TELEPHONE, "1234567890");
    owner.put(CITY, springfield);

    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post(pathOwner)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owner)));

    result.andExpect(status().isCreated());

    String id = "123";
    PETCLINICPet_OwnerDTORead dto = new PETCLINICPet_OwnerDTORead();
    when(repository.findById(id)).thenReturn(dto);

    mockMvc.perform(get(getPathOwnerWithId(), id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void testGetInvalidIdShouldReturnNotFound() throws Exception {
    String id = "999";
    when(repository.findById(id)).thenReturn(null);

    mockMvc.perform(get(getPathOwnerWithId(), id))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetValidArrayShouldReturnOk() throws Exception {
    String springfield = "Springfield 567";

    Map<String, Object> owner = new HashMap<>();
    owner.put("id", "123");
    owner.put(FIRST_NAME, "John");
    owner.put(LAST_NAME, "Doe");
    owner.put(ADDRESS, "123 Main St");
    owner.put(TELEPHONE, "1234567890");
    owner.put(CITY, springfield);

    List<Map<String, Object>> owners = List.of(owner);
    ObjectMapper mapper = new ObjectMapper();

    when(converter.convert(anyString())).thenReturn(new PETCLINICPet_OwnerDTOWrite());

    ResultActions result = mockMvc.perform(post(pathOwner)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(owners)));

    result.andExpect(status().isCreated());

    String id = "123";
    PETCLINICPet_OwnerDTORead dto = new PETCLINICPet_OwnerDTORead();
    when(repository.findById(id)).thenReturn(dto);

    mockMvc.perform(get(getPathOwnerWithId(), id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void testPutValidRequestShouldReturnOk() throws Exception {
    String id = "123";
    Map<String, Object> updatedOwner = new HashMap<>();
    String johnUpdated = "John Updated";
    String doeUpdated = "Doe Updated";
    String address = "456 Updated St";
    String telephone = "9876543210";
    String updatedCity = "Updated City";

    updatedOwner.put(FIRST_NAME, johnUpdated);
    updatedOwner.put(LAST_NAME, doeUpdated);
    updatedOwner.put(ADDRESS, address);
    updatedOwner.put(TELEPHONE, telephone);
    updatedOwner.put(CITY, updatedCity);

    ObjectMapper mapper = new ObjectMapper();
    String requestBody = mapper.writeValueAsString(updatedOwner);

    PETCLINICPet_OwnerDTOWrite dtoWrite = new PETCLINICPet_OwnerDTOWrite();
    dtoWrite.setId(id);
    dtoWrite.setFirstName(johnUpdated);
    dtoWrite.setLastName(doeUpdated);
    dtoWrite.setAddress(address);
    dtoWrite.setTelephone(telephone);
    dtoWrite.setCity(updatedCity);

    when(converter.convert(anyString())).thenReturn(dtoWrite);
    when(repository.update(dtoWrite)).thenReturn(new PETCLINICPet_OwnerDTORead());

    mockMvc.perform(put(getPathOwnerWithId(), id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated());
  }

  @Test
  void testPutInvalidRequestShouldReturnBadRequest() throws Exception {
    String requestBody = "{ \"firstName\": \"Invalid\" }";

    mockMvc.perform(put(pathOwner + "/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertFalse(result.getResolvedException() instanceof ResponseStatusException));
  }
}
