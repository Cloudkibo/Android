package com.cloudkibo.model;

/**
 * The Class ChatItem is a Java Bean class that can be used for representing a
 * Chat conversation item.
 */
public class ChatItem
{

	/** The name. */
	private String name;

	/** The title. */
	private String title;

	/** The msg. */
	private String msg;

	/** The icon. */
	private int icon;

	/** The online. */
	private boolean online;

	/** The date. */
	private String date;

	/** The is group. */
	private boolean isGroup;
	private boolean isArchived;

	private String contact_id;

	private int pendingMsgs;
	private String profileimg;

	private String lastSender;

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
	public ChatItem(String name, String title, String msg, String date,
			int icon, boolean online, boolean isGroup, int pending, String lastSender)
	{
		this.name = name;
		this.date = date;
		this.icon = icon;
		this.isGroup = isGroup;
		this.msg = msg;
		this.online = online;
		this.title = title;
		this.pendingMsgs = pending;
		this.isArchived=false;
		this.lastSender = lastSender;
	}

	public ChatItem setProfileImage(String profileimg){
		this.profileimg = profileimg;
		return this;
	}
	public String getProfileimg(){ return this.profileimg; }
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * Sets the msg.
	 * 
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
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
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public String getDate()
	{
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(String date)
	{
		this.date = date;
	}

	/**
	 * set the archive status of archive chat list
	 * @param status
     */

	public void setArchiveStatus(boolean status){
		if(status)
			isArchived=true;
		else
			isArchived=false;
	}

	/**
	 * Checks if is group.
	 * 
	 * @return true, if is group
	 */
	public boolean isGroup()
	{
		return this.isGroup;
	}

	/**
	 * check if is archived
	 * @return true if is arhived
     */

	public boolean isArchived()
	{
		return this.isArchived;
	}

	/**
	 * Sets the group.
	 * 
	 * @param isGroup
	 *            the new group
	 */
	public void setGroup(boolean isGroup)
	{
		this.isGroup = isGroup;
	}

	public String getContactId () { return contact_id; }

	public void setContactId (String contact_id) { this.contact_id = contact_id; }

	public int getPendingMsgs() {
		return pendingMsgs;
	}

	public void setPendingMsgs(int pendingMsgs) {
		this.pendingMsgs = pendingMsgs;
	}

	public String getLastSender() {
		return lastSender;
	}

	public void setLastSender(String lastSender) {
		this.lastSender = lastSender;
	}
}
