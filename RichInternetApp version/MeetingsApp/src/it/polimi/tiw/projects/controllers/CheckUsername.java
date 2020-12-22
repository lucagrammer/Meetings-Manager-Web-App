package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckUsername")
@MultipartConfig
public class CheckUsername extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckUsername() {
		super();
	}

	public void init() throws ServletException {
		
		// Set the connection
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Parameter escaping 
		String username = null;
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		if(username==null || username=="") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid username");
			return;
		}
		

		// Check credentials
		UserDAO userDao = new UserDAO(connection);
		boolean isAvailable;
		try {
			isAvailable = userDao.isAvailableUsername(username);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Failed to check the username");
			return;
		}

		if (isAvailable) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("Available");
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Unavailable");
		}
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
