package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.Meeting;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/Anagrafica")
public class GoToAnagrafica extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public GoToAnagrafica() {
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

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		// No meeting created? Redirect to the home page
		HttpSession session = request.getSession();
		Meeting meeting= (Meeting)session.getAttribute("newMeeting");
		if (meeting == null) {
			request.setAttribute("errorMsg", "To invite users, you must first create a new  event");
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Home");
			dispatcher.forward(request, response);
			return;
		}

		// Set users list
		UserDAO userDAO= new UserDAO(connection);
		ArrayList<String> users;
		try {
			users =(ArrayList<String>) request.getAttribute("selectedUser");
			if(users!=null) {
				ctx.setVariable("selected", true);
			}else {
				users= userDAO.findAllUsersExcept(((User)session.getAttribute("user")).getUsername());
			}
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot retrieve users");
			return;
		}
		if (users.size() == 0) {
			request.setAttribute("errorMsg", "There aren't users to invite to the meeting. Try later.");
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Home");
			dispatcher.forward(request, response);
			return;
		}
		ctx.setVariable("users", users);
		
		// Redirect
		if(request.getParameter("errorMsg")!=null) {
			String errorMsg = StringEscapeUtils.escapeJava(request.getParameter("errorMsg"));
			ctx.setVariable("errorMsg", errorMsg);
		}
		String anagraficapath = "/WEB-INF/Anagrafica.html";
		templateEngine.process(anagraficapath, ctx, response.getWriter());
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
