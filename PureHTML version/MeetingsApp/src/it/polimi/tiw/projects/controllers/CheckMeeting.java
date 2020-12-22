package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Date;
import java.util.Calendar;

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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.beans.Meeting;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckMeeting")
public class CheckMeeting extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public CheckMeeting() {
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
		
		// Get and parse all parameters from request
		String errorMessage = null;
		Date date = null;
		Time time = null;
		String title = null;
		Integer maxParticipants = null;
		Integer duration = null;
		try {
			duration = Integer.parseInt(request.getParameter("duration"));
			maxParticipants = Integer.parseInt(request.getParameter("maxParticipants"));
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
		    time = Time.valueOf(request.getParameter("time").concat(":00"));
			date = Date.valueOf(request.getParameter("date"));
			errorMessage = checkParameters(title,date,time,duration,maxParticipants);
		} catch (Exception e) {
			errorMessage = "Please check the entered data";
		}
		
		// Any error?
		if (errorMessage!=null) {
			request.setAttribute("errorMsg", errorMessage);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Home");
			dispatcher.forward(request, response);
			return;
		}
		
		// Add meeting info to the session
		HttpSession session = request.getSession();
		Meeting meeting= new Meeting();
		meeting.setDuration(duration);
		meeting.setDate(date);
		meeting.setTime(time);
		meeting.setMaxParticipants(maxParticipants);
		meeting.setTitle(title);
		meeting.setCreator(((User) session.getAttribute("user")).getUsername());
		session.setAttribute("newMeeting",meeting );
		session.removeAttribute("attempts");
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/Anagrafica");
		dispatcher.forward(request, response);
		return;
	}
	
	/**
	 * Check the parameters
	 */
	public String checkParameters(String title, java.sql.Date date, java.sql.Time time, Integer duration,Integer maxParticipants) {
		String errorMessage=null;
		try {
			if(duration<1) {
				errorMessage="Duration must be >=1";
			}else {
				if(maxParticipants<1) {
					errorMessage="Max Participants must be >=1";
				}else {
					if(title==null || title.isEmpty()) {
						errorMessage="Title can't be empty";
					}else {
						Calendar now = Calendar.getInstance();
					    Date currentDate = new Date((now.getTime()).getTime());
					    Time currentTime = new Time((now.getTime()).getTime());
					    Date sqlCurrentDate = Date.valueOf(currentDate.toString());
					    Time sqlCurrentTime = Time.valueOf(currentTime.toString());
						if(date.compareTo(sqlCurrentDate)<0) {
							errorMessage="Invalid date";
						}
						else {
							if(date.compareTo(sqlCurrentDate)==0 && time.compareTo(sqlCurrentTime)<=0) {
								errorMessage="Invalid time";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			errorMessage="Invalid data";
		}
		return errorMessage;
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
