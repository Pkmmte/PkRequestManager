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
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.pkmmte.requestmanager.AppInfo;
import com.pkmmte.requestmanager.AppLoadListener;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;
import com.pkmmte.requestmanager.SendRequestListener;
import com.pkmmte.requestmanager.sample.QuickScroll.Scrollable;

public class AdvancedActivity extends Activity implements OnItemClickListener, OnQueryTextListener, AppLoadListener, SendRequestListener
{
	// Request Manager
	private PkRequestManager mRequestManager;
	
	// AppInfo List & Adapter
	private List<AppInfo> mApps;
	private RequestAdapter mAdapter;
	
	// UI Handler
	private Handler mHandler;
	
	// Views
	private SearchView searchView;
	private GridView mGrid;
	private QuickScroll mScroll;
	private LinearLayout containerLoading;
	private TextView txtLoading;
	private TextView txtSelected;
	private ImageButton btnSelectAll;
	private ImageButton btnDeselectAll;
	
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
		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setOnQueryTextListener(this);
		
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (!searchView.isIconified())
			{
				searchView.onActionViewCollapsed();
				searchView.setEnabled(false);
				
				return true;
			}
			else
				finish();
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private void initViews()
	{
		mGrid = (GridView) findViewById(R.id.appGrid);
		mScroll = (QuickScroll) findViewById(R.id.quickScroll);
		containerLoading = (LinearLayout) findViewById(R.id.Loading);
		txtLoading = (TextView) findViewById(R.id.txtLoading);
		txtSelected = (TextView) findViewById(R.id.txtNumSelected);
		btnSelectAll = (ImageButton) findViewById(R.id.btnSelectAll);
		btnDeselectAll = (ImageButton) findViewById(R.id.btnDeselectAll);
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
		// Load & Request Listeners
		mRequestManager.addAppLoadListener(this);
		mRequestManager.addSendRequestListener(this);
		
		// Select/Deselect All Button Listeners
		btnSelectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Return if not yet ready
				if(mApps == null || mAdapter == null)
					return;
				
				// Keep track of previously selected apps for animation
				SparseBooleanArray selectedApps = new SparseBooleanArray();
				int size1 = mApps.size();
				for(int i = 0; i < size1; i++) {
					selectedApps.put(i, mApps.get(i).isSelected());
				}
				
				// Select all
				mRequestManager.selectAll();
				
				// Animate
				int size2 = mApps.size();
				for(int i = 0; i < size2; i++) {
					if(!selectedApps.get(i))
						mAdapter.animateView(i, mGrid);
				}
				
				// Update Number of apps selected
				int numSelected = mRequestManager.getNumSelected();
				txtSelected.setText(getResources().getQuantityString(R.plurals.num_apps_selected, numSelected, numSelected));
			}
		});
		btnDeselectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Return if not yet ready
				if(mApps == null || mAdapter == null)
					return;
				
				// Keep track of previously selected apps for animation
				SparseBooleanArray selectedApps = new SparseBooleanArray();
				int size1 = mApps.size();
				for(int i = 0; i < size1; i++) {
					selectedApps.put(i, mApps.get(i).isSelected());
				}
				
				// Deselect all
				mRequestManager.deselectAll();
				
				// Animate
				int size = mApps.size();
				for(int i = 0; i < size; i++) {
					if(selectedApps.get(i))
						mAdapter.animateView(i, mGrid);
				}
				
				// Update Number of apps selected
				int numSelected = mRequestManager.getNumSelected();
				txtSelected.setText(getResources().getQuantityString(R.plurals.num_apps_selected, numSelected, numSelected));
			}
		});
		
		// Fancy toast
		btnSelectAll.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(AdvancedActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		btnDeselectAll.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(AdvancedActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}
	
	private void removeListeners()
	{
		mRequestManager.removeAppLoadListener(this);
		mRequestManager.removeSendRequestListener(this);
		btnSelectAll.setOnClickListener(null);
		btnDeselectAll.setOnClickListener(null);
	}
	
	private void populateGrid()
	{
		// Do nothing if app list is null or empty
		if(mApps == null || mApps.size() == 0)
			return;
		
		// Hide loading views
		containerLoading.setVisibility(View.GONE);
		
		// Populate the GridView
		mAdapter = new RequestAdapter(this, mApps);
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(this);
		mGrid.setVisibility(View.VISIBLE);
		
		// Initialize & Configure QuickScroll
		mScroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, mGrid, mAdapter, QuickScroll.STYLE_HOLO);
		mScroll.setFixedSize(1);
		mScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
		mScroll.setVisibility(View.VISIBLE);
		
		// Update Number of apps selected (if any were already selected)
		int numSelected = mRequestManager.getNumSelected();
		txtSelected.setText(getResources().getQuantityString(R.plurals.num_apps_selected, numSelected, numSelected));
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id)
	{
		// Trigger fancy animation
		mAdapter.animateView(position, mGrid);
		
		// Mark the app as selected
		AppInfo mApp = mApps.get(position);
		mApp.setSelected(!mApp.isSelected());
		mApps.set(position, mApp);
		
		// Let the adapter know you selected something
		//mAdapter.notifyDataSetChanged();
		
		// Update Number of apps selected
		int numSelected = mRequestManager.getNumSelected();
		txtSelected.setText(getResources().getQuantityString(R.plurals.num_apps_selected, numSelected, numSelected));
	}
	
	@Override
	public boolean onQueryTextSubmit(String query)
	{
		// TODO
		
		// Hide Keyboard
		((InputMethodManager) getSystemService(FragmentActivity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		
		return false;
	}
	
	@Override
	public boolean onQueryTextChange(String newText)
	{
		// TODO
		return false;
	}
	
	@Override
	public void onAppPreload()
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				if(containerLoading.isShown())
					txtLoading.setText(getResources().getString(R.string.preparing_to_load));
			}
		});
	}
	
	@Override
	public void onAppLoading(final int status,  final int progress)
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				if(containerLoading.isShown())
					txtLoading.setText("Loading... " + progress + "%");
			}
		});
	}
	
	@Override
	public void onAppLoaded()
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				mApps = mRequestManager.getApps();
				populateGrid();
			}
		});
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
			return this.mApps.size();
		}

		@Override
		public AppInfo getItem(int position)
		{
			return this.mApps.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}
		
		@Override
		public String getIndicatorForPosition(int childposition, int groupposition)
		{
			return Character.toString(this.mApps.get(childposition).getName().charAt(0)).toUpperCase(Locale.getDefault());
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
			AppInfo mApp = this.mApps.get(position);
			
			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_advanced_item, null);
				
				holder = new ViewHolder();
				holder.txtCode = (TextView) convertView.findViewById(R.id.txtCode);
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);

				holder.Card = (FrameLayout) convertView.findViewById(R.id.Card);
				holder.btnContainer = (FrameLayout) convertView.findViewById(R.id.btnIconContainer);
				holder.imgSelected = (ImageView) convertView.findViewById(R.id.imgSelected);
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
				holder.imgSelected.setVisibility(View.VISIBLE);
			}
			else {
				selectCard(false, holder.Card);
				holder.bgSelected.setVisibility(View.GONE);
				holder.imgSelected.setVisibility(View.GONE);
			}
			
			return convertView;
		}
		
		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		private void selectCard(boolean Selected, FrameLayout Card)
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				Card.setBackgroundDrawable(Selected ? mRes.getDrawable(R.drawable.card_bg_selected) : mRes.getDrawable(R.drawable.card_bg));
			else
				Card.setBackground(Selected ? mRes.getDrawable(R.drawable.card_bg_selected) : mRes.getDrawable(R.drawable.card_bg));
		}
		
		public void animateView(int position, GridView grid)
		{
			try {
				View v = grid.getChildAt(position - grid.getFirstVisiblePosition());
	
				ViewHolder holder = new ViewHolder();
				holder.Card = (FrameLayout) v.findViewById(R.id.Card);
				holder.btnContainer = (FrameLayout) v.findViewById(R.id.btnIconContainer);
				holder.imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
				holder.imgSelected = (ImageView) v.findViewById(R.id.imgSelected);
				holder.bgSelected = v.findViewById(R.id.bgSelected);
	
				if (this.mApps.get(position).isSelected())
					animateAppDeselected(holder);
				else
					animateAppSelected(holder);
			}
			catch(Exception e) {
				// View not visible
			}
		}
		
		private void animateAppSelected(final ViewHolder holderFinal)
		{
			// Declare AnimatorSets
			final AnimatorSet animOut = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.card_flip_right_out);
			final AnimatorSet animIn = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.card_flip_left_in);
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
					holderFinal.imgSelected.setVisibility(View.VISIBLE);
					notifyDataSetChanged();
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
					holderFinal.imgSelected.setVisibility(View.GONE);
				}
			});
			animOut.start();
		}

		private void animateAppDeselected(final ViewHolder holderFinal)
		{
			// Declare AnimatorSets
			final AnimatorSet animOut = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.card_flip_left_out);
			final AnimatorSet animIn = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.card_flip_right_in);
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
					holderFinal.imgSelected.setVisibility(View.GONE);
					notifyDataSetChanged();
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
					holderFinal.imgSelected.setVisibility(View.VISIBLE);
				}
			});
			animOut.start();
		}
		
		private class ViewHolder
		{
			public TextView txtCode;
			public TextView txtName;
			public ImageView imgIcon;

			public FrameLayout Card;
			public FrameLayout btnContainer;
			public ImageView imgSelected;
			public View bgSelected;
		}
	}
}