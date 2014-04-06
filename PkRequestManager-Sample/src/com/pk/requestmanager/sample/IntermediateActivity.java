package com.pk.requestmanager.sample;

import java.util.List;

import com.pk.requestmanager.AppInfo;
import com.pk.requestmanager.AppLoadListener;
import com.pk.requestmanager.PkRequestManager;
import com.pk.requestmanager.RequestSettings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class IntermediateActivity extends Activity implements AppLoadListener
{
	// Request Manager
	private PkRequestManager mRequestManager;
	
	// AppInfo List
	private List<AppInfo> mApps;
	
	// UI Handler
	private Handler mHandler;
	
	// GridView & Adapter
	private GridView mGrid;
	private RequestAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intermediate);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Initialize your manager
		initRequestManager();
		
		// Initialize your UI Handler. This is for modifying your UI from a background thread
		mHandler = new Handler();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Add basic AppLoadListener to reload if empty
		mRequestManager.addAppLoadListener(this);
		
		// Load apps in the background only if they're not already loaded
		mRequestManager.loadAppsIfEmptyAsync();
		
		// Get a list of apps. This may be empty.
		mApps = mRequestManager.getApps();
		
		// Show the list of apps
		populateGrid();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		// Remove listener so it doesn't get called when the app isn't running
		mRequestManager.removeAppLoadListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.request, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
			case R.id.submitButton:
				// Small workaround
				mRequestManager.setActivity(this);
				
				// Build and send the request in the background.
				mRequestManager.sendRequestAsync();
				Toast.makeText(this, getString(R.string.building_request), Toast.LENGTH_LONG).show();
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void initRequestManager()
	{
		// Grab a reference to the manager and store it in a variable. This helps make code shorter.
		mRequestManager = PkRequestManager.getInstance(this);
		
		// Enable debugging. Disable this during production!
		mRequestManager.setDebugging(true);
		
		// Set your custom settings. Email address is required! Everything else is set to default.
		mRequestManager.setSettings(new RequestSettings.Builder()
		.addEmailAddress("example@gmail.com")
		.addEmailAddress("iconrequests@example.net") // You can add multiple emails
		.emailSubject("Icon Request")
		.emailPrecontent("These apps are missing on my phone:\n\n")
		.saveLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mytheme/.icon_request")
		.createAppfilter(true)
		.createZip(true)
		.filterDefined(true)
		.build());
	}
	
	private void populateGrid()
	{
		// Don't do anything if no apps are loaded
		if(mApps == null || mApps.size() == 0)
			return;
		
		mGrid = (GridView) findViewById(R.id.appGrid);
		mAdapter = new RequestAdapter(this, mApps);
		mGrid.setAdapter(mAdapter);

		// Set basic listener to your ListView
		mGrid.setOnItemClickListener(new OnItemClickListener () {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Mark the app as selected
				AppInfo mApp = mApps.get(position);
				mApp.setSelected(!mApp.isSelected());
				mApps.set(position, mApp);
				
				// Let the adapter know you selected something
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void onAppPreload()
	{
		// Nothing
	}
	
	@Override
	public void onAppLoading(int status, int progress)
	{
		// Nothing
	}
	
	@Override
	public void onAppLoaded()
	{
		// Use your UI handler to run this task since this gets called from a background thread.
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				// Get new list of apps and populate your list
				mApps = mRequestManager.getApps();
				populateGrid();
			}
		});
	}
	
	// You should probably put this in a separate .java file
	private class RequestAdapter extends BaseAdapter
	{
		private Context mContext;
		private List<AppInfo> mApps;
		
		public RequestAdapter(Context context, List<AppInfo> apps)
		{
			this.mContext = context;
			this.mApps = apps;
		}
		
		@Override
		public int getCount()
		{
			return mApps.size();
		}

		@Override
		public AppInfo getItem(int position)
		{
			return mApps.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			AppInfo mApp = mApps.get(position);
			
			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_intermediate_item, null);
				
				holder = new ViewHolder();
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
				holder.chkSelected = (CheckBox) convertView.findViewById(R.id.chkSelected);
				
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.txtName.setText(mApp.getName());
			holder.imgIcon.setImageDrawable(mApp.getImage());
			holder.chkSelected.setChecked(mApp.isSelected());
			
			return convertView;
		}
		
		private class ViewHolder
		{
			public ImageView imgIcon;
			public TextView txtName;
			public CheckBox chkSelected;
		}
	}
}