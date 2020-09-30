package org.cvs.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;

import org.cvs.application.exceptions.EntryNotActiveException;
import org.cvs.application.exceptions.EntryNotFoundException;
import org.cvs.application.services.PortfolioService;
import org.cvs.data.entities.Candidate;
import org.cvs.data.entities.Portfolio;

@Controller 
@RequestMapping(path = "/portfolios") 
public class PortfolioController {

	@Autowired
	private PortfolioService portfolioService;

	/**
	 * 
	 * Adds a new portfolio to an optional application user via POST through URL:
	 * <code>/portfolios</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *   "name": "Test portfolio 5",
	 *   "applicationUser": [{"id": 141}]
	 *	}
	 * </code>
	 * 
	 * @param portfolio the portfolio (can be a JSON payload) to be added to the
	 *                  system.
	 * 
	 * @return the newly added portfolio
	 */
	@PostMapping(path = "")
	public @ResponseBody Portfolio addNewPortfolio(@RequestBody Portfolio portfolio) {

		try {
			Portfolio newPortfolio = portfolioService.addPortfolio(portfolio);
			return newPortfolio;
		} catch (ConstraintViolationException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active portfolio via GET through URL:
	 * <code>/portfolios/active/{portfolioId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /portfolios/active/1
	 * </code>
	 * 
	 * @param portfolioId the portfolio ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active portfolio if found
	 */
	@GetMapping(path = "/active/{portfolioId}")
	public @ResponseBody Portfolio getPortfolio(@PathVariable Long portfolioId) {
		try {
			Portfolio portfolio = portfolioService.getActivePortfolio(portfolioId);
			return portfolio;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Fetches an active portfolio via GET through URL:
	 * <code>/portfolios/name</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /portfolios/name?name=test5
	 * </code>
	 * 
	 * @param name the portfolio name as a request parameter to be used in the query
	 * 
	 * @return an active portfolio if found
	 */
	@GetMapping(path = "/name")
	public @ResponseBody Portfolio getByPortfolioName(@RequestParam String name) {
		try {
			Portfolio portfolio = portfolioService.getByPortfolioName(name);
			return portfolio;
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (EntryNotActiveException e) {
			throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Deletes a portfolio via DELETE method through base URL:
	 * <code>/portfolios/{portfolioId}</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /portfolios/1
	 * </code>
	 * 
	 * @param portfolioId the portfolio (can be a JSON payload) to be deleted from
	 *                    the system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "{portfolioId}")
	public @ResponseBody String deletePortfolio(@PathVariable Long portfolioId) {
		try {
			portfolioService.deletePortfolio(portfolioId);
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a portfolio via POST through URL:
	 * <code>/portfolios/retire/{portfolioId}</code>.
	 * 
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /portfolios/retire/1
	 * </code>
	 * 
	 * @param portfolioId the ID of the portfolio to be retired from the system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire/{portfolioId}")
	public @ResponseBody String retirePortfolio(@PathVariable Long portfolioId) {

		try {
			portfolioService.retirePortfolio(portfolioId);
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active portfolios via GET through URL: <code>/portfolios</code>.
	 * 
	 * 
	 */
	@GetMapping(path = "")
	public @ResponseBody Iterable<Portfolio> getAllPortfolios() {
		// This returns a JSON or XML with the portfolios
		return portfolioService.getPortfolios();
	}

	/**
	 * 
	 * Fetches active portfolios for a specific candidate via GET through URL:
	 * <code>/portfolios/users/{userId}</code>.
	 * 
	 * @param userId the ID of the user to filter portfolios for
	 * 
	 * @return a list of all active portfolio entries for a particular user
	 * 
	 */
	@GetMapping(path = "/user/{userId}")
	public @ResponseBody Iterable<Portfolio> getPortfolios(@PathVariable Long userId) {
		// This returns a JSON or XML with the workExperiences
		return portfolioService.getPortfolios(userId);
	}

}
