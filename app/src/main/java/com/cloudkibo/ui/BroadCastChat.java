package com.cloudkibo.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
