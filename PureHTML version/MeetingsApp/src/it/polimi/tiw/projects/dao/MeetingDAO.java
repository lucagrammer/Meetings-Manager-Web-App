package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import it.polimi.tiw.projects.beans.Meeting;

public class MeetingDAO {
	private Connection connection;

	public MeetingDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Find the upcoming meetings to which the user has been invited
	 * @param username	The username
	 * @return A list containing all the upcoming meetings to which the user has been invited
	 */
	public List<Meeting> findInvitedMeetingsByUser(String username) throws SQLException {
		
		List<Meeting> meetings = new ArrayList<Meeting>();

		String query = "SELECT id,title,date,time,duration,max_participants,creator from meeting,invitation where id_meeting = id AND guest = ? AND date>=curdate() AND (date!=curdate() OR time>=curtime()) ORDER BY date,time;";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Meeting meeting = new Meeting();
					meeting.setId(result.getInt("id"));
					meeting.setTitle(result.getString("title"));
					meeting.setDate(Date.valueOf(result.getString("date")));
					meeting.setTime(Time.valueOf(result.getString("time")));
					meeting.setDuration(result.getInt("duration"));
					meeting.setMaxParticipants(result.getInt("max_participants"));
					meeting.setCreator(result.getString("creator"));
					meetings.add(meeting);
				}
			}
		}
		
		return meetings;
	}

	/**
	 * Find the upcoming meetings created by the user
	 * @param username	The username
	 * @return	A list containing all the upcoming meetings created by the user
	 */
	public List<Meeting> findConvenedMeetingsByUser(String username) throws SQLException {
		
		List<Meeting> meetings = new ArrayList<Meeting>();

		String query = "SELECT id,title,date,time,duration,max_participants,creator,COUNT(guest) AS participants from meeting left join invitation on id=id_meeting where creator = ? AND date>=curdate() AND (date!=curdate() OR time>=curtime()) GROUP BY id,title,date,duration,max_participants,creator ORDER BY date,time; ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Meeting meeting = new Meeting();
					meeting.setId(result.getInt("id"));
					meeting.setTitle(result.getString("title"));
					meeting.setDate(Date.valueOf(result.getString("date")));
					meeting.setTime(Time.valueOf(result.getString("time")));
					meeting.setDuration(result.getInt("duration"));
					meeting.setMaxParticipants(result.getInt("max_participants"));
					meeting.setParticipants(result.getInt("participants"));
					meeting.setCreator(result.getString("creator"));
					meetings.add(meeting);
				}
			}
		}
		return meetings;
	}

	/**
	 * Adds a meeting 
	 */
	public Integer createMeeting(Meeting meeting) throws SQLException {
		ResultSet rs;
		String query = "INSERT into meeting (title, date, time, duration, max_participants, creator) VALUES(?, ?, ?, ?, ?, ?)";
		System.out.println(meeting.getDate()+" "+meeting.getTime().toString());
		try (PreparedStatement pstatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, meeting.getTitle());
			pstatement.setString(2, meeting.getDate().toString());
			pstatement.setString(3, meeting.getTime().toString());
			pstatement.setInt(4, meeting.getDuration());
			pstatement.setInt(5, meeting.getMaxParticipants());
			pstatement.setString(6, meeting.getCreator());
			System.out.println(pstatement);
			pstatement.executeUpdate();
			rs = pstatement.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1); 
			}else {
				return null;
			}	
		}
	}
	
	/**
	 * Adds an invitation
	 */
	public void addInvitation(int meetingId, String guest) throws SQLException {
		
		String query = "INSERT into invitation (id_meeting, guest) VALUES(?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, meetingId);
			pstatement.setString(2, guest);
			pstatement.executeUpdate();
		}
	}

}
