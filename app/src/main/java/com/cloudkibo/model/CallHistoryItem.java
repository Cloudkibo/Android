package com.cloudkibo.model;

/**
 * Created by sojharo on 6/20/16.
 */
public class CallHistoryItem {


    /** The date. */
    private String date;

    /** The type. */
    private String type;

    /** The contact_name. */
    private String contact_name;

    private String contact_phone;

    private String contact_id;

    /**
     * Instantiates a new feed class.
     *
     * @param data1 the date
     * @param type the type
     */
    public CallHistoryItem(String date1, String type, String contact_name1, String contact_phone1, String contact_id1)
    {
        this.date = date1;
        this.type = type;
        this.contact_name = contact_name1;
        this.contact_phone = contact_phone1;
        this.contact_id = contact_id1;
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
     * Gets the type.
     *
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Gets the contact_name.
     *
     * @return the contact_name
     */
    public String getContact_name()
    {
        return contact_name;
    }

    /**
     * Sets the contact_name.
     *
     * @param contact_name
     *            the new contact_name
     */
    public void setContact_name(String contact_name)
    {
        this.contact_name = contact_name;
    }

    /**
     * Gets the contact_phone.
     *
     * @return the contact_phone
     */
    public String getContact_phone()
    {
        return contact_phone;
    }

    /**
     * Sets the contact_phone.
     *
     * @param contact_phone
     *            the new contact_phone
     */
    public void setContact_phone(String contact_phone)
    {
        this.contact_phone = contact_phone;
    }

    /**
     * Gets the contact_id.
     *
     * @return the contact_id
     */
    public String getContact_id()
    {
        return contact_id;
    }

    /**
     * Sets the contact_id.
     *
     * @param contact_id
     *            the new contact_id
     */
    public void setContact_id(String contact_id)
    {
        this.contact_id = contact_id;
    }

}
