package com.chatt.model;

public class ContactItem {


	/** The username. */
	private String username;

	/** The firstname. */
	private String firstname;

	/** The lastname. */
	private String lastname;

	/** The icon. */
	private int icon;

	/** The online. */
	private boolean online;

	/** The phone*/
	private String phone;

	/** The is city*/
	private String city;
	
	/** The is status*/
	private String status;

	/**
	 * Instantiates a new chat item.
	 * 
	 * @param name
	 *            the name
	 * @param title
	 *            the title
	 * @param msg
	 *            the msg
	 * @param date
	 *            the date
	 * @param icon
	 *            the icon
	 * @param online
	 *            the online
	 * @param isGroup
	 *            the is group
	 */
	public ContactItem(String username, String firstname, String lastname, String phone,
			int icon, boolean online, String city, String status)
	{
		this.username = username;
		this.phone = phone;
		this.icon = icon;
		this.city = city;
		this.firstname = firstname;
		this.online = online;
		this.lastname = lastname;
		this.status = status;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getUserName()
	{
		return username;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setUserName(String username)
	{
		this.username = username;
	}

	/**
	 * Gets the first name.
	 * 
	 * @return the first name
	 */
	public String firstName()
	{
		return firstname;
	}

	/**
	 * Sets the first name.
	 * 
	 * @param firstname
	 *            the new first name
	 */
	public void setFirstName(String firstname)
	{
		this.firstname = firstname;
	}

	/**
	 * Gets the last name.
	 * 
	 * @return the last name
	 */
	public String lastName()
	{
		return lastname;
	}

	/**
	 * Sets the last name.
	 * 
	 * @param lastname
	 *            the new last name
	 */
	public void setLastName(String lastname)
	{
		this.lastname = lastname;
	}

	/**
	 * Gets the icon.
	 * 
	 * @return the icon
	 */
	public int getIcon()
	{
		return icon;
	}

	/**
	 * Sets the icon.
	 * 
	 * @param icon
	 *            the new icon
	 */
	public void setIcon(int icon)
	{
		this.icon = icon;
	}

	/**
	 * Checks if is online.
	 * 
	 * @return true, if is online
	 */
	public boolean isOnline()
	{
		return online;
	}

	/**
	 * Sets the online.
	 * 
	 * @param online
	 *            the new online
	 */
	public void setOnline(boolean online)
	{
		this.online = online;
	}

	/**
	 * Gets the phone.
	 * 
	 * @return the phone
	 */
	public String getPhone()
	{
		return phone;
	}

	/**
	 * Sets the phone.
	 * 
	 * @param phone
	 *            the new phone
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	/**
	 * Checks if is city.
	 * 
	 * @return true, if is city
	 */
	public String city()
	{
		return city;
	}

	/**
	 * Sets the city.
	 * 
	 * @param city
	 *            the new city
	 */
	public void setCity(String city)
	{
		this.city = city;
	}
	
	/**
	 * Checks if is status
	 * 
	 * @return status
	 */
	public String status()
	{
		return status;
	}

	/**
	 * Sets the status
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

}
