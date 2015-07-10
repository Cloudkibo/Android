package com.cloudkibo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

/**
 * The Class AboutChat is the Fragment class that is launched when the user
 * clicks on About Chatt option in Left navigation drawer and it simply shows a
 * dummy About text. You can customize this to display actual About text.
 */
public class AboutChat extends CustomFragment implements IFragmentName
{

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.about, null);

		return v;
	}
	
	 public String getFragmentName()
     {
       return "About Chat";
     }

}
