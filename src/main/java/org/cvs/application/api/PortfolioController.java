package org.cvs.application.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import org.cvs.data.entities.Portfolio;

@Controller // This means that this class is a Controller
@RequestMapping(path = "/portfolios") // This means URL's start with /demo (after Application path)
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
	 * <code>/portfolios/active</code>.
	 * <p>
	 * 
	 * Example URL:
	 * 
	 * <code> 
	 *  /portfolios/active?portfolioId=1
	 * </code>
	 * 
	 * @param portfolioId the portfolio ID as a request parameter to be used in the
	 *                    query
	 * 
	 * @return an active portfolio if found
	 */
	@GetMapping(path = "/active")
	public @ResponseBody Portfolio getPortfolio(@RequestParam Long portfolioId) {
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
	 * @param name the portfolio name as a request parameter to be used in the
	 *                    query
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
	 * <code>/portfolios</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *     "id" : 1
	 * }
	 * </code>
	 * 
	 * @param portfolio the portfolio (can be a JSON payload) to be deleted from the
	 *                  system.
	 * 
	 * @return a string that says 'Deleted'
	 * 
	 */
	@DeleteMapping(path = "")
	public @ResponseBody String deletePortfolio(@RequestBody Portfolio portfolio) {
		try {
			portfolioService.deletePortfolio(portfolio.getId());
			return "Deleted";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Retires a portfolio via POST through URL: <code>/portfolios/retire</code>.
	 * <p>
	 * 
	 * Example payload:
	 * 
	 * <code> 
	 * {
	 *     "id" : 1
	 * }
	 * </code>
	 * 
	 * @param portfolio the portfolio (can be a JSON payload) to be retired from the
	 *                  system.
	 * 
	 * @return a string that says 'Retired'
	 */
	@PostMapping(path = "/retire")
	public @ResponseBody String retirePortfolio(@RequestBody Portfolio portfolio) {

		try {
			portfolioService.retirePortfolio(portfolio.getId());
			return "Retired";
		} catch (EntryNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * Fetches all active portfolios via GET through URL:
	 * <code>/portfolios/all</code>.
	 * 
	 * 
	 */
	@GetMapping(path = "/all")
	public @ResponseBody Iterable<Portfolio> getAllPortfolios() {
		// This returns a JSON or XML with the portfolios
		return portfolioService.getPortfolios();
	}

}
