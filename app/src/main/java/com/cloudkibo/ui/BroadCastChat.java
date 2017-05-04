package com.cloudkibo.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

import static android.content.ContentValues.TAG;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDocuments;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDownloads;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForImages;
import static com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData;

public class BroadCastChat extends CustomFragment implements IFragmentName {

    /** The Conversation list. */
    private ArrayList<Conversation> convList;
    public ArrayList<Conversation> backupList = new ArrayList<Conversation>();

    /** The chat adapter. */
    private ChatAdapter adp;

    /** The Editext to compose the message. */
    private EditText txt;

    private String authtoken;

    private HashMap<String, String> user;

    private String tempCameraCaptureHolderString;

    View view;
    EditText editsearch;
    LinearLayout search_view;
    public static int totalCount = 0;

    LinearLayout mRevealView;
    Boolean attachmentViewHidden = true;

    String list_name;
    String bList_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.group_chat, null);
        view = v;
        setHasOptionsMenu(true);

        list_name = this.getArguments().getString("list_name");

        bList_id = this.getArguments().getString("bList_id");

        authtoken = this.getArguments().getString("authtoken");

        mRevealView = (LinearLayout) v.findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.GONE);

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        user = db.getUserDetails();

        loadConversationList();

        ListView list = (ListView) v.findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Conversation cItem = convList.get(i);
                if(cItem.getType().equals("contact")) {

                    String[] parts = cItem.getMsg().split(":");
                    String name = parts[0];
                    String phone = parts[1];

                    ViewContact vCntctFrag = new ViewContact();
                    Bundle bundle = new Bundle();

                    bundle.putString("name", name);
                    bundle.putString("phone", phone);
                    bundle.putString("authtoken", authtoken);

                    vCntctFrag.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, vCntctFrag, "viewContactFragmentTag")
                            .addToBackStack("View Contact")
                            .commit();
                }
            }
        });

        registerForContextMenu(list);


        adp.notifyDataSetChanged();

        txt = (EditText) v.findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(v.findViewById(R.id.btnSend));

        editsearch = (EditText) v.findViewById(R.id.contact_search);

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
                adp.filter(text);

            }
        });

        search_view = (LinearLayout) v.findViewById(R.id.search_view);
        search_view.setVisibility(View.GONE);
        ImageView close_search = (ImageView) v.findViewById(R.id.close_search);
        close_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view = (LinearLayout) v.findViewById(R.id.search_view);
                search_view.setVisibility(View.GONE);
                editsearch.setText("");
                adp.filter("");
            }
        });


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        inflater.inflate(R.menu.chat, menu);  // Use filter.xml from step 1
//		getActivity().getActionBar().setSubtitle("Last seen on: ");
        //Utility.getLastSeenStatus(getActivity().getApplicationContext(), contactPhone, authtoken, getActivity().getActionBar());
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_imageview, null);
        ImageView search_button = (ImageView) v.findViewById(R.id.imageView4);
        ImageView attach_button = (ImageView) v.findViewById(R.id.imageView5);
        TextView lstSeen = (TextView) v.findViewById(R.id.lastSeen);
        TextView chatTitle = (TextView) v.findViewById(R.id.chatTitle);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setVisibility(View.GONE);
        attach_button.setVisibility(View.VISIBLE);

        try{
            chatTitle.setText(list_name);
        } catch (Exception e){
            e.printStackTrace();
        }

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view.setVisibility(View.VISIBLE);
            }
        });

        attach_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int cx = (mRevealView.getLeft() + mRevealView.getRight());
                int cy = mRevealView.getTop();

                // for doing animation from center
                //int cy = (mRevealView.getTop() + mRevealView.getBottom())/2;

                int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                ImageButton sendImage = (ImageButton) mRevealView.findViewById(R.id.sendImage);
                ImageButton sendDoc = (ImageButton) mRevealView.findViewById(R.id.sendDoc);
                ImageButton sendAudio = (ImageButton) mRevealView.findViewById(R.id.sendAudio);
                ImageButton sendVideo = (ImageButton) mRevealView.findViewById(R.id.sendVideo);
                ImageButton sendContact = (ImageButton) mRevealView.findViewById(R.id.sendContact);
                ImageButton sendLocation = (ImageButton) mRevealView.findViewById(R.id.sendLocation);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                    SupportAnimator animator =
                            ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(800);

                    final SupportAnimator animator_reverse = animator.reverse();

                    if (attachmentViewHidden) {
                        mRevealView.setVisibility(View.VISIBLE);
                        animator.start();
                        attachmentViewHidden = false;
                    } else {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {

                            }

                            @Override
                            public void onAnimationEnd() {
                                mRevealView.setVisibility(View.GONE);
                                attachmentViewHidden = true;

                            }

                            @Override
                            public void onAnimationCancel() {

                            }

                            @Override
                            public void onAnimationRepeat() {

                            }
                        });
                        animator_reverse.start();
                    }
                    sendImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            sendImageSelected();
                        }
                    });
                    sendDoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            MainActivity act2 = (MainActivity)getActivity();
                            act2.uploadChatAttachment("document", "not_group"); // TODO MUST DO ATTACHMENT WORK HERE
                        }
                    });
                    sendAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            MainActivity act2 = (MainActivity)getActivity();
                            act2.uploadChatAttachment("audio", "not_group");
                        }
                    });
                    sendVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            MainActivity act2 = (MainActivity)getActivity();
                            act2.uploadChatAttachment("video", "not_group");
                        }
                    });
                    sendContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            getActivity().startActivityForResult(contactPickerIntent, 129);
                        }
                    });
                    sendLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                            try {
                                getActivity().startActivityForResult(builder.build(MainActivity.mainActivity), 141);
                            } catch (GooglePlayServicesRepairableException e) {
                                e.printStackTrace();
                            } catch (GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    if (attachmentViewHidden) {
                        final Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                        mRevealView.setVisibility(View.VISIBLE);
                        anim.start();
                        attachmentViewHidden = false;

                        final int cxTemp = cx, cyTemp = cy, radiusTemp = radius;
                        sendImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                sendImageSelected();
                            }
                        });
                        sendDoc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                MainActivity act2 = (MainActivity)getActivity();
                                act2.uploadChatAttachment("document", "not_group");
                            }
                        });
                        sendAudio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                MainActivity act2 = (MainActivity)getActivity();
                                act2.uploadChatAttachment("audio", "not_group");
                            }
                        });
                        sendVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                MainActivity act2 = (MainActivity)getActivity();
                                act2.uploadChatAttachment("video", "not_group");
                            }
                        });
                        sendContact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                                getActivity().startActivityForResult(contactPickerIntent, 129);
                            }
                        });
                        sendLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cxTemp, cyTemp, radiusTemp, 0);
                                anim.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mRevealView.setVisibility(View.GONE);
                                        attachmentViewHidden = true;
                                    }
                                });
                                anim.start();
                                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                                try {
                                    getActivity().startActivityForResult(builder.build(MainActivity.mainActivity), 141);
                                } catch (GooglePlayServicesRepairableException e) {
                                    e.printStackTrace();
                                } catch (GooglePlayServicesNotAvailableException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, radius, 0);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mRevealView.setVisibility(View.GONE);
                                attachmentViewHidden = true;
                            }
                        });
                        anim.start();
                    }
                }
            }
        });
        actionBar.setCustomView(v);
        super.onCreateOptionsMenu(menu, inflater);

    }

    // TODO MUST DO ATTACHMENT WORK HERE
    private void sendImageSelected() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity);

        builder.setTitle(R.string.menu_send_image);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 103);
                    } else {
                        uploadImageFromCamera();
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    MainActivity act3 = (MainActivity)getActivity();
                    act3.uploadChatAttachment("image", "not_group");
                } else if (options[item].equals(R.string.cancel)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO THIS SHOULD HAS ITS OWN MENU ITEMS
//        if(id == R.id.callMenu){
//            MainActivity act1 = (MainActivity)getActivity();
//
//            act1.callThisPerson(contactPhone,
//                    contactName);
//            return true;
//        }
//        if(id == R.id.blockThisContact){
//            JSONObject body = new JSONObject();
//            try {
//                body.put("phone", contactPhone);
//                Utility.blockContact(getActivity().getApplicationContext(), body, authtoken);
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Utility.sendLogToServer(getActivity().getApplicationContext(), "Block Contact failed on android in GroupChat");
//            }
//        }

        return super.onOptionsItemSelected(item);
    }

    // TODO MUST DO ATTACHMENT WORK HERE
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok

        if (resultCode == Activity.RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case 129:
                    Cursor cursor = null;
                    try {
                        String phoneNo = null ;
                        String name = null;
                        String photo_uri = null;
                        Uri uri = data.getData();
                        cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int  photoIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);
                        photo_uri = cursor.getString(photoIndex);
                        sendContact(name, phoneNo, photo_uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 141:
                    Place place = PlacePicker.getPlace(data, MainActivity.mainActivity);
                    String placename = String.format("%s", place.getName());
                    String latitude = String.valueOf(place.getLatLng().latitude);
                    String longitude = String.valueOf(place.getLatLng().longitude);
                    String address = String.format("%s", place.getAddress());
                    sendLocation(latitude, longitude);
                    break;
                case 152:
                    String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                    uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                    uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

                    try {
                        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                        db.createFilesInfo(uniqueid,
                                com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
                                        .getString("name"),
                                com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
                                        .getString("size"),
                                "image",
                                com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
                                        .getString("filetype"), tempCameraCaptureHolderString);
                        MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[] { tempCameraCaptureHolderString }, new String[] { "image/jpeg" }, null);
                        tempCameraCaptureHolderString = "";
                        sendFileAttachment(uniqueid, "image");
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }

//        super.onActivityResult(requestCode, resultCode, data);
    }

    // TODO MUST DO ATTACHMENT WORK HERE
    public void uploadImageFromCamera(){
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File folder= getExternalStoragePublicDirForImages(getString(R.string.app_name));
        File f = new File(folder, uniqueid +".jpg");
        tempCameraCaptureHolderString = f.getPath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        getActivity().startActivityForResult(intent, 152);
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */
    @Override
    public void onClick(View v)
    {
        super.onClick(v);
        if (v.getId() == R.id.btnSend)
        {
            sendMessage();
//            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
//            db.unArchive(contactPhone);

        }

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuinfo;
        Conversation cItem = convList.get(info.position);

        if(cItem.isSent()){
            menu.setHeaderTitle(getString(R.string.common_select_action));
            menu.add(0, v.getId(), 0, getString(R.string.common_message_info));
            menu.add(0, v.getId(), 0, getString(R.string.common_remove_message));
        } else {
            menu.setHeaderTitle(getString(R.string.common_select_action));
            menu.add(0, v.getId(), 0, getString(R.string.common_remove_message));
        }
    }

    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        // TODO THIS SHOULD BE FOR BROADCAST, COMMENTED CODE IS FOR ONE TO ONE
//        if(item.getTitle() == getString(R.string.common_message_info)){
//
//            MessageInfo mInfoFrag = new MessageInfo();
//            Bundle bundle = new Bundle();
//
//            bundle.putString("authtoken",authtoken);
//            bundle.putString("message",convList.get(info.position).getMsg());
//            bundle.putString("status",convList.get(info.position).getStatus());
//            bundle.putString("date",convList.get(info.position).getDate());
//
//            mInfoFrag.setArguments(bundle);
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.content_frame, mInfoFrag, "messageInfoFragmentTag")
//                    .addToBackStack("Message Info")
//                    .commit();
//        }
//        if(item.getTitle() == getString(R.string.common_remove_message)){
//            DatabaseHandler db = new DatabaseHandler(getContext());
//            db.deleteNormalChatMessage(convList.get(info.position).getUniqueid());
//            loadConversationList();
//        }

        return true;
    }

    private void sendMessage()
    {
        try {
            if (txt.length() == 0)
                return;

            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

            String messageString = txt.getText().toString();

            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            try {
                JSONArray listMembers = db.getBroadCastListMembers(bList_id);
                for(int i=0; i<listMembers.length(); i++) {
                    db.addChat(listMembers.getJSONObject(i).getString("phone"), user.get("phone"),
                            user.get("display_name"), messageString, Utility.getCurrentTimeInISO(),
                            "pending", uniqueid, "chat", "");
                }
            }catch(JSONException ee) {
                ee.printStackTrace();
            }

            convList.add(new Conversation(messageString,
                    Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()),
                    true, true, "pending", uniqueid, "chat", ""));
            totalCount = convList.size();
            adp.notifyDataSetChanged();

            sendMessageUsingAPI(messageString, uniqueid, "chat", "");

            String []links = Utility.extractLinks(messageString);

            if(links.length > 0) {
                Utility.getURLInfo(getActivity().getApplicationContext(), links[0], uniqueid, false);
            }

            txt.setText(null);
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    // TODO MUST DO FILE ATTACHMENT IN THIS
    public void sendFileAttachment(final String uniqueid, final String fileType)
    {
        try {

            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            final JSONObject fileInfo = db.getFilesInfo(uniqueid);

            db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
                    fileInfo.getString("file_name"), Utility.getCurrentTimeInISO(), "pending", uniqueid, "file",
                    fileType);

            convList.add(new Conversation(fileInfo.getString("file_name"),
                    Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()),
                    true, true, "pending", uniqueid, "file", fileType).setFile_uri(fileInfo.getString("path")));
            adp.notifyDataSetChanged();

            final int id = 102;

            final NotificationManager mNotifyManager =
                    (NotificationManager) getActivity().getApplicationContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            final android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getActivity().getApplicationContext());
            mBuilder.setContentTitle("Uploading attachment")
                    .setContentText("Upload in progress")
                    .setSmallIcon(R.drawable.icon);

            UserFunctions userFunctions = new UserFunctions(getActivity().getApplicationContext());
            Ion.with(getActivity().getApplicationContext())
                    .load(userFunctions.getBaseURL() + "/api/filetransfers/upload")
                    .progressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            mBuilder.setProgress((int) total, (int) downloaded, false);
                            if(downloaded < total) {
                                mBuilder.setContentText("Upload in progress: "+
                                        ((downloaded / total) * 100) +"%");
                            } else {
                                mBuilder.setContentText("Uploaded file attachment");
                            }
                            mNotifyManager.notify(id, mBuilder.build());
                        }
                    })
                    .setHeader("kibo-token", authtoken)
                    .setMultipartParameter("filetype", fileType)
                    .setMultipartParameter("from", user.get("phone"))
                    .setMultipartParameter("to", contactPhone)
                    .setMultipartParameter("uniqueid", uniqueid)
                    .setMultipartParameter("filename", fileInfo.getString("file_name"))
                    .setMultipartParameter("filesize", fileInfo.getString("file_size"))
                    .setMultipartFile("file", FileUtils.getExtension(fileInfo.getString("path")), new File(fileInfo.getString("path")))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if(e == null) {
                                try {
                                    if (MainActivity.isVisible)
                                        MainActivity.mainActivity.ToastNotify2("Uploaded the file to server.");
                                    sendMessageUsingAPI(fileInfo.getString("file_name"), uniqueid, "file", fileType);
                                }catch (JSONException ee){ ee.printStackTrace(); }
                            }
                            else {
                                if(MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Some error has occurred or Internet not available. Please try later.");
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (ParseException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ChatAdapter extends BaseAdapter
    {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0)
        {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            Conversation c = getItem(pos);
            if(c.getType().equals("contact")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_contact, null);
                if (c.isSent()) {
                    TextView contact_name = (TextView) v.findViewById(R.id.contact_name);
                    TextView status = (TextView) v.findViewById(R.id.lblContactPhone);
                    contact_name.setText(c.getMsg().split(":")[0]);
                    ImageView contact_image = (ImageView) v.findViewById(R.id.contact_image);
                    DatabaseHandler db = new DatabaseHandler(MainActivity.mainActivity);
                    status.setText(c.getStatus());
                    String image_uri = db.getContactImage(c.getMsg().split(":")[1]);
                    Glide
                            .with(MainActivity.mainActivity)
                            .load(image_uri)
                            .thumbnail(0.1f)
                            .centerCrop()
                            .transform(new CircleTransform(MainActivity.mainActivity))
                            .placeholder(R.drawable.avatar)
                            .into(contact_image);
                    TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                    try {
                        lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                    if (c.isSuccess())
                        lbl.setText(c.getStatus());
                    else
                        lbl.setText("");
                }
                else {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_contact_received, null);
                    TextView contact_name = (TextView) v.findViewById(R.id.contact_name);
                    contact_name.setText(c.getMsg().split(":")[0]);
                    final Conversation cTemp = c;
                    (v.findViewById(R.id.addButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainActivity act1 = MainActivity.mainActivity;
                            act1.createContact(cTemp.getMsg().split(":")[1], cTemp.getMsg().split(":")[0]);
                        }
                    });
                    TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                    try {
                        lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return  v;
            }

            if(c.getType().equals("location")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_image, null);
                String name = user.get("display_name");

                TextView lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                if (c.isSuccess())
                    lbl.setText(c.getStatus());
                else
                    lbl.setText("");
                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_image_received, null);
                    //name = contactName;
                }
                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                final String latitude = c.getMsg().split(":")[0];
                final String longitude = c.getMsg().split(":")[1];
                ImageView container_image = (ImageView) v.findViewById(R.id.row_stamp);
                String image_uri = "http://maps.google.com/maps/api/staticmap?center="+ latitude +","+ longitude +"&zoom=18&size=500x300&sensor=TRUE_OR_FALSE";
                Glide
                        .with(MainActivity.mainActivity)
                        .load(image_uri)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_dialog_map)
                        .into(container_image);

                final String nameOfMapPerson = name;
                container_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Double lat = Double.parseDouble(latitude);
                        Double  lon = Double.parseDouble(longitude);
                        String uri = "geo:" + lat + ","
                                +lon + "?q=" + lat
                                + "," + lon;
                        startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(uri)));
                    }
                });

                return  v;
            }

            if(c.getFile_type().equals("image")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_image, null);
                String name = user.get("display_name");

                TextView lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                if (c.isSuccess())
                    lbl.setText(c.getStatus());
                else
                    lbl.setText("");
                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_image_received, null);
                    //name = contactName;
                }

                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ImageView container_image = (ImageView) v.findViewById(R.id.row_stamp);
                Glide
                        .with(MainActivity.mainActivity)
                        .load(c.getFile_uri())
                        .thumbnail(0.1f)
                        .centerCrop()
                        .placeholder(R.drawable.avatar)
                        .into(container_image);

                final String uri = c.getFile_uri();
                container_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + uri), "image/*");
                        startActivity(intent);
                    }
                });

                return  v;
            }

            if(c.getFile_type().equals("document")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_file, null);
                String name = user.get("display_name");

                TextView lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                if (c.isSuccess())
                    lbl.setText(c.getStatus());
                else
                    lbl.setText("");
                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_file_received, null);
                    //name = contactName;
                }

                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TextView msgView = (TextView) v.findViewById(R.id.file_name);
                msgView.setText(c.getMsg());

                LinearLayout fileItem = (LinearLayout) v.findViewById(R.id.fileItem);
                final String uri = c.getFile_uri();
                fileItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + uri), "application/*");
                        startActivity(intent);
                    }
                });

                return  v;
            }

            if(c.getFile_type().equals("audio")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_audio, null);
                String name = user.get("display_name");

                TextView lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                if (c.isSuccess())
                    lbl.setText(c.getStatus());
                else
                    lbl.setText("");
                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_audio_received, null);
                    //name = contactName;
                }

                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TextView msgView = (TextView) v.findViewById(R.id.file_name);
                msgView.setText(c.getFile_type());

                LinearLayout audioFileItem = (LinearLayout) v.findViewById(R.id.fileItem);
                final String uri = c.getFile_uri();
                audioFileItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + uri), "audio/*");
                        startActivity(intent);
                    }
                });

                return  v;
            }

            if(c.getFile_type().equals("video")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_audio, null); // todo change this
                String name = user.get("display_name");

                TextView lbl = (TextView) v.findViewById(R.id.lblContactPhone);
                if (c.isSuccess())
                    lbl.setText(c.getStatus());
                else
                    lbl.setText("");
                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_audio_received, null); // todo change this
                    //name = contactName;
                }

                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TextView msgView = (TextView) v.findViewById(R.id.file_name);
                msgView.setText(c.getFile_type());

                LinearLayout audioFileItem = (LinearLayout) v.findViewById(R.id.fileItem);
                final String uri = c.getFile_uri();
                audioFileItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + uri), "video/*");
                        startActivity(intent);
                    }
                });

                return  v;
            }

            if(c.getType().equals("link")){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_url, null); // todo change this
                String name = user.get("display_name");

                if (!c.isSent()) {
                    v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.chat_item_url_received, null); // todo change this
                    //name = contactName;
                }

                TextView lbl = (TextView) v.findViewById(R.id.link_title);
                lbl.setText(c.getLink_title());

                lbl = (TextView) v.findViewById(R.id.link_desc);
                lbl.setText(c.getLink());

                lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                try {
                    lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TextView msgView = (TextView) v.findViewById(R.id.msgbody);
                msgView.setText(c.getMsg());

                return  v;
            }

            if (c.isSent()) {
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_sent, null);
            } else {
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.chat_item_rcv, null);
            }



            TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
//				String date_temp = c.getDate().replaceAll("-", "/").split(" ")[0].split("/")[1] + "/" +c.getDate().replaceAll("-", "/").split(" ")[0].split("/")[2];
//				c.getDate() +" - "+ date_temp+" "+Utility.dateConversion(c.getDate().replaceAll("-", "/").split("/",2)[1].split(" ")[1])
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
            try {
                lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            TextView phone = (TextView) v.findViewById(R.id.phone);
            phone.setVisibility(View.GONE);

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) v.findViewById(R.id.lblContactPhone);
            if (c.isSuccess())
                lbl.setText(c.getStatus());
            else
                lbl.setText("");


            return v;
        }

       //  Filter Class
        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
//            if(backupList.size() < totalCount) {
//                backupList.addAll(convList);
//            }
            backupList.clear();
            loadChatFromDatabase();
            backupList.addAll(convList);
            convList.clear();
            if (charText.length() == 0) {
                convList.addAll(backupList);
            } else {
                for (Conversation conv : backupList) {
                    if (conv.getMsg().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        convList.add(conv);
                    }
                }
            }
//            Set<Conversation> duplicate = new HashSet<Conversation>(convList);
//            convList.clear();
//            convList.addAll(new ArrayList<Conversation>(duplicate));

            notifyDataSetChanged();
        }

    }




    @Override
    public String getFragmentName() {
        return "Broadcast Chat";
    }

    @Override
    public String getFragmentContactPhone() {
        return null;
    }
}
