package de.blablubbabc.paintball.utils.uuids.namehistory;

import java.util.Date;

public class NameHistoryEntry {

	private final String name;
	private final Date changedToAt; // can be null

	public NameHistoryEntry(String name, Date changedToAt) {
		this.name = name;
		this.changedToAt = changedToAt;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return can be null
	 */
	public Date getChangedToAt() {
		return changedToAt;
	}
}
