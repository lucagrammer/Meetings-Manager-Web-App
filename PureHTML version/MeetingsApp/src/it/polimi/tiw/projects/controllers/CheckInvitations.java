package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.Meeting;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.MeetingDAO;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckInvitations")
public class CheckInvitations extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public CheckInvitations() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
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
		
		// Retrieve the guests from the request
		Map<String,String[]> users= request.getParameterMap();
		ArrayList<String> guests= new ArrayList<>();
		for(String username: users.keySet()) {
			if(users.get(username)[0].equals("on")) {
				guests.add(username);
			}
		}
		
		// Check if the users exist
		UserDAO userDAO= new UserDAO(connection);
		ArrayList<String> allowedNicknames;
		try {
			allowedNicknames= userDAO.findAllUsersExcept(((User)session.getAttribute("user")).getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not check users list");
			return;
		}
		if(!allowedNicknames.containsAll(guests)){
			request.setAttribute("errorMsg", "Invalid users");
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Anagrafica");
			dispatcher.forward(request, response);
			return;
		}
		
		// No guests? 
		if(guests.size()<1) {
			request.setAttribute("errorMsg", "Select at least one user!");
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Anagrafica");
			dispatcher.forward(request, response);
			return;
		}
		
		// Too many guests?
		int diff=guests.size()-meeting.getMaxParticipants();
		if(diff>0){
			Integer attempts=(Integer) session.getAttribute("attempts");
			if(attempts==null) {
				attempts=1;
			}else {
				attempts++;
			}
			session.setAttribute("attempts", attempts);
			
			if(attempts==3) {
				session.removeAttribute("attempts");
				session.removeAttribute("newMeeting");
				templateEngine.process("/WEB-INF/Cancellazione.html", ctx, response.getWriter());
			}else {
				request.setAttribute("errorMsg", "Too many users selected, remove at least "+diff+" users!");
				request.setAttribute("selectedUser", guests);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/Anagrafica");
				dispatcher.forward(request, response);
			}
			return;
		}
		
	
		// Add the meeting to the database 
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		Integer assignedId;
		try {
			assignedId = meetingDAO.createMeeting(meeting);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not create meeting");
			return;
		}
		
		// Add the invitations to the database 
		try {
			for(String guest: guests)
				meetingDAO.addInvitation(assignedId,guest);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not invite all the selected users");
			return;
		}

		// Redirect to the Home
		session.removeAttribute("attempts");
		session.removeAttribute("newMeeting");
		String path = getServletContext().getContextPath() + "/Home";
		response.sendRedirect(path);
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
