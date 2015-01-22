package com.cloudkibo.model;

/**
 * The Class Conversation is a Java Bean class that represents a single chat conversation message.
 */
public class Conversation
{
	
	/** The msg. */
	private String msg;
	
	/** The is sent. */
	private boolean isSent;
	
	/** The is success. */
	private boolean isSuccess;
	
	/** The date. */
	private String date;

	/**
	 * Instantiates a new conversation.
	 *
	 * @param msg the msg
	 * @param date the date
	 * @param isSent the is sent
	 * @param isSuccess the is success
	 */
	public Conversation(String msg, String date, boolean isSent,
			boolean isSuccess)
	{
		this.msg = msg;
		this.isSent = isSent;
		this.date = date;
		if (isSent)
			this.isSuccess = isSuccess;
		else
			this.isSuccess = false;
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
	 * @param msg the new msg
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Checks if is sent.
	 *
	 * @return true, if is sent
	 */
	public boolean isSent()
	{
		return isSent;
	}

	/**
	 * Sets the sent.
	 *
	 * @param isSent the new sent
	 */
	public void setSent(boolean isSent)
	{
		this.isSent = isSent;
	}

	/**
	 * Checks if is success.
	 *
	 * @return true, if is success
	 */
	public boolean isSuccess()
	{
		return isSuccess;
	}

	/**
	 * Sets the success.
	 *
	 * @param isSuccess the new success
	 */
	public void setSuccess(boolean isSuccess)
	{
		this.isSuccess = isSuccess;
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
	 * @param date the new date
	 */
	public void setDate(String date)
	{
		this.date = date;
	}

}
