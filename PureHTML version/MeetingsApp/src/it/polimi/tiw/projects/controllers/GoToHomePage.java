package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import it.polimi.tiw.projects.dao.MeetingDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/Home")
public class GoToHomePage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public GoToHomePage() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	
		// Retrieve username 
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		
		// Retrieve convened meetings
		List<Meeting> convenedMeetings = new ArrayList<Meeting>();
		try {
			convenedMeetings = meetingDAO.findConvenedMeetingsByUser(user.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot retrieve convened meetings");
			return;
		}
		
		// Retrieve meetings the user has been invited to
		List<Meeting> invitedMeetings = new ArrayList<Meeting>();
		try {
			invitedMeetings = meetingDAO.findInvitedMeetingsByUser(user.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot retrieve meetings you've been invited to");
			return;
		}
		
		
		// Addition of meetings to the parameters and redirect
		ctx.setVariable("invitedMeetings", invitedMeetings);
		ctx.setVariable("convenedMeetings", convenedMeetings);
		if(request.getParameter("errorMsg")!=null) {
			String errorMsg = StringEscapeUtils.escapeJava(request.getParameter("errorMsg"));
			ctx.setVariable("errorMsg", errorMsg);
		}
		String path = "/WEB-INF/Home.html";
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
