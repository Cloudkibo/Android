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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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

    private ArrayList<Conversation> convList;
    private ChatAdapter adp;
    private EditText txt;

    private String authtoken;

    private HashMap<String, String> user;

    String list_name;
    String bList_id;
    private String tempCameraCaptureHolderString;

    View view;
    EditText editsearch;
    LinearLayout search_view;
    public static int totalCount = 0;

    LinearLayout mRevealView;
    Boolean attachmentViewHidden = true;



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

//        if(contactName.equals(contactPhone)){
//            LinearLayout tabs = (LinearLayout) v.findViewById(R.id.newContactOptionsBtns);
//            tabs.setVisibility(View.VISIBLE);
//            Button tab1 = (Button) v.findViewById(R.id.tab1);
//            tab1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    JSONObject body = new JSONObject();
//                    try {
//                        body.put("phone", contactPhone);
//                        Utility.blockContact(getActivity().getApplicationContext(), body, authtoken);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Utility.sendLogToServer(getActivity().getApplicationContext(), "Block Contact failed on android in GroupChat");
//                    }
//                }
//            });
//            Button tab2 = (Button) v.findViewById(R.id.tab2);
//            tab2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    MainActivity act1 = (MainActivity) getActivity();
//                    act1.createContact(contactPhone);
//                }
//            });
//        }

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        user = db.getUserDetails();

        //loadConversationList();

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
                //adp.filter(text);

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
                //adp.filter("");
            }
        });

        //lastSeenStatus();


        return v;
    }




    /** attachment work starts **/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        inflater.inflate(R.menu.groupchat, menu);  // Use filter.xml from step 1
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_imageview, null);
        ImageView search_button = (ImageView) v.findViewById(R.id.imageView4);
        ImageView attach_button = (ImageView) v.findViewById(R.id.imageView5);

        attach_button.setVisibility(View.VISIBLE);

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
                            act2.uploadChatAttachment("document", "group");
                        }
                    });
                    sendAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            MainActivity act2 = (MainActivity)getActivity();
                            act2.uploadChatAttachment("audio", "group");
                        }
                    });
                    sendVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animator_reverse.start();
                            MainActivity act2 = (MainActivity)getActivity();
                            act2.uploadChatAttachment("video", "group");
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
                                act2.uploadChatAttachment("document", "group");
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
                                act2.uploadChatAttachment("audio", "group");
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
                                act2.uploadChatAttachment("video", "group");
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

    }

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
                    act3.uploadChatAttachment("image", "group");
                } else if (options[item].equals(R.string.cancel)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        Log.i(TAG, "onActivityResult: Location");
        if (resultCode == Activity.RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case 0123:
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
                case 0141:
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
                default:
                    Toast.makeText(getContext(), "Could not get the contact you selected", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }

//        super.onActivityResult(requestCode, resultCode, data);
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

    public void sendMessage(EditText my_message){
        String message = my_message.getText().toString();
        if(message.trim().equals("")){
            return;
        }
        DatabaseHandler db = new DatabaseHandler(getContext());
        GroupUtility groupUtility = new GroupUtility(getContext());
        String msg_unique_id = groupUtility.sendGroupMessage(group_id, message, authtoken, "chat");
        messages.add(message);
        names.add("");

        try {
            convList.add(new Conversation(message, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status"), "chat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String []links = Utility.extractLinks(message);

        if(links.length > 0) {
            Utility.getURLInfo(getActivity().getApplicationContext(), links[0], msg_unique_id, true);
        }

        groupAdapter.notifyDataSetChanged();
        my_message.setText("");
    }

    public void sendFileAttachment(final String uniqueid, final String fileType)
    {
        try {

            final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            final JSONObject fileInfo = db.getFilesInfo(uniqueid);

            db.addGroupMessage(group_id,fileInfo.getString("file_name"), db.getUserDetails().get("phone"),"", uniqueid, fileType);
            try {
                JSONArray group_members = db.getGroupMembers(group_id);
                for (int i = 0; i < group_members.length(); i++)
                {
                    JSONObject member = group_members.getJSONObject(i);
                    db.addGroupChatStatus(uniqueid, "pending", member.getString("phone"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            messages.add(fileInfo.getString("file_name"));
            names.add("");

            try {
                convList.add(new Conversation(fileInfo.getString("file_name"),
                        db.getUserDetails().get("phone"), true,"",
                        uniqueid,
                        db.getGroupMessageStatus(uniqueid, db.getUserDetails().get("phone"))
                                .getJSONObject(0).getString("status"), "contact").setFile_uri(fileInfo.getString("path")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            groupAdapter.notifyDataSetChanged();

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
                    .load(userFunctions.getBaseURL() + "/api/filetransfersgroup/upload")
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
                    .setMultipartParameter("file_type", fileType)
                    .setMultipartParameter("from", db.getUserDetails().get("phone"))
                    .setMultipartParameter("group_unique_id", group_id)
                    .setMultipartParameter("total_members", Integer.toString(db.getGroupMembers(group_id).length()))
                    .setMultipartParameter("uniqueid", uniqueid)
                    .setMultipartParameter("file_name", fileInfo.getString("file_name"))
                    .setMultipartParameter("file_size", fileInfo.getString("file_size"))
                    .setMultipartFile("file", FileUtils.getExtension(fileInfo.getString("path")), new File(fileInfo.getString("path")))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if(e == null) {
                                if (MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Uploaded the file to server.");
                                new AsyncTask<String, String, JSONObject>() {

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        String msg = "";
                                        try{
                                            msg = fileInfo.getString("file_name");
                                        } catch (JSONException e55) {
                                            e55.printStackTrace();
                                        }
                                        UserFunctions user = new UserFunctions(getActivity().getApplicationContext());
                                        return user.sendGroupChat(group_id,db.getUserDetails().get("phone"),fileType,msg,db.getUserDetails().get("display_name"),uniqueid, authtoken);
                                    }

                                    @Override
                                    protected void onPostExecute(JSONObject row) {
                                        if(!row.optString("group_unique_id").equals("")){
                                            try {
                                                JSONArray group_members = db.getGroupMembers(group_id);
                                                for (int i = 0; i < group_members.length(); i++)
                                                {
                                                    JSONObject member = group_members.getJSONObject(i);
                                                    db.updateGroupChatStatus(uniqueid, "sent", member.getString("phone"));
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            if(MainActivity.isVisible) {
                                                MainActivity.mainActivity.updateGroupUIChat();
                                            }
                                        }else if(row.optString("Error").equals("No Internet")){
                                            // todo good for debug but remove in release
                                            //sendNotification("No Internet Connection", "Message will be sent as soon as the device gets connected to internet");
                                        }else{
                                            // todo good for debug but remove in release
                                            //sendNotification("Failed to Send Message", "Oops message was not sent due to some reason");
                                        }
                                    }

                                }.execute();
                            }
                            else {
                                if(MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Some error has occurred or Internet not available. Please try later.");
                                e.printStackTrace();
                            }
                        }
                    });
        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendContact(String display_name, String phone, String contact_image){
        String message = display_name + ":" + phone;
        DatabaseHandler db = new DatabaseHandler(getContext());
        GroupUtility groupUtility = new GroupUtility(getContext());
        String msg_unique_id = groupUtility.sendGroupMessage(group_id, message, authtoken, "contact");
        messages.add(message);
        names.add("");

        try {
            convList.add(new Conversation(message, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status"), "contact").setContact_image(contact_image));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        groupAdapter.notifyDataSetChanged();
    }


    private void sendLocation(String latitude, String longitude)
    {
        try {
            String messageString = latitude + ":" + longitude;
            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            GroupUtility groupUtility = new GroupUtility(getContext());
            String msg_unique_id = groupUtility.sendGroupMessage(group_id, messageString, authtoken, "location");
            messages.add(messageString);
            names.add("");

            convList.add(new Conversation(messageString, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status"), "location"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        groupAdapter.notifyDataSetChanged();
    }

    public void populateMessages(){
        DatabaseHandler db = new DatabaseHandler(getContext());
        messages.clear();
        names.clear();
        convList.clear();
        try {
            JSONArray msgs = db.getGroupMessages(group_id);
            for (int i = 0; i < msgs.length(); i++) {
                final JSONObject row = msgs.getJSONObject(i);
                messages.add(msgs.getJSONObject(i).get("msg").toString());
                names.add(msgs.getJSONObject(i).get("from_fullname").toString());
                String message = msgs.getJSONObject(i).get("msg").toString();
                String from = msgs.getJSONObject(i).get("from").toString();
                String date = msgs.getJSONObject(i).get("date").toString();
                String unique_id = msgs.getJSONObject(i).get("unique_id").toString();
                String type = msgs.getJSONObject(i).get("type").toString();
                boolean isSent = false;
                if(db.getUserDetails().get("phone").equals(from)){
                    isSent = true;
                    from = "You";
                }
                String display_name = db.getDisplayName(from);
                if(!display_name.equals("")){
                    from = display_name;
                }
                final String tmpFrom = from;
                JSONArray msgStatus = db.getGroupMessageStatusSeen(unique_id);
                String status = "";
//                if(msgStatus.length() != 0){
//                    status = msgStatus.getJSONObject(0).getString("status");
//                }

                if(msgStatus.length() >= db.getGroupMembers(group_id).length() - 1){
                    status = "seen";
                }
                else if(db.getGroupMessageStatusDelivered(unique_id).length() >= db.getGroupMembers(group_id).length() - 1){
                    status = "delivered";
                }
                else if(db.getGroupMessageStatusPending(unique_id).length() >= db.getGroupMembers(group_id).length() - 1){
                    status = "pending";
//                    String temp = db.getGroupMessageStatus(unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status");
//                    Toast.makeText(getContext(), temp, Toast.LENGTH_LONG).show();
                }
                else {
                    status = "sent";
                }

                if(!from.equals("You")){
                    JSONArray arrayForMyStatus = db.getGroupMessageStatusDelivered(unique_id);
                    for( int k=0; k<arrayForMyStatus.length(); k++ ) {
                        JSONObject jsonForMyStatus = arrayForMyStatus.getJSONObject(k);
                        if(jsonForMyStatus.getString("status").equals("delivered")){
                            final String uniqueIdTemp = unique_id;
                            new AsyncTask<String, String, JSONObject>() {
                                @Override
                                protected JSONObject doInBackground(String... args) {
                                    UserFunctions userFunctions = new UserFunctions(getActivity().getApplicationContext());
                                    updateChatStatus(uniqueIdTemp, "seen");
                                    addChatHistorySync(uniqueIdTemp, tmpFrom);
                                    return userFunctions.updateGroupChatStatusToSeen(uniqueIdTemp, authtoken);
                                }
                                @Override
                                protected void onPostExecute(JSONObject row) {
                                    if(row != null){
                                        if(row.has("status")) {
                                            resetSpecificChatHistorySync(uniqueIdTemp);
                                            updateChatStatus(uniqueIdTemp, "seen");
                                        }
                                    }
                                }
                            }.execute();
                        }
                    }
                }

                Conversation conv = new Conversation(message, from, isSent, date, unique_id, status, type);

                if(conv.getType().equals("image") || conv.getType().equals("document") ||
                        conv.getType().equals("audio") || conv.getType().equals("video")) {

                    if(from.equals("You")){
                        JSONObject fileInfo = db.getFilesInfo(unique_id);
                        String path = "";
                        try {
                            if (fileInfo.has("path")) path = fileInfo.getString("path");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        conv.setFile_uri(path);
                    } else {
                        JSONObject fileInfo = db.getFilesInfo(unique_id);
                        String path = "";
                        try {
                            if (fileInfo.has("path")) path = fileInfo.getString("path");
                            if (fileInfo.getString("file_size").equals("notDownloaded")) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    // todo check if we need this really or not. This might be irritating to user
                                    Utility.sendNotification(getActivity().getApplicationContext(), "Storage Permissions", "This conversation contains file attachments to be downloaded. Please give storage permission from settings and come back.");
                                } else {
                                    downloadPendingFile(row);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        conv.setFile_uri(path);
                    }

                } else if(row.getString("type").equals("link")){
                    JSONObject fileInfo = db.getLinksInfo(row.getString("uniqueid"));
                    try {
                        conv.setLinkInfo(fileInfo.getString("link"), fileInfo.getString("title"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    String[] links = Utility.extractLinks(message);

                    if (links.length > 0) {
                        Utility.getURLInfo(getActivity().getApplicationContext(), links[0], unique_id, true);
                    }
                }

                convList.add(conv);

            }

            if(groupAdapter != null && lv != null){
                groupAdapter.notifyDataSetChanged();
//                lv.smoothScrollToPosition(lv.getCount()+1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadPendingFile(final JSONObject row) {
        try {
            final int id = 101;

            final NotificationManager mNotifyManager =
                    (NotificationManager) getActivity().getApplicationContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            final android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getActivity().getApplicationContext());
            mBuilder.setContentTitle("Downloading attachment")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.icon);

            final UserFunctions userFunctions = new UserFunctions(getActivity().getApplicationContext());

            Ion.with(getActivity().getApplicationContext())
                    .load(userFunctions.getBaseURL() + "/api/filetransfersgroup/download")
                    .progressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            mBuilder.setProgress((int) total, (int) downloaded,
                                    false);
                            if (downloaded < total) {
                                mBuilder.setContentText("Download in progress: " +
                                        ((downloaded / total) * 100) + "%");
                            } else {
                                mBuilder.setContentText("Downloaded file attachment");
                            }
                            mNotifyManager.notify(id, mBuilder.build());
                        }
                    })
                    .setHeader("kibo-token", authtoken)
                    .setBodyParameter("uniqueid", row.getString("unique_id"))
                    .write(new File(getActivity().getApplicationContext().getFilesDir().getPath() + "" + row.getString("unique_id")))
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File file) {
                            // download done...
                            // do stuff with the File or error

                            try {
                                DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                                File folder = getExternalStoragePublicDirForImages(getActivity().getString(R.string.app_name));
                                if (row.getString("type").equals("document")) {
                                    folder = getExternalStoragePublicDirForDocuments(getActivity().getString(R.string.app_name));
                                }
                                if (row.getString("type").equals("audio")) {
                                    folder = getExternalStoragePublicDirForDownloads(getActivity().getString(R.string.app_name));
                                }
                                if (row.getString("type").equals("video")) {
                                    folder = getExternalStoragePublicDirForDownloads(getActivity().getString(R.string.app_name));
                                }
                                FileOutputStream outputStream;
                                outputStream = new FileOutputStream(folder.getPath() + "/" + row.getString("msg"));
                                outputStream.write(com.cloudkibo.webrtc.filesharing.Utility.convertFileToByteArray(file));
                                outputStream.close();

                                JSONObject fileMetaData = getFileMetaData(folder.getPath() + "/" + row.getString("msg"));
                                db.createFilesInfoGroup(group_id, row.getString("unique_id"),
                                        fileMetaData.getString("name"),
                                        fileMetaData.getString("size"),
                                        row.getString("type"),
                                        fileMetaData.getString("filetype"), folder.getPath() + "/" + row.getString("msg"));

                                updateFileDownloaded(row.getString("unique_id"));

                                final AccessToken accessToken = AccountKit.getCurrentAccessToken();

                                new AsyncTask<String, String, JSONObject>() {
                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        try {
                                            UserFunctions userFunctions1 = new UserFunctions(getActivity().getApplicationContext());
                                            return userFunctions1.confirmFileDownloadGroup(row.getString("unique_id"), accessToken.getToken());
                                        } catch (JSONException e5) {
                                            e5.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(JSONObject row) {
                                        if (row != null) {
                                            // todo see if server couldn't get the confirmation
                                        }
                                    }
                                }.execute();

                                file.delete();

                                Log.d("chat attachment", "Downloaded file attachment");

                            } catch (IOException e2) {
                                e2.printStackTrace();
                            } catch (JSONException e3) {
                                e3.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /** end attachment work */








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

        // Filter Class
//        public void filter(String charText) {
//            charText = charText.toLowerCase(Locale.getDefault());
////            if(backupList.size() < totalCount) {
////                backupList.addAll(convList);
////            }
//            backupList.clear();
//            loadChatFromDatabase();
//            backupList.addAll(convList);
//            convList.clear();
//            if (charText.length() == 0) {
//                convList.addAll(backupList);
//            } else {
//                for (Conversation conv : backupList) {
//                    if (conv.getMsg().toLowerCase(Locale.getDefault())
//                            .contains(charText)) {
//                        convList.add(conv);
//                    }
//                }
//            }
////            Set<Conversation> duplicate = new HashSet<Conversation>(convList);
////            convList.clear();
////            convList.addAll(new ArrayList<Conversation>(duplicate));
//
//            notifyDataSetChanged();
//        }

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
