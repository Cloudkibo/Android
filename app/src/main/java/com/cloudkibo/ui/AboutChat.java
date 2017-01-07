package com.cloudkibo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
		setHasOptionsMenu(true);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menu != null) {
			menu.findItem(R.id.archived).setVisible(false);
			menu.findItem(R.id.language).setVisible(false);
			menu.findItem(R.id.backup_setting).setVisible(false);
		}
		inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.archived){
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	 public String getFragmentName()
     {
       return "About Chat";
     }

	public String getFragmentContactPhone()
	{
		return "About Chat";
	}

}
