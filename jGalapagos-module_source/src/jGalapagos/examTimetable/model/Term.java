package jGalapagos.examTimetable.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Class that contains informations about term such as: label, data, soft capacity, hard capacity,
 * day index, index within a day.
 *
 */
public final class Term implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String label;
	private final Date date;
	private final int softCapacity;
	private final int hardCapacity;
	private final int dayIndex;
	private final int withinDayIndex;

	/**
	 * Term object constructor.
	 * @param date	Date
	 * @param softCapacity	Soft student capacity
	 * @param hardCapacity	Hard student capacity.
	 * @param dayIndex	Index of a containing day
	 * @param withinDayIndex	Term index within a day
	 * @param label	Label of the term.
	 */
	public Term(Date date, int softCapacity, int hardCapacity, int dayIndex, int withinDayIndex, String label) {
		this.label = label;
		this.date = date;
		this.softCapacity = softCapacity;
		this.hardCapacity = hardCapacity;
		this.dayIndex = dayIndex;
		this.withinDayIndex = withinDayIndex;
	}
	
	/**
	 * @return	Term label. 
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return	Term date.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return Soft student capacity.
	 */
	public int getSoftCapacity() {
		return softCapacity;
	}

	/**
	 * @return	Hard student capacity.
	 */
	public int getHardCapacity() {
		return hardCapacity;
	}

	/**
	 * @return Term day index.
	 */
	public int getDayIndex() {
		return dayIndex;
	}

	/**
	 * @return	Term index within a day.
	 */
	public int getWithinDayIndex() {
		return withinDayIndex;
	}

}
