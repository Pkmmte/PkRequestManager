package com.pkmmte.requestmanager.sample;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkmmte.requestmanager.AppInfo;
import com.pkmmte.requestmanager.AppLoadListener;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;

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
			case R.id.send:
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
		
		// Set your custom settings. Email address is required! Everything else is set to default if not specified.
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
		private Resources mRes;
		
		public RequestAdapter(Context context, List<AppInfo> apps)
		{
			this.mContext = context;
			this.mApps = apps;
			this.mRes = context.getResources();
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
			
			// Inflate layout if null, otherwise use current ViewHolder
			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_intermediate_item, null);
				
				holder = new ViewHolder();
				holder.Card = (FrameLayout) convertView.findViewById(R.id.Card);
				holder.txtCode = (TextView) convertView.findViewById(R.id.txtCode);
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Set the app's name & code
			holder.txtName.setText(mApp.getName());
			holder.txtCode.setText(mApp.getCode());
			
			// Set selection/icon
			holder.imgIcon.setImageDrawable(mApp.isSelected() ? mRes.getDrawable(R.drawable.ic_selected) : mApp.getImage());
			selectCard(mApp.isSelected(), holder.Card);
			
			return convertView;
		}
		
		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		private void selectCard(boolean Selected, FrameLayout Card)
		{
			// Use new API beyond Jelly Bean but keep compatibility with ICS
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				Card.setBackgroundDrawable(Selected ? mRes.getDrawable(R.drawable.card_bg_selected) : mRes.getDrawable(R.drawable.card_bg));
			}
			else {
				Card.setBackground(Selected ? mRes.getDrawable(R.drawable.card_bg_selected) : mRes.getDrawable(R.drawable.card_bg));
			}
		}
		
		private class ViewHolder
		{
			public FrameLayout Card;
			public TextView txtCode;
			public TextView txtName;
			public ImageView imgIcon;
		}
	}
}
