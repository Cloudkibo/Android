package com.cloudkibo.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.model.Data;
import com.cloudkibo.utils.IFragmentName;

/**
 * The Class ContactList is the Fragment class that is launched when the user
 * clicks on Projects option in Left navigation drawer. It simply display a
 * dummy list of projects. You need to write actual implementation for loading
 * and displaying projects.
 */
public class ProjectList extends CustomFragment implements IFragmentName
{

	/** The Activity list. */
	private ArrayList<Data> projectList;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.project, null);

		loadProjectList();
		ListView list = (ListView) v.findViewById(R.id.list);
		list.setAdapter(new NoteAdapter());
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3)
			{

			}
		});

		setTouchNClick(v.findViewById(R.id.tab1));
		setTouchNClick(v.findViewById(R.id.tab2));
		return v;
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
		if (v.getId() == R.id.tab1)
		{
			getView().findViewById(R.id.tab2).setEnabled(true);
			v.setEnabled(false);
		}
		else if (v.getId() == R.id.tab2)
		{
			getView().findViewById(R.id.tab1).setEnabled(true);
			v.setEnabled(false);
		}
	}

	/**
	 * This method currently loads a dummy list of Projects. You can write the
	 * actual implementation of loading projects.
	 */
	private void loadProjectList()
	{
		ArrayList<Data> pList = new ArrayList<Data>();
		pList.add(new Data("Android new app", "My special project", 1));
		pList.add(new Data("Steps", "iOS app developer", 2));
		pList.add(new Data("To hurry up!", "Urgent!", 1));
		pList.add(new Data("Evaluate with wife", "Vacations...", 0));
		pList.add(new Data("Case Study", "Life!", 3));
		pList.add(new Data("Apratment block", "Happy hours", 2));
		pList.add(new Data("Dinner party", "Last party", 0));

		projectList = new ArrayList<Data>(pList);
		projectList.addAll(pList);
		projectList.addAll(pList);
	}

	/**
	 * The Class CutsomAdapter is the adapter class for Projects ListView. The
	 * currently implementation of this adapter simply display static dummy
	 * contents. You need to write the code for displaying actual contents.
	 */
	private class NoteAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return projectList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Data getItem(int arg0)
		{
			return projectList.get(arg0);
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
			if (v == null)
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.project_item, null);

			Data c = getItem(pos);
			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			lbl.setText(c.getTitle1());

			lbl = (TextView) v.findViewById(R.id.lbl2);
			lbl.setText(c.getDesc());

			if (c.getImage1() == 0)
				v.findViewById(R.id.vCount).setVisibility(View.INVISIBLE);
			else
			{
				v.findViewById(R.id.vCount).setVisibility(View.VISIBLE);

				lbl = (TextView) v.findViewById(R.id.l1);
				lbl.setText((pos + 1) * 2 + "");
				lbl.setEnabled(c.getImage1() != 1);

				lbl = (TextView) v.findViewById(R.id.l2);
				lbl.setText((pos + 1) * 4 + "");
				lbl.setEnabled(c.getImage1() != 2);

				lbl = (TextView) v.findViewById(R.id.l3);
				lbl.setText((pos + 1) * 3 + "");
				lbl.setEnabled(c.getImage1() != 3);
			}
			return v;
		}

	}
	
	 public String getFragmentName()
     {
       return "ProjectList";
     }

	public String getFragmentContactPhone()
	{
		return "About Chat";
	}
}
