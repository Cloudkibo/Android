package com.cloudkibo.model;

/**
 * Created by sojharo on 10/2/2015.
 */
public class AddressBookContactItem {


    /** The username. */
    private String name;
    private String email;
    private String phone;


    /**
     * Instantiates a new chat item.
     *
     * @param name
     *            the name
     */
    public AddressBookContactItem(String name, String phone, String email)
    {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String name()
    {
        return name;
    }

    /**
     * Sets the first name.
     *
     * @param firstname
     *            the new first name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getPhone()
    {
        return phone;
    }

    /**
     * Sets the last name.
     *
     * @param lastname
     *            the new last name
     */
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Sets the last name.
     *
     * @param lastname
     *            the new last name
     */
    public void setEmail(String email)
    {
        this.email = email;
    }


}
