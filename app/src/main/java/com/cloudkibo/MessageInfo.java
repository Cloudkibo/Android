package com.cloudkibo;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MessageInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_info);
        TextView message = (TextView) this.findViewById(R.id.lbl2);
        TextView status = (TextView) this.findViewById(R.id.lblMessageStatus);
        TextView date = (TextView) this.findViewById(R.id.lblTimeStatus);

        String iMessage = getIntent().getExtras().getString("message");
        String iStatus = getIntent().getExtras().getString("status");
        String iDate = getIntent().getExtras().getString("date");

        iDate.replaceAll("-", "/").split("/",2);

        message.setText(iMessage);
        status.setText(iStatus);
        date.setText(iDate);


    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
