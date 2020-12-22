package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckCredentials")
public class CheckCredentials extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public CheckCredentials() {
		super();
	}

	public void init() throws ServletException {
		
		// Set the connection
		connection = ConnectionHandler.getConnection(getServletContext());
		
		// Set the template resolver
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Parameter escaping 
		String username = null;
		String password = null;
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		} catch (Exception e) {
			sendErrorMessage("Username and password cannot be empty",request,response);
			return;
		}

		// Check credentials
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(username, password);
		} catch (SQLException e) {
			sendErrorMessage("Unable to check credentials",request,response);
			return;
		}

		if (user == null) {
			if (username==null || password==null || username.isEmpty() || password.isEmpty()) {
				sendErrorMessage("Username and password cannot be empty",request,response);
			} else {
				sendErrorMessage("Incorrect username or password",request,response);
			}
		} else {
			request.getSession().setAttribute("user", user);
			String path = getServletContext().getContextPath() + "/Home";
			response.sendRedirect(path);
		}

	}
	
	/**
	 * Sends an error message to the user
	 * @param errorMessage	The error message to be shown
	 */
	private void sendErrorMessage(String errorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("errorMsg", errorMessage);
	
		String path = "/index.html";
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
