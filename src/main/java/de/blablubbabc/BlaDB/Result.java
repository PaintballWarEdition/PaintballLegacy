/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Result {
	private ResultSet resultSet;
	private Statement statement;
	
	public Result(Statement statement, ResultSet resultSet) {
		this.statement = statement;
		this.resultSet = resultSet;
	}
	
	public ResultSet getResultSet() {
		return this.resultSet;
	}

	public void close() {
		try {
			this.statement.close();
			this.resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
