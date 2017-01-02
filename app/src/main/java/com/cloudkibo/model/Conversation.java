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

	private String status;

	private String uniqueid;

	private String sender_phone;

	private String sender_name;

	private String type;

	private String file_type;

	private String uri;

	private Boolean onLocal;

	private String contact_image;

	/**
	 * Instantiates a new conversation.
	 *
	 * @param msg the msg
	 * @param date the date
	 * @param isSent the is sent
	 * @param isSuccess the is success
	 */
	public Conversation(String msg, String date, boolean isSent,
			boolean isSuccess, String status, String uniqueid, String type)
	{
		this.msg = msg;
		this.isSent = isSent;
		this.date = date;
		if (isSent)
			this.isSuccess = isSuccess;
		else
			this.isSuccess = false;

		this.status = status;
		this.uniqueid = uniqueid;
		this.type = type;
	}

	public Conversation(String msg, String sender_phone, boolean isSent, String date, String uniqueid, String status, String type){
		this.msg = msg;
		this.sender_phone = sender_phone;
		this.isSent = isSent;
		this.date = date;
		this.uniqueid = uniqueid;
		this.status = status;
		this.type = type;
	}

	public Conversation setContact_image(String contact_image){
		this.contact_image = contact_image;
        return this;
	}

	public String getContact_image(){
		return this.contact_image;
	}


	public String getSender_phone(){
		return  sender_phone;
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

	public String getStatus () { return status; }

	public void setStatus (String status) { this.status = status; }

	public String getUniqueid () { return uniqueid; }

	public void setUniqueid (String uniqueid) { this.uniqueid = uniqueid; }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFile_type() {
		return file_type;
	}

	public void setFile_type(String file_type) {
		this.file_type = file_type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Boolean getOnLocal() {
		return onLocal;
	}

	public void setOnLocal(Boolean onLocal) {
		this.onLocal = onLocal;
	}
}
