package com.pkmmte.requestmanager.sample;

import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.TypedValue;
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
import com.pkmmte.requestmanager.SendRequestListener;
import com.pkmmte.requestmanager.sample.QuickScroll.Scrollable;

public class AdvancedActivity extends Activity implements OnItemClickListener, AppLoadListener, SendRequestListener
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
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
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
		
		// Get a list of apps and try populating, even if they're not loaded
		mApps = mRequestManager.getApps();
		populateGrid();
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
		mRequestManager.addAppLoadListener(this);
		mRequestManager.addSendRequestListener(this);
	}
	
	private void removeListeners()
	{
		mRequestManager.removeAppLoadListener(this);
		mRequestManager.removeSendRequestListener(this);
	}
	
	private void populateGrid()
	{
		// Return if app list is null or empty
		if(mApps == null || mApps.size() == 0)
			return;
		
		mAdapter = new RequestAdapter(this, mApps);
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(this);
		
		mScroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, mGrid, mAdapter, QuickScroll.STYLE_HOLO);
		mScroll.setFixedSize(1);
		mScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
		mScroll.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id)
	{
		mAdapter.animateView(position, mGrid);
		AppInfo mApp = mApps.get(position);
		mApp.setSelected(!mApp.isSelected());
		mApps.set(position, mApp);
	}
	
	@Override
	public void onAppPreload()
	{
		// TODO
	}
	
	@Override
	public void onAppLoading(int status, int progress)
	{
		// TODO
	}
	
	@Override
	public void onAppLoaded()
	{
		// TODO
	}
	
	@Override
	public void onRequestStart(boolean automatic)
	{
		// TODO
	}
	
	@Override
	public void onRequestBuild(boolean automatic, int progress)
	{
		// TODO
	}
	
	@Override
	public void onRequestFinished(boolean automatic, boolean intentSuccessful, Intent intent)
	{
		// TODO
	}
	
	// You should probably put this in a separate .java file
	private class RequestAdapter extends BaseAdapter implements Scrollable
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
		public String getIndicatorForPosition(int childposition, int groupposition)
		{
			return Character.toString(mApps.get(childposition).getName().charAt(0)).toUpperCase(Locale.getDefault());
		}
		
		@Override
		public int getScrollPosition(int childposition, int groupposition)
		{
			return childposition;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			AppInfo mApp = mApps.get(position);
			
			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_advanced_item, null);
				
				holder = new ViewHolder();
				holder.txtCode = (TextView) convertView.findViewById(R.id.txtCode);
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				holder.chkSelected = (ImageView) convertView.findViewById(R.id.chkSelected);

				holder.Card = (FrameLayout) convertView.findViewById(R.id.Card);
				holder.btnContainer = (FrameLayout) convertView.findViewById(R.id.btnIconContainer);
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
		
		public void animateView(int position, GridView grid)
		{
			View v = grid.getChildAt(position - grid.getFirstVisiblePosition());

			ViewHolder holder = new ViewHolder();
			holder.Card = (FrameLayout) v.findViewById(R.id.Card);
			holder.btnContainer = (FrameLayout) v.findViewById(R.id.btnIconContainer);
			holder.imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
			holder.chkSelected = (ImageView) v.findViewById(R.id.chkSelected);
			holder.bgSelected = v.findViewById(R.id.bgSelected);

			if (mApps.get(position).isSelected())
				animateAppDeselected(holder);
			else
				animateAppSelected(holder);

		}
		
		private void animateAppSelected(final ViewHolder holderFinal)
		{
			// Declare AnimatorSets
			final AnimatorSet animOut = (AnimatorSet) AnimatorInflater
					.loadAnimator(mContext, R.anim.card_flip_right_out);
			final AnimatorSet animIn = (AnimatorSet) AnimatorInflater.loadAnimator(
					mContext, R.anim.card_flip_left_in);
			animOut.setTarget(holderFinal.btnContainer);
			animIn.setTarget(holderFinal.btnContainer);
			animOut.addListener(new AnimatorListener() {
				@Override
				public void onAnimationCancel(Animator animation) {
					// Nothing
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					selectCard(true, holderFinal.Card);
					holderFinal.bgSelected.setVisibility(View.VISIBLE);
					holderFinal.chkSelected.setVisibility(View.VISIBLE);
					animIn.start();
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// Nothing
				}

				@Override
				public void onAnimationStart(Animator animation) {
					selectCard(false, holderFinal.Card);
					holderFinal.bgSelected.setVisibility(View.GONE);
					holderFinal.chkSelected.setVisibility(View.GONE);
				}
			});
			animOut.start();
		}

		private void animateAppDeselected(final ViewHolder holderFinal)
		{
			// Declare AnimatorSets
			final AnimatorSet animOut = (AnimatorSet) AnimatorInflater
					.loadAnimator(mContext, R.anim.card_flip_left_out);
			final AnimatorSet animIn = (AnimatorSet) AnimatorInflater.loadAnimator(
					mContext, R.anim.card_flip_right_in);
			animOut.setTarget(holderFinal.btnContainer);
			animIn.setTarget(holderFinal.btnContainer);
			animOut.addListener(new AnimatorListener() {
				@Override
				public void onAnimationCancel(Animator animation) {
					// Nothing
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					selectCard(false, holderFinal.Card);
					holderFinal.bgSelected.setVisibility(View.GONE);
					holderFinal.chkSelected.setVisibility(View.GONE);
					animIn.start();
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// Nothing
				}

				@Override
				public void onAnimationStart(Animator animation) {
					selectCard(true, holderFinal.Card);
					holderFinal.bgSelected.setVisibility(View.VISIBLE);
					holderFinal.chkSelected.setVisibility(View.VISIBLE);
				}
			});
			animOut.start();
		}
		
		private class ViewHolder
		{
			public TextView txtCode;
			public TextView txtName;
			public ImageView imgIcon;
			public ImageView chkSelected;

			public FrameLayout Card;
			public FrameLayout btnContainer;
			public View bgSelected;
		}
	}
}