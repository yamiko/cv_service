package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.ReferenceService;
import org.cvs.application.services.CandidateService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.Reference;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.ReferenceRepository;
import org.cvs.utils.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.Month;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for {@link ReferenceRepository}.
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
@AutoConfigureMockMvc
public class ReferenceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	ReferenceService referenceService;

	@Autowired
	CandidateService candidateService;

	Candidate candidate, candidate2;

	Reference reference, reference2, invalidReference, fetchedReference;

	@BeforeEach
	void init() {
		// Create valid references
		reference = new Reference("John Malkovich", "Professor of History Studies", "University of Kent",
		        "test@test.com");
		reference.setContactNumber("0893453234");
		reference.setAddressLine1("Address 1");
		reference.setCountry("UK");
		reference.setRetired(Lookup.NOT_RETIRED);
		reference.setVoided(Lookup.NOT_VOIDED);

		reference2 = new Reference("Tom Hanks", "Dean of History Studies", "University of Warwick", "test2@test.com");
		reference2.setContactNumber("0893453234");
		reference2.setAddressLine1("Address 1");
		reference2.setCountry("UK");
		reference2.setRetired(Lookup.NOT_RETIRED);
		reference2.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid reference
		invalidReference = new Reference("", "", "", "someemail");

		invalidReference.setRetired(Lookup.NOT_RETIRED);
		invalidReference.setVoided(Lookup.NOT_VOIDED);

		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
		
		candidate2 = new Candidate("John2", "", "Smith2");
		candidate2.setAddressLine1("Address 1");
		candidate2.setCountry("UK");
		candidate2.setGender("M");
		candidate2.setEmail("email2@email.com");
		candidate2.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate2.setRetired(Lookup.NOT_RETIRED);
		candidate2.setVoided(Lookup.NOT_VOIDED);				
	}

	@Test
	@WithMockUser
	public void testGetReferences() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		reference.setCandidate(candidate);
		referenceService.addReference(reference);

		reference2.setCandidate(candidate);
		referenceService.addReference(reference2);

		MvcResult result = mockMvc.perform(get("/references").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].name").value("Tom Hanks")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetCandidateReferences() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		candidate2 = candidateService.addCandidate(candidate2);

		reference.setCandidate(candidate);
		referenceService.addReference(reference);

		reference2.setCandidate(candidate2);
		referenceService.addReference(reference2);


		MvcResult result = mockMvc
		        .perform(get("/references/candidate/{candidateId}", candidate2.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[0].candidate.id").value(candidate2.getId())).andReturn();

		assertNotNull(result);
	}	
	
	@Test
	@WithMockUser
	public void testAddReference() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		reference.setCandidate(candidate);

		MvcResult result = mockMvc
		        .perform(post("/references").content(asJsonString(reference)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("John Malkovich")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long referenceId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		reference = referenceService.getActiveReference(referenceId);

		assertAll("Properties", () -> assertTrue(reference.getId() > 0), () -> assertTrue(reference.getId() > 0),
		        () -> assertTrue(reference.getName().equals("John Malkovich")),
		        () -> assertTrue(reference.getInstitution().equals("University of Kent")),
		        () -> assertTrue(reference.getJobTitle().equals("Professor of History Studies")),
		        () -> assertTrue(reference.getEmail().equals("test@test.com")),
		        () -> assertTrue(reference.getAddressLine1().equals("Address 1")),
		        () -> assertTrue(reference.getCountry().equals("UK")), () -> assertNotNull(reference.getCreatedDate()),
		        () -> assertNotNull(reference.getCreatedBy()), () -> assertNotNull(reference.getLastModifiedBy()),
		        () -> assertNotNull(reference.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testAddWithoutCandidateReference() throws Exception {
		mockMvc.perform(post("/references").content(asJsonString(reference)).contentType(MediaType.APPLICATION_JSON)
		        .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader())).andExpect(status().isNotFound())
		        .andReturn();
	}

	@Test
	@WithMockUser
	public void testFindNonExistentReference() throws Exception {
		Long referenceId = -1L;

		mockMvc.perform(get("/references/active/{referenceId}", referenceId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidReference() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);
		log.info("Added reference with ID: " + reference.getId());

		Long referenceId = reference.getId();

		mockMvc.perform(delete("/references/{referenceId}", referenceId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			reference = referenceService.getActiveReference(referenceId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireReference() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);
		log.info("Added reference with ID: " + reference.getId());

		Long referenceId = reference.getId();

		mockMvc.perform(post("/references/retire/{referenceId}", referenceId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			reference = referenceService.getActiveReference(referenceId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		candidate = candidateService.addCandidate(candidate);

		reference.setCandidate(candidate);
		reference = referenceService.addReference(reference);
		log.info("Added reference with ID: " + reference.getId());

		MvcResult result = mockMvc
		        .perform(post("/references").content(asJsonString(reference)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("John Malkovich")).andReturn();

		String response = result.getResponse().getContentAsString();

		Long referenceId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		reference = referenceService.getActiveReference(referenceId);

		assertAll("Properties", () -> assertTrue(reference.getCreatedBy().equals("Peter")),
		        () -> assertTrue(reference.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/references").content(asJsonString(invalidReference))
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotAcceptable()).andReturn();
	}

	public static String asJsonString(final Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
