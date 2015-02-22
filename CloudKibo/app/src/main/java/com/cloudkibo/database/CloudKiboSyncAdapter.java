package com.cloudkibo.database;

import static com.cloudkibo.library.AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.cloudkibo.database.CloudKiboDatabaseContract.Contacts;
import com.cloudkibo.database.CloudKiboDatabaseContract.User;
import com.cloudkibo.library.AccountGeneral;
import com.cloudkibo.library.KiboAuthenticator;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.model.ContactItem;

public class CloudKiboSyncAdapter extends AbstractThreadedSyncAdapter {

	private final AccountManager mAccountManager;

    private final KiboAuthenticator kiboAuth;
	 
    public CloudKiboSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);

        kiboAuth = new KiboAuthenticator(context);
    }
	
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		
		try {

			String authToken = mAccountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);

//            String authToken = kiboAuth.

			UserFunctions userFunction = new UserFunctions();
			
			JSONObject remoteUser = userFunction.getUserData(authToken);
			
			JSONArray remoteContacts = userFunction.getContactsList(authToken);

            if(remoteUser == null){
                mAccountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, authToken);

                Log.e("TOKEN", "TOKEN INVALIDATED");

            }

			/*
			JSONObject localUser = new JSONObject();
			Cursor curUser = provider.query(User.CONTENT_URI, null, null, null, null);
			curUser.moveToFirst();
	        if(curUser.getCount() > 0){
	        	localUser.put(User.KEY_FIRSTNAME, curUser.getString(1));
	        	localUser.put(User.KEY_LASTNAME, curUser.getString(2));
	        	localUser.put(User.KEY_EMAIL, curUser.getString(3));
	        	localUser.put(User.KEY_USERNAME, curUser.getString(4));
	        	localUser.put(User.KEY_UID, curUser.getString(5));
	        	localUser.put(User.KEY_CREATED_AT, curUser.getString(6));
	        }
	        curUser.close();
	        */

	        Log.d("SYNC_ADAPTER", remoteUser.toString());
	        
	        provider.delete(User.CONTENT_URI, null, null);
	        
	        ContentValues cvUser = new ContentValues();
	        
	        cvUser.put(User.KEY_FIRSTNAME, remoteUser.getString(User.KEY_FIRSTNAME));
	        cvUser.put(User.KEY_LASTNAME, remoteUser.getString(User.KEY_LASTNAME));
	        cvUser.put(User.KEY_EMAIL, remoteUser.getString(User.KEY_EMAIL));
	        cvUser.put(User.KEY_USERNAME, remoteUser.getString(User.KEY_USERNAME));
	        cvUser.put(User.KEY_UID, remoteUser.getString(User.KEY_UID));
	        cvUser.put(User.KEY_CREATED_AT, remoteUser.getString(User.KEY_CREATED_AT));
	        
	        provider.insert(User.CONTENT_URI, cvUser);
	        
	        /*
			JSONArray localContacts = new JSONArray();
			Cursor curContacts = provider.query(Contacts.CONTENT_URI, null, null, null, null);
			curContacts.moveToFirst();
	        if(curContacts.getCount() > 0){
	        	
	        	while (curContacts.isAfterLast() != true) {
	        		
	        		JSONObject contact = new JSONObject();
	        		contact.put(Contacts.CONTACT_FIRSTNAME, curContacts.getString(1));
	        		contact.put(Contacts.CONTACT_LASTNAME, curContacts.getString(2));
	        		contact.put(Contacts.CONTACT_PHONE, curContacts.getString(3));
	        		contact.put(Contacts.CONTACT_USERNAME, curContacts.getString(4));
	        		contact.put(Contacts.CONTACT_UID, curContacts.getString(5));
	        		contact.put(Contacts.SHARED_DETAILS, curContacts.getString(6));
	        		contact.put(Contacts.CONTACT_STATUS, curContacts.getString(7));
	        		
	        		localContacts.put(contact);
	        		
	        		curContacts.moveToNext();
	            }
	        }
	        curContacts.close();
	        */
	        Log.d("SYNC_ADAPTER", remoteContacts.toString());
	        
	        provider.delete(Contacts.CONTENT_URI, null, null);
	        
	        ContentValues cvContacts = new ContentValues();
	        
	        for (int i=0; i < remoteContacts.length(); i++) {
	        	JSONObject row = remoteContacts.getJSONObject(i);
	        	
	        	cvContacts.put(Contacts.CONTACT_FIRSTNAME, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_FIRSTNAME));
	        	cvContacts.put(Contacts.CONTACT_LASTNAME, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_LASTNAME));
	        	cvContacts.put(Contacts.CONTACT_PHONE, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_PHONE));
	        	cvContacts.put(Contacts.CONTACT_USERNAME, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_USERNAME));
	        	cvContacts.put(Contacts.CONTACT_UID, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_UID));
	        	cvContacts.put(Contacts.SHARED_DETAILS, 
	        			row.getString(Contacts.SHARED_DETAILS));
	        	cvContacts.put(Contacts.CONTACT_STATUS, 
	        			row.getJSONObject("contactid").getString(Contacts.CONTACT_STATUS));
	        	
	        	provider.insert(Contacts.CONTENT_URI, cvContacts);
	        	
	        	cvContacts = new ContentValues();
	        }
			
			
		} catch(NullPointerException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		

	}

}
