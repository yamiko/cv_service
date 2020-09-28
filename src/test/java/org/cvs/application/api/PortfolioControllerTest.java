package org.cvs.application.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.Month;

import javax.transaction.Transactional;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.ApplicationUserService;
import org.cvs.application.services.CandidateService;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.ApplicationUser;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;
import org.cvs.data.repositories.ApplicationUserRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for {@link ApplicationUserRepository}.
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
public class PortfolioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	PortfolioService portfolioService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	ApplicationUserService userService;

	Portfolio portfolio, portfolio2, portfolio3, portfolio4, invalidPortfolio, fetchedPortfolio;

	ApplicationUser user, user2;

	Candidate candidate;

	@BeforeEach
	void init() {
		// Create valid portfolios
		portfolio = new Portfolio("Portfolio Name1");
		portfolio.setRetired(Lookup.NOT_RETIRED);
		portfolio.setVoided(Lookup.NOT_VOIDED);

		portfolio2 = new Portfolio("Portfolio Name2");
		portfolio2.setRetired(Lookup.NOT_RETIRED);
		portfolio2.setVoided(Lookup.NOT_VOIDED);

		portfolio3 = new Portfolio("Portfolio Name3");
		portfolio3.setRetired(Lookup.NOT_RETIRED);
		portfolio3.setVoided(Lookup.NOT_VOIDED);

		portfolio4 = new Portfolio("Portfolio Name4");
		portfolio4.setRetired(Lookup.NOT_RETIRED);
		portfolio4.setVoided(Lookup.NOT_VOIDED);

		// Create an invalid portfolio
		invalidPortfolio = new Portfolio("");
		invalidPortfolio.setRetired(Lookup.NOT_RETIRED);
		invalidPortfolio.setVoided(Lookup.NOT_VOIDED);

		// Create valid users
		user = new ApplicationUser("test1", "password1", "Test User1");
		user.setRetired(Lookup.NOT_RETIRED);
		user.setVoided(Lookup.NOT_VOIDED);

		user2 = new ApplicationUser("test2", "password2", "Test User2");
		user2.setRetired(Lookup.NOT_RETIRED);
		user2.setVoided(Lookup.NOT_VOIDED);

		// Create a valid candidate
		candidate = new Candidate("John", "", "Smith");

		candidate.setAddressLine1("Address 1");
		candidate.setCountry("UK");
		candidate.setGender("M");
		candidate.setEmail("email@email.com");
		candidate.setDateOfBirth(LocalDate.of(1987, Month.JUNE, 15));

		candidate.setRetired(Lookup.NOT_RETIRED);
		candidate.setVoided(Lookup.NOT_VOIDED);
	}

	@Test
	@WithMockUser
	public void testGetPortfolios() throws Exception {
		portfolioService.addPortfolio(portfolio);
		portfolioService.addPortfolio(portfolio2);

		MvcResult result = mockMvc.perform(get("/portfolios").with(csrf().asHeader())).andExpect(status().isOk())
		        .andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].name").value("Portfolio Name2")).andReturn();

		assertNotNull(result);
	}

	@Test
	@WithMockUser
	public void testGetUserPortfolios() throws Exception {
		portfolio = portfolioService.addPortfolio(portfolio);
		portfolio2 = portfolioService.addPortfolio(portfolio2);
		portfolio3 = portfolioService.addPortfolio(portfolio3);
		portfolio4 = portfolioService.addPortfolio(portfolio4);

		
		user = userService.addUser(user);
		user2 = userService.addUser(user2);
		
		portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio.getId());
		portfolio = portfolioService.updatePortfolioWithUser(user.getId(), portfolio3.getId());

		portfolio2 = portfolioService.updatePortfolioWithUser(user2.getId(), portfolio2.getId());
		portfolio2 = portfolioService.updatePortfolioWithUser(user2.getId(), portfolio4.getId());

		log.info("Portfolio ID --> " + portfolio3.getId());
		log.info("User ID --> " + user.getId());
		mockMvc.perform(get("/portfolios/user/{userId}", user.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].id").value(portfolio3.getId())).andReturn();

		mockMvc.perform(get("/portfolios/user/{userId}", user2.getId()).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.[1].id").value(portfolio4.getId())).andReturn();

	}
	
	
	@Test
	@WithMockUser
	public void testAddPortfolio() throws Exception {
		MvcResult result = mockMvc
		        .perform(post("/portfolios").content(asJsonString(portfolio)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("Portfolio Name1")).andReturn();

		assertNotNull(result);

		String response = result.getResponse().getContentAsString();

		Long portfolioId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		portfolio = portfolioService.getActivePortfolio(portfolioId);

		assertAll("Properties", () -> assertTrue(portfolio.getId() > 0),
		        () -> assertTrue(portfolio.getName().equals("Portfolio Name1")),
		        () -> assertNotNull(portfolio.getCreatedDate()), () -> assertNotNull(portfolio.getCreatedBy()),
		        () -> assertNotNull(portfolio.getLastModifiedBy()), () -> assertNotNull(portfolio.getModifiedDate()));
	}

	@Test
	@WithMockUser
	public void testFindNonExistentPortfolio() throws Exception {
		Long portfolioId = -1L;

		mockMvc.perform(get("/portfolios/active/{portfolioId}", portfolioId).with(csrf().asHeader()))
		        .andExpect(status().isNotFound()).andReturn();
	}

	@Test
	@WithMockUser
	public void testVoidPortfolio() throws Exception {
		portfolio = portfolioService.addPortfolio(portfolio);
		log.info("Added portfolio with ID: " + portfolio.getId());

		Long portfolioId = portfolio.getId();

		mockMvc.perform(delete("/portfolios/{portfolioId}", portfolioId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotFoundException.class, () -> {
			portfolio = portfolioService.getActivePortfolio(portfolioId);
		});
	}

	@Test
	@WithMockUser
	public void testRetirePortfolio() throws Exception {
		portfolio = portfolioService.addPortfolio(portfolio);
		log.info("Added portfolio with ID: " + portfolio.getId());

		Long portfolioId = portfolio.getId();

		mockMvc.perform(post("/portfolios/retire/{portfolioId}", portfolioId).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andReturn();

		assertThrows(EntryNotActiveException.class, () -> {
			portfolio = portfolioService.getActivePortfolio(portfolioId);
		});
	}

	@Test
	@WithMockUser("Peter")
	public void testAuditing() throws Exception {
		MvcResult result = null;
		result = mockMvc
		        .perform(post("/portfolios").content(asJsonString(portfolio)).contentType(MediaType.APPLICATION_JSON)
		                .accept(MediaType.APPLICATION_JSON).with(csrf().asHeader()))
		        .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
		        .andExpect(jsonPath("$.name").value("Portfolio Name1")).andReturn();

		assertNotNull(result);
		String response = result.getResponse().getContentAsString();

		Long portfolioId = ((Integer) (JsonPath.parse(response).read("$.id"))).longValue();
		portfolio = portfolioService.getActivePortfolio(portfolioId);

		assertAll("Properties", () -> assertTrue(portfolio.getCreatedBy().equals("Peter")),
		        () -> assertTrue(portfolio.getLastModifiedBy().equals("Peter")));
	}

	@Test
	@WithMockUser
	public void testConstraintViolations() throws Exception {
		mockMvc.perform(post("/portfolios").content(asJsonString(invalidPortfolio))
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
