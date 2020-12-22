package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.beans.User;

@WebServlet("/CreateAccount")
@MultipartConfig

public class CreateAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateAccount() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Get and parse all parameters from request
		boolean isBadRequest=false;
		String username = null;
		String name = null;
		String surname = null;
		String password = null;
		String passwordR = null;
		String mail = null;
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			passwordR = StringEscapeUtils.escapeJava(request.getParameter("passwordR"));
			mail = StringEscapeUtils.escapeJava(request.getParameter("email"));
			//System.out.println("UserOk: "+checkUsername(username,name,surname)+" PwdOK: "+checkPassword(password,passwordR) + "MailOK"+checkMail(mail));
			isBadRequest= !(checkUsername(username,name,surname) && checkPassword(password,passwordR) && checkMail(mail));
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not check the username");
			return;
		}
		catch (Exception e) {
			isBadRequest = true;
		}
		
		// Any error?
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
		}
		
		// Store all the user info into an object
		User user= new User();
		user.setName(name);
		user.setSurname(surname);
		user.setUsername(username);
		user.setMail(mail);
		user.setPassword(password);
		
		// Add it to the DB
		UserDAO userDAO= new UserDAO(connection);
		try {
			userDAO.createUser(user);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not create an account");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Success");
	}
	
	
	/**
	 * Check if a string ia a valid mail 
	 * @param passwordR	The mail field
	 * @return	True if and only if the string is a valid mail 
	 */
	private boolean checkMail(String mail) {
		return mail!=null && !mail.isEmpty() && !mail.isBlank() && mail.contains("@") && !mail.endsWith("@") && mail.contains(".");
	}

	/**
	 * Check if a string ia a valid password 
	 * @param passwordR	The first password field
	 * @param passwordR	The other password field
	 * @return	True if and only if the string is a valid password 
	 */
	private boolean checkPassword(String password, String passwordR) {
		return password!=null && passwordR!=null && !password.isBlank() && password.equals(passwordR) && password.length()>=4;
	}

	/**
	 * Check if a strings are valid username, name and surname
	 * @param username	The username string to be checked
	 * @param surname 	The name string to be checked
	 * @param name 		The username string to be checked
	 * @return	True if and only if the strings are valid
	 */
	private boolean checkUsername(String username, String name, String surname) throws SQLException {
		UserDAO userDAO = new UserDAO(connection);
		boolean userOK = username!=null && !username.isEmpty() && !username.isBlank() && userDAO.isAvailableUsername(username);
		boolean nameOK = name!=null && !name.isEmpty() && !name.isBlank();
		boolean surnameOK = surname!=null && !surname.isEmpty() && !surname.isBlank();
		return  userOK && nameOK && surnameOK;
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
