package com.cloudkibo.library;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "com.cloudkibo";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "CloudKibo";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to a CloudKibo account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to a CloudKibo account";

    public static final String KEY_STATUS = "status";
    public static final String KEY_MSG = "msg";

    public static final ServerAuthenticate sServerAuthenticate = new ParseComServerAuthenticate();
}
