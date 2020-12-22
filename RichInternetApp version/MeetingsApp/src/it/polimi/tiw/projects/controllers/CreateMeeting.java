package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.MeetingDAO;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.beans.Meeting;
import it.polimi.tiw.projects.beans.User;

@WebServlet("/CreateMeeting")
@MultipartConfig

public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateMeeting() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Get and parse all parameters from request
		boolean isBadRequest=false;
		String creator=((User)request.getSession().getAttribute("user")).getUsername();
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
			isBadRequest = checkParameters(title,date,time,duration,maxParticipants);
		} catch (Exception e) {
			isBadRequest = true;
		}
		
		// Any error?
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
		}
		
		// Store all the meeting info into an object
		Meeting meeting= new Meeting();
		meeting.setTitle(title);
		meeting.setDate(date);
		meeting.setTime(time);
		meeting.setDuration(duration);
		meeting.setCreator(creator);
		meeting.setMaxParticipants(maxParticipants);
		
		
		// Retrieve the guests from the request and check they exist
		UserDAO userDAO= new UserDAO(connection);
		ArrayList<String> allowedNicknames;
		try {
			allowedNicknames= userDAO.findAllUsersExcept(creator);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not check users list");
			return;
		}	
		
		String[] checkedUsernames= request.getParameterValues("guests");
		ArrayList<String> guests= new ArrayList<>();
		for(String username: checkedUsernames) {
			if(!allowedNicknames.contains(username)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid users");
				return;
			}
			guests.add(username);
		}
				
		// Zero or too many guests?
		if(guests.size()<1 || guests.size()-maxParticipants>0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Zero or too many guests selected");
			return;
		}	
	
		// Add the meeting to the database 
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		Integer assignedId;
		try {
			assignedId = meetingDAO.createMeeting(meeting);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not create meeting");
			return;
		}
		
		// Add the invitations to the database 
		try {
			for(String guest: guests)
				meetingDAO.addInvitation(assignedId,guest);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not invite all the selected users");
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	}
	
	/**
	 * Check the parameters
	 */
	public boolean checkParameters(String title, java.sql.Date date, java.sql.Time time, Integer duration,Integer maxParticipants) {
		boolean isBadRequest=false;
		try {
			if(duration<1 || maxParticipants<1 || title==null || title.isEmpty()) {
				isBadRequest=true;
			}else{
				Calendar now = Calendar.getInstance();
			    Date currentDate = new Date((now.getTime()).getTime());
			    Time currentTime = new Time((now.getTime()).getTime());
			    Date sqlCurrentDate = Date.valueOf(currentDate.toString());
			    Time sqlCurrentTime = Time.valueOf(currentTime.toString());
				if(date.compareTo(sqlCurrentDate)<0) {
					isBadRequest=true;
				}
				else {
					if(date.compareTo(sqlCurrentDate)==0 && time.compareTo(sqlCurrentTime)<=0) {
						isBadRequest=true;
					}
				}
			}
		} catch (Exception e) {
			isBadRequest=true;
		}
		return isBadRequest;
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
