package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.projects.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Checks the credential of the user
	 * @param username	The username to be checked
	 * @param password	The password
	 * @return	The user bean if credentials are correct, otherwise null value
	 */
	public User checkCredentials(String username, String password) throws SQLException {
		
		String query = "SELECT username, name, surname FROM user  WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1, username);
			pstatement.setString(2,password);
			try (ResultSet result = pstatement.executeQuery();) {
				
				//no rows?
				if (!result.isBeforeFirst()) {
					return null;
				}
				else {
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}
	
	/**
	 * Find all the users except the specified one
	 * @param username	The username of the excluded user
	 * @return	A list containing all the usersnames
	 */
	public ArrayList<String> findAllUsersExcept(String username) throws SQLException{
		
		ArrayList<String> users = new ArrayList<String>();
		
		String query = "SELECT username FROM user where username!= ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1,username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					users.add(result.getString("username"));
				}
			}
		}
		return users;
	}
}
