package it.polimi.tiw.projects.beans;

import java.sql.Date;
import java.sql.Time;

public class Meeting {
	
	private int id;
	private String title;
	private Date date;
	private Time time;
	private int duration;
	private int maxParticipants;
	private int participants;
	private String creator;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title){
		this.title=title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public int getParticipants() {
		return participants;
	}

	public void setParticipants(int participants) {
		this.participants = participants;
	}

	public void setTime(Time time) {
		this.time=time;
	}
	
	public Time getTime() {
		return time;
	}

}
