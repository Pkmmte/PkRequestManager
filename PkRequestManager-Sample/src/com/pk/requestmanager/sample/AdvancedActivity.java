package com.pk.requestmanager.sample;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pk.requestmanager.AppInfo;
import com.pk.requestmanager.AppLoadListener;
import com.pk.requestmanager.PkRequestManager;
import com.pk.requestmanager.RequestSettings;

public class AdvancedActivity extends Activity implements AppLoadListener
{
	// Request Manager
	private PkRequestManager mRequestManager;
	
	// AppInfo List & Adapter
	private List<AppInfo> mApps;
	private RequestAdapter mAdapter;
	
	// UI Handler
	private Handler mHandler;
	
	// Views
	private GridView mGrid;
	private QuickScroll mScroll;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced);
		
		// Initialize your layout views
		initViews();
		
		// Initialize your PkRequestManager
		initRequestManager();
		
		// Initialize your UI Handler. This is for modifying your UI from a background thread
		mHandler = new Handler();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Event listeners to notify us when a task has finished executing
		setListeners();
		
		// Asynchronously load apps if they're not already loaded.
		mRequestManager.loadAppsIfEmptyAsync();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		// Remove listeners when the app isn't visible to prevent any issues
		removeListeners();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.request_advanced, menu);
		
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
				
				if(mRequestManager.getNumSelected() > 0) {
					// Build and send the request in the background if apps are selected.
					mRequestManager.sendRequestAsync();
					Toast.makeText(this, getString(R.string.building_request), Toast.LENGTH_LONG).show();
				}
				else {
					// Show error message if no apps are selected
					Toast.makeText(this, getString(R.string.none_selected), Toast.LENGTH_LONG).show();
				}
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void initViews()
	{
		mGrid = (GridView) findViewById(R.id.appGrid);
		mScroll = (QuickScroll) findViewById(R.id.quickScroll);
	}
	
	private void initRequestManager()
	{
		// Grab a reference to the manager and store it in a variable. This helps make code shorter.
		mRequestManager = PkRequestManager.getInstance(this);
		
		// Enable debugging. Disable this during production!
		mRequestManager.setDebugging(true);
		
		// Set your custom settings.
		mRequestManager.setSettings(new RequestSettings.Builder()
	    	.addEmailAddress("iconrequests@example.net")    // Email where the request will be sent to
	    	.addEmailAddress("example@gmail.com")   // You can specify multiple emails to send it to
	    	.emailSubject("[MyIconPack] App Icon Request")  // Email Subject
	    	.emailPrecontent("These apps are missing on my phone:\n\n") // Text before the main app information
	    	.saveLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mytheme/.icon_request")   // Location to where the .zips and temporary files will be saved
	    	.appfilterName("appfilter.xml") // Specify your appfilter.xml name if it's different from the standard. This will be used to filter out apps from the list.
	    	.compressFormat(PkRequestManager.PNG)   // Compression format for the attached app icons
	    	.appendInformation(true)    // Choose whether or not you'd like to receive information about the user's device such as OS version, manufacturer, model number, build, etc.
	    	.createAppfilter(true)  // True if you'd like to automatically generate an appfilter.xml for the requested apps
	    	.createZip(true)    // True if you want to receive app icons with the email
	    	.filterAutomatic(true)  // True if you want apps you support in your appfilter.xml to be filtered out from automatic requests
	    	.filterDefined(true)    // True if you don't want apps you already defined in your appfilter.xml to show up in the app list
	    	.byteBuffer(2048)   // Buffer size in bytes for writing to memory.
	    	.compressQuality(100)   // Compression quality for attached app icons
	    	.build());
	}
	
	private void setListeners()
	{
		
	}
	
	private void removeListeners()
	{
		
	}

	@Override
	public void onAppLoaded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAppLoading(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAppPreload() {
		// TODO Auto-generated method stub
		
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
					holder.txtCode = (TextView) convertView.findViewById(R.id.txtCode);
					holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
					holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
					holder.chkSelected = (ImageView) convertView.findViewById(R.id.chkSelected);

					holder.Card = (FrameLayout) convertView.findViewById(R.id.Card);
					holder.bgSelected = convertView.findViewById(R.id.bgSelected);
					
					convertView.setTag(holder);
				}
				else {
					holder = (ViewHolder) convertView.getTag();
				}

				holder.txtName.setText(mApp.getName());
				holder.txtCode.setText(mApp.getCode());
				holder.imgIcon.setImageDrawable(mApp.getImage());
				
				if(mApp.isSelected()) {
					selectCard(true, holder.Card);
					holder.bgSelected.setVisibility(View.VISIBLE);
					holder.chkSelected.setVisibility(View.VISIBLE);
				}
				else {
					selectCard(false, holder.Card);
					holder.bgSelected.setVisibility(View.GONE);
					holder.chkSelected.setVisibility(View.GONE);
				}
				
				return convertView;
			}
			
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			private void selectCard(boolean Selected, FrameLayout Card)
			{
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					if (Selected)
						Card.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.card_selected));
					else
						Card.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.card_bg));
				}
				else {
					if (Selected)
						Card.setBackground(mContext.getResources().getDrawable(R.drawable.card_selected));
					else
						Card.setBackground(mContext.getResources().getDrawable(R.drawable.card_bg));
				}
			}
			
			private class ViewHolder
			{
				public TextView txtCode;
				public TextView txtName;
				public ImageView imgIcon;
				public ImageView chkSelected;

				public FrameLayout Card;
				public View bgSelected;
			}
		}
}