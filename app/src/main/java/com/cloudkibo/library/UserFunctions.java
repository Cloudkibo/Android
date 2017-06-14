package com.cloudkibo.library;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.database.DatabaseHandler;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class UserFunctions {
	
	
    /////////////////////////////////////////////////////////////////////
    // VARIABLES                                                       //
    /////////////////////////////////////////////////////////////////////

    private ConnectionManager connection;

    private String baseURL = ""; //"https://api.cloudkibo.com";
    private final String TAG = "UserFunctions";

    //URL of the NODEJS API
    private String loginURL;
    private String registerURL;
    private String userDataURL;
    private String saveChatURL;
    private String getChatURL;
    private String markChatReadURL;
    private String saveContactURL;
    private String forpassURL;
    private String chgpassURL;
    private String getContactsURL;
    private String getContactsWhoBlockedMeURL;
    private String getContactsBlockedByMeURL;
    private String getPendingContactsURL;
    private String approveContactURL;
    private String rejectContactURL;
    private String removeChatURL;
    private String removeContactURL;
    private String phoneContactsURL;
    private String emailContactsURL;
    private String inviteContactsURL;
    private String saveDisplayNameURL;
    private String getAllChatURL;
    private String sendChatURL;
    private String sendChatStatusURL;
    private String getPartialChatURL;
    private String getSingleChatURL;
    private String getDaystatusInfoURL;
    private String checkSentChatStatus;
    private String getSingleGroupChatURL;
    private String sendLogURL;
    private String createGroupURL;
    private String getGroupInfo;
    private String getMyGroups;
    private String sendGroupChat;
    private String sendChangedGroupName;
    private String addGroupMembers;
    private String leaveGroup;
    private String removeMember;
    private String groupMembers;
    private String updateGroupChatStatus;
    private String uploadIcon;
    private String downloadIcon;
    private String checkGroupChatStatus;
    private String updateMemberRole;
    private String lastSeenStatus;
    private String confirmFileDownloadURL;
    private String confirmFileDownloadBroadCastURL;
    private String confirmFileDownloadGroupURL;
    private String blockContactURL;
    private String sendBroadCastURL;
    private String unBlockContactURL;
    private String syncURL;
    private String syncDownWardURL;
    
    
    
    
    
    
	/////////////////////////////////////////////////////////////////////
	// Constructor                                                     //
	/////////////////////////////////////////////////////////////////////

    public UserFunctions(Context ctx){

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_SERVER_URL_KEY = "server_url";
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
        baseURL = prefs.getString(PREF_SERVER_URL_KEY, "-1");

        loginURL =                baseURL + "/auth/local";
        registerURL =             baseURL + "/api/users/";
        userDataURL =             baseURL + "/api/users/me";
        saveChatURL =             baseURL + "/api/userchat/save";
        getChatURL =              baseURL + "/api/userchat";
        markChatReadURL =         baseURL + "/api/userchat/markasread";
        saveContactURL =          baseURL + "/api/contactslist/addbyusername";
        forpassURL =              baseURL + "/api/users/resetpasswordrequest";
        chgpassURL =              baseURL + "/learn2crack_login_api/";
        getContactsURL =          baseURL + "/api/contactslist/";
        getContactsWhoBlockedMeURL=baseURL + "/api/contactslist/blockedby";
        getContactsBlockedByMeURL=baseURL + "/api/contactslist/blockedbyme";
        getPendingContactsURL =   baseURL + "/api/contactslist/pendingcontacts/";
        approveContactURL =       baseURL + "/api/contactslist/approvefriendrequest/";
        rejectContactURL =        baseURL + "/api/contactslist/rejectfriendrequest/";
        removeChatURL =           baseURL + "/api/userchat/removechathistory/";
        removeContactURL =        baseURL + "/api/contactslist/removefriend/";
        phoneContactsURL =        baseURL + "/api/users/searchaccountsbyphone/";
        emailContactsURL =        baseURL + "/api/users/searchaccountsbyemail/";
        inviteContactsURL =       baseURL + "/api/users/invitebymultipleemail/";
        saveDisplayNameURL =      baseURL + "/api/users/newuser";
        getAllChatURL =           baseURL + "/api/userchat/alluserchat";
        sendChatURL =             baseURL + "/api/userchat/save2";
        sendChatStatusURL =       baseURL + "/api/userchat/updateStatus";
        getPartialChatURL =       baseURL + "/api/userchat/partialchatsync";
        getSingleChatURL =        baseURL + "/api/userchat/getsinglechat";
        getDaystatusInfoURL =     baseURL + "/api/daystatus/getInfo";
        checkSentChatStatus =     baseURL + "/api/userchat/checkStatus";
        getSingleGroupChatURL =   baseURL + "/api/groupchat/fetchSingleChat";
        sendLogURL =              baseURL + "/api/users/log";
        createGroupURL =          baseURL + "/api/groupmessaging/";
        getGroupInfo =            baseURL + "/api/groupmessaging/specificGroup";
        getMyGroups =             baseURL + "/api/groupmessaginguser/mygroups";
        sendGroupChat =           baseURL + "/api/groupchat/";
        sendChangedGroupName =    baseURL + "/api/groupmessaging/updateGroupName";
        addGroupMembers =         baseURL + "/api/groupmessaginguser/";
        leaveGroup =              baseURL + "/api/groupmessaginguser/leaveGroup";
        removeMember =            baseURL + "/api/groupmessaginguser/removeFromGroup";
        groupMembers =            baseURL + "/api/groupmessaginguser/myspecificgroupsmembers";
        updateGroupChatStatus =   baseURL + "/api/groupchatstatus/updateStatus";
        uploadIcon =              baseURL + "/api/groupmessaging/uploadIcon";
        downloadIcon =            baseURL + "/api/groupmessaging/downloadIcon";
        checkGroupChatStatus =    baseURL + "/api/groupchatstatus/checkStatus";
        updateMemberRole =        baseURL + "/api/groupmessaginguser/updateRole";
        lastSeenStatus =          baseURL + "/api/users/getUserInfo";
        confirmFileDownloadURL =  baseURL + "/api/filetransfers/confirmdownload";
        confirmFileDownloadBroadCastURL =  baseURL + "/api/broadcastfile/confirmdownload";
        confirmFileDownloadGroupURL =  baseURL + "/api/filetransfersgroup/confirmdownload";
        blockContactURL =         baseURL + "/api/contactslist/blockContact";
        unBlockContactURL =       baseURL + "/api/contactslist/unblockContact";
        syncURL =                 baseURL + "/api/sync/upwardSync";
        syncDownWardURL =         baseURL + "/api/sync/downwardSync";
        sendBroadCastURL =        baseURL + "/api/userchat/sendbroadcast";

        connection = new ConnectionManager();
    }
    
    
    
    
    
    

    /**
     * Function to Login
     * @throws Exception 
     **/

    public String loginUser(String username, String password) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        String authtoken = connection.getTokenFromServer(loginURL, params);
        return authtoken;
    }

    
    
    
    
    
    
    

    /**
     * Function to change password
     * @throws Exception 
     **/

    public String chgPass(String newpas, String email) throws Exception{
        List<NameValuePair> params = new ArrayList<NameValuePair>();


        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        String result = connection.getTokenFromServer(chgpassURL, params);
        return result;
        // Add this feature in future
    }





    /**
     * Function to request server for password reset link. It takes the user email address.
     * @throws Exception
     **/

    public JSONObject forgotPass(String email) throws Exception{
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("email", email));
        JSONObject response = connection.sendObjectToServerNoAuth(forpassURL, params);
        return response;
    }

    public JSONObject sendLog(String data) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data", data));
        JSONObject response = connection.sendObjectToServerNoAuth(sendLogURL, params);
        return response;
    }


    
    
    
    


     /**
      * Function to Register
      * @throws Exception 
      **/
    public String registerUser(String fname, String lname, String email, String uname, String password, String phone) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("firstname", fname));
        params.add(new BasicNameValuePair("lastname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("phone", phone));
        String authtoken = connection.getTokenFromServer(registerURL, params);
        return authtoken;
    }
    
    
    
    
    
    
    
    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        }
        return false;
    }
    

    
    
    
    

    /**
     * Function to logout user
     * Resets the temporary data stored in SQLite Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }





    public JSONObject setDisplayName(List<NameValuePair> params, String authtoken) {
        JSONObject response = connection.sendObjectToServer(saveDisplayNameURL, authtoken, params);
        return response;
    }

	public JSONObject getUserData(String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONObject userdata = connection.getDataFromServer(userDataURL, authtoken);
        return userdata;
	}
	
	
	public JSONArray getContactsList(String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONArray contactslist = connection.getArrayFromServer(getContactsURL, authtoken);
        return contactslist;
	}

    public JSONArray getContactsListWhoBlockedMe(String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONArray contactslist = connection.getArrayFromServer(getContactsWhoBlockedMeURL, authtoken);
        return contactslist;
    }

    public JSONArray getContactsListBlockedByMe(String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONArray contactslist = connection.getArrayFromServer(getContactsBlockedByMeURL, authtoken);
        return contactslist;
    }

    public JSONObject sendChatMessageToServer(JSONObject data, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try{
        params.add(new BasicNameValuePair("from", data.getString("from")));
        params.add(new BasicNameValuePair("to", data.getString("to")));
        params.add(new BasicNameValuePair("fromFullName", data.getString("fromFullName")));
        params.add(new BasicNameValuePair("msg", data.getString("msg")));
        params.add(new BasicNameValuePair("date", data.getString("date")));
        params.add(new BasicNameValuePair("uniqueid", data.getString("uniqueid")));
        params.add(new BasicNameValuePair("type", data.getString("type")));
        params.add(new BasicNameValuePair("file_type", data.getString("file_type")));
        } catch (JSONException e){
            e.printStackTrace();
        }
        JSONObject response = connection.sendObjectToServer(sendChatURL, authtoken, params);
        return response;
    }

    public JSONObject sendBroadCastMessageToServer(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServer(sendBroadCastURL, authtoken, data);
    }

    public JSONObject blockContact(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServer(blockContactURL, authtoken, data);
    }

    public JSONObject unBlockContact(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServer(unBlockContactURL, authtoken, data);
    }

    public JSONObject sendCreateGroupToServer(JSONObject data, String authtoken){
         return connection.sendJSONObjectToServer(createGroupURL, authtoken, data);
    }

    public JSONObject addGroupMembers(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServer(addGroupMembers, authtoken, data);
    }

    public JSONObject upwardSync(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServer(syncURL, authtoken, data);
    }

    public JSONObject downwardSync(JSONObject body, String authtoken) {
        JSONObject data = connection.sendJSONObjectToServer(syncDownWardURL, authtoken, body);
        return data;
    }

    public JSONArray checkStatusOfGroupMessages(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServerReturnArray(checkGroupChatStatus, authtoken, data);
    }

    public JSONArray checkStatusOfSentChatMessages(JSONObject data, String authtoken){
        return connection.sendJSONObjectToServerReturnArray(checkSentChatStatus, authtoken, data);
    }

    public JSONObject sendChatMessageStatusToServer(JSONObject data, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try{
            params.add(new BasicNameValuePair("sender", data.getString("sender")));
            params.add(new BasicNameValuePair("status", data.getString("status")));
            params.add(new BasicNameValuePair("uniqueid", data.getString("uniqueid")));
        } catch (JSONException e){
            e.printStackTrace();
        }
        JSONObject response = connection.sendObjectToServer(sendChatStatusURL, authtoken, params);
        return response;
    }

    public JSONObject sendGroupChat(String group_unique_id,String from,String type, String msg, String from_fullname, String unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("group_unique_id", group_unique_id));
        params.add(new BasicNameValuePair("from", from));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("msg", msg));
        params.add(new BasicNameValuePair("from_fullname", from_fullname));
        params.add(new BasicNameValuePair("unique_id", unique_id));
        JSONObject response = connection.sendObjectToServer(sendGroupChat, authtoken, params);
        return response;
    }

    public JSONObject sendChangedGroupName(String group_unique_id, String new_name, String authtoken){
        List <NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("mygroupname", new_name));
        params.add(new BasicNameValuePair("unique_id", group_unique_id));

        JSONObject response = connection.sendObjectToServer(sendChangedGroupName, authtoken, params);
        return response;
    }

    public JSONObject confirmFileDownload(String chat_unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("uniqueid", chat_unique_id));
        JSONObject response = connection.sendObjectToServer(confirmFileDownloadURL, authtoken, params);
        return response;
    }

    public JSONObject confirmFileDownloadBroadCast(String chat_unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("uniqueid", chat_unique_id));
        JSONObject response = connection.sendObjectToServer(confirmFileDownloadBroadCastURL, authtoken, params);
        return response;
    }

    public JSONObject confirmFileDownloadGroup(String chat_unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("uniqueid", chat_unique_id));
        JSONObject response = connection.sendObjectToServer(confirmFileDownloadGroupURL, authtoken, params);
        return response;
    }


    public JSONObject updateGroupChatStatusToDelivered(String chat_unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("chat_unique_id", chat_unique_id));
        params.add(new BasicNameValuePair("status", "delivered"));
        JSONObject response = connection.sendObjectToServer(updateGroupChatStatus, authtoken, params);
        return response;
    }

    public JSONObject updateGroupChatStatusToSeen(String chat_unique_id,  String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("chat_unique_id", chat_unique_id));
        params.add(new BasicNameValuePair("status", "seen"));
        JSONObject response = connection.sendObjectToServer(updateGroupChatStatus, authtoken, params);
        return response;
    }
    public JSONObject getAllChatList(String user1, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user1", user1));
        JSONObject response = connection.sendObjectToServer(getAllChatURL, authtoken, params);
        return response;
    }

    public JSONArray getGroupMembers(String group_id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("unique_id", group_id));
        JSONArray response = connection.sendObjectReturnArray(groupMembers, authtoken, params);
        return response;
    }

    public JSONObject leaveGroup(String group_id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("group_unique_id", group_id));
        Log.d(TAG, "leaveGroup: THis is the group id send to server " + group_id);
        JSONObject response = connection.sendObjectToServer(leaveGroup, authtoken, params);
        return response;
    }

    public JSONObject removeMember(String group_id, String number, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("group_unique_id", group_id));
        params.add(new BasicNameValuePair("phone", number));
        JSONObject response = connection.sendObjectToServer(removeMember, authtoken, params);
        return response;
    }

    public JSONObject uploadIcon(String group_id, String filepath, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("unique_id", group_id));
        JSONObject response = connection.sendObjectToServer(uploadIcon, authtoken, params);
        return response;
    }

    public JSONObject getPartialChatList(String user1, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user1", user1));
        JSONObject response = connection.sendObjectToServer(getPartialChatURL, authtoken, params);
        return response;
    }

    public JSONObject getSingleChat(String id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uniqueid", id));
        JSONObject response = connection.sendObjectToServer(getSingleChatURL, authtoken, params);
        return response;
    }

    public JSONObject getDaystatusInfo(String id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uniqueid", id));
        JSONObject response = connection.sendObjectToServer(getDaystatusInfoURL, authtoken, params);
        return response;
    }

    public JSONObject getSingleGroupChat(String id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("unique_id", id));
        JSONObject response = connection.sendObjectToServer(getSingleGroupChatURL, authtoken, params);
        return response;
    }

    public JSONObject updateMemberRole(String group_id, String member_phone, String makeAdmin, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("group_unique_id", group_id));
        params.add(new BasicNameValuePair("member_phone", member_phone));
        params.add(new BasicNameValuePair("makeAdmin", makeAdmin));
        JSONObject response = connection.sendObjectToServer(updateMemberRole, authtoken, params);
        return response;
    }
	
	public JSONArray getPendingContactsList(String authtoken) {
		JSONArray contactslist = connection.getArrayFromServer(getPendingContactsURL, authtoken);
        return contactslist;
	}
	
	public JSONObject saveChat(List<NameValuePair> params, String authtoken) {
		JSONObject userchatresponse = connection.sendObjectToServer(saveChatURL, authtoken, params);
        return userchatresponse;
	}

    public JSONObject sendAddressBookPhoneContactsToServer(List<NameValuePair> params, String authtoken) {
        JSONObject contactresponse = connection.sendObjectToServer(phoneContactsURL, authtoken, params);
        return contactresponse;
    }

    public JSONObject sendEmailsOfInvitees(List<NameValuePair> params, String authtoken) {
        JSONObject contactresponse = connection.sendObjectToServer(inviteContactsURL, authtoken, params);
        return contactresponse;
    }
	
	public JSONObject markChatAsRead(String user1, String user2, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user1", user1));
		params.add(new BasicNameValuePair("user2", user2));
		JSONObject userchatresponse = connection.sendObjectToServer(markChatReadURL, authtoken, params);
        return userchatresponse;
	}

    public JSONObject removeChat(String user1, String user2, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", user2));
        JSONObject userchatresponse = connection.sendObjectToServer(removeChatURL, authtoken, params);
        return userchatresponse;
    }

    public JSONObject removeContact(String user1, String user2, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", user2));
        JSONObject userchatresponse = connection.sendObjectToServer(removeContactURL, authtoken, params);
        return userchatresponse;
    }
	
	public JSONObject acceptFriendRequest(List<NameValuePair> param, String authtoken) {
		List<NameValuePair> params = param;
		JSONObject userchatresponse = connection.sendObjectToServer(approveContactURL, authtoken, params);
        return userchatresponse;
	}
	
	public JSONObject rejectFriendRequest(List<NameValuePair> param, String authtoken) {
		List<NameValuePair> params = param;
		JSONObject userchatresponse = connection.sendObjectToServer(rejectContactURL, authtoken, params);
        return userchatresponse;
	}
	
	public JSONObject getUserChat(String user1, String user2, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user1", user1));
		params.add(new BasicNameValuePair("user2", user2));
		JSONObject response = connection.sendObjectToServer(getChatURL, authtoken, params);
		return response;
	}

    public JSONObject getUserStatus(String phone, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phone));
        JSONObject response = connection.sendObjectToServer(lastSeenStatus, authtoken, params);
        return response;
    }

    public JSONObject saveContact(String username, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("searchusername", username));
		JSONObject response = connection.sendObjectToServer(saveContactURL, authtoken, params);
        return response;
	}

    public JSONObject getGroupInfo(String group_id, String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("unique_id", group_id));
        JSONObject response = connection.sendObjectToServer(getGroupInfo, authtoken, params);
        return response;
    }

    public JSONObject getAllGroupInfo(String authtoken) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONObject groupdata = connection.getDataFromServer(getMyGroups, authtoken);
        return groupdata;
    }



    public static JSONArray sortJSONArray(JSONArray jsonArr, String key) {

        JSONArray sortedJsonArray = new JSONArray();
        final String myKey = key;

        try{
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private final String KEY_NAME = myKey;

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    } catch (JSONException e) {
                        //do something
                    }

                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });

            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
        } catch (JSONException ex ){
            ex.printStackTrace();
        }

        return sortedJsonArray;
    }

    public static JSONArray sortJSONArrayIgnoreCase(JSONArray jsonArr, String key) {

        JSONArray sortedJsonArray = new JSONArray();
        final String myKey = key;

        try{
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private final String KEY_NAME = myKey;

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    } catch (JSONException e) {
                        //do something
                    }

                    return valA.compareToIgnoreCase(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });

            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
        } catch (JSONException ex ){
            ex.printStackTrace();
        }

        return sortedJsonArray;
    }

    public String getBaseURL () {
        return baseURL;
    }

}

