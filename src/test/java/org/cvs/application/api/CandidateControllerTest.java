package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.CandidateService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.CandidateRepository;
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

import java.time.LocalDate;
import java.time.Month;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for {@link CandidateRepository}.
 * 
 * @author Yamiko Msosa
 *
 */
@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=validate" })
@AutoConfigureMockMvc
public class CandidateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	CandidateService candidateService;

	@Autowired
	PortfolioService portfolioService;

	Candidate candidate, candidate2, candidate3, candidate4, invalidCandidate, fetchedCandidate;

	Portfolio portfolio, portfolio2;

	@BeforeEach
	void init() {
		// Create valid candidates
		candidate = new Candidate("John", "", "Smith");
		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);

		candidate2 = new Candidate("John2", "", "Smith2");
		candidate2.setAddressLine1("Address 2");
		candidate2.setCountry("UK");
		candidate2.setGender("M");
		candidate2.setEmail("email2@email.com");
		candidate2.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate2.setRetired(Lookup.NOT_RETIRED);
		candidate2.setVoided(Lookup.NOT_VOIDED);

		candidate3 = new Candidate("John3", "", "Smith3");
		candidate3.setAddressLine1("Address 3");
		candidate3.setCountry("UK");
		candidate3.setGender("M");
		candidate3.setEmail("email3@email.com");
		candidate3.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));
		candidate3.setRetired(Lookup.NOT_RETIRED);
		candidate3.setVoided(Lookup.NOT_VOIDED);

		candidate4 = new Candidate("Jane", "", "Smith3");
		candidate4.setAddressLine1("Address 4");
		candidate4.setCountry("UK");
		candidate4.setGender("F");
		candidate4.setEmail("email4@email.com");
		candidate4.setDateOfBirth(LocalDate.of(1967, Month.JUNE, 15));
		candidate4.setRetired(Lookup.NOT_RETIRED);
		candidate4.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid new candidate
		invalidCandidate = new Candidate("", "", "");
		invalidCandidate.setGender("K");
		invalidCandidate.setDateOfBirth(LocalDate.of(2080, Month.JUNE, 15));
		invalidCandidate.setRetired(Lookup.NOT_RETIRED);
		invalidCandidate.setVoided(Lookup.NOT_VOIDED);

		// Create valid portfolios
		portfolio = new Portfolio("Portfolio Name1");
		portfolio.setRetired(Lookup.NOT_RETIRED);
		portfolio.setVoided(Lookup.NOT_VOIDED);

		portfolio2 = new Portfolio("Portfolio Name2");
		portfolio2.setRetired(Lookup.NOT_RETIRED);
		portfolio2.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithValidEntries() throws Exception {
		portfolio = portfolioService.addPortfolio(portfolio);

		candidate = candidateService.addCandidate(candidate);

		MvcResult result = mockMvc
		        .perform(
		                post("/candidates/{candidateId}/portfolios/{portfolioId}", candidate.getId(), portfolio.getId())
		                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
		                        .with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("Portfolio Name1")).andReturn();

		String response = result.getResponse().getContentAsString();

		Long portfolioId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		portfolio = portfolioService.getActivePortfolio(portfolioId);
		candidate = candidateService.getActiveCandidate(candidate.getId());

		assertTrue(candidate.getPortfolio().stream().filter(p -> p.getId() == portfolio.getId()).findFirst()
		        .orElse(null) != null);
	}

	@Test
	@WithMockUser
	public void testUpdatePortfolioWithInvalidEntries() throws Exception {
		portfolio = portfolioService.addPortfolio(portfolio);

		candidate = candidateService.addCandidate(candidate);
		mockMvc.perform(post("/candidates/{candidateId}/portfolios/{portfolioId}", candidate.getId(), -2L)
		        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();

	}

	@Test
	@WithMockUser
	public void testGetCandidates() throws Exception {
		candidateService.addCandidate(candidate);
		candidateService.addCandidate(candidate2);

		MvcResult result = mockMvc.perform(get("/candidates").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].email").value("email2@email.com")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetPortfolioCandidates() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		candidate2 = candidateService.addCandidate(candidate2);
		candidate3 = candidateService.addCandidate(candidate3);
		candidate4 = candidateService.addCandidate(candidate4);

		portfolio = portfolioService.addPortfolio(portfolio);
		portfolio2 = portfolioService.addPortfolio(portfolio2);

		portfolio = portfolioService.updatePortfolioWithCandidate(candidate.getId(), portfolio.getId());
		portfolio = portfolioService.updatePortfolioWithCandidate(candidate3.getId(), portfolio.getId());

		portfolio2 = portfolioService.updatePortfolioWithCandidate(candidate2.getId(), portfolio2.getId());
		portfolio2 = portfolioService.updatePortfolioWithCandidate(candidate4.getId(), portfolio2.getId());

		log.info("Portfolio ID --> " + portfolio.getId());
		log.info("Candidate ID --> " + candidate3.getId());
		mockMvc.perform(get("/candidates/portfolio/{portfolioId}", portfolio.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].id").value(candidate3.getId())).andReturn();

		mockMvc.perform(get("/candidates/portfolio/{portfolioId}", portfolio2.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].id").value(candidate4.getId())).andReturn();

	}

	@Test
	@WithMockUser
	public void testAddCandidate() throws Exception {
		MvcResult result = mockMvc
		        .perform(post("/candidates").content(asJsonString(candidate)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.email").value("email@email.com")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long candidateId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		candidate = candidateService.getActiveCandidate(candidateId);

		assertAll("Properties", () -> assertTrue(candidate.getId() > 0), () -> assertTrue(candidate.getId() > 0),
		        () -> assertTrue(candidate.getFirstName().equals("John")),
		        () -> assertTrue(candidate.getLastName().equals("Smith")),
		        () -> assertTrue(candidate.getCountry().equals("UK")),
		        () -> assertTrue(candidate.getAddressLine1().equals("Address 1")),
		        () -> assertTrue(candidate.getGender().equals("M")),
		        () -> assertTrue(candidate.getEmail().equals("email@email.com")),
		        () -> assertTrue(candidate.getDateOfBirth().isEqual(LocalDate.of(1987, Month.JUNE, 15))),
		        () -> assertNotNull(candidate.getCreatedDate()), () -> assertNotNull(candidate.getCreatedBy()),
		        () -> assertNotNull(candidate.getLastModifiedBy()), () -> assertNotNull(candidate.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testFindNonExistentCandidate() throws Exception {
		Long candidateId = -1L;

		mockMvc.perform(get("/candidates/active/{candidateId}", candidateId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidCandidate() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		log.info("Added candidate with ID: " + candidate.getId());

		Long candidateId = candidate.getId();

		mockMvc.perform(delete("/candidates/{candidateId}", candidateId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			candidate = candidateService.getActiveCandidate(candidateId);
		});
	}

	@Test
	@WithMockUser
	public void testRetireCandidate() throws Exception {
		candidate = candidateService.addCandidate(candidate);
		log.info("Added candidate with ID: " + candidate.getId());

		Long candidateId = candidate.getId();

		mockMvc.perform(post("/candidates/retire/{candidateId}", candidateId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			candidate = candidateService.getActiveCandidate(candidateId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		MvcResult result = null;
		result = mockMvc
		        .perform(post("/candidates").content(asJsonString(candidate)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.firstName").value("John")).andReturn();

		assertNotNull(result);
		String response = result.getResponse().getContentAsString();

		Long candidateId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		candidate = candidateService.getActiveCandidate(candidateId);

		assertAll("Properties", () -> assertTrue(candidate.getCreatedBy().equals("Peter")),
		        () -> assertTrue(candidate.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/candidates").content(asJsonString(invalidCandidate))
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
