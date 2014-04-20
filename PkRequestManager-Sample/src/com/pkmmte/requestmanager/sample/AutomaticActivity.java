package com.pkmmte.requestmanager.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pkmmte.requestmanager.AppLoadListener;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;
import com.pkmmte.requestmanager.SendRequestListener;

public class AutomaticActivity extends Activity implements OnClickListener, AppLoadListener, SendRequestListener
{
	// Request Manager
	private PkRequestManager mRequestManager;
	
	// Progress Dialog
	private ProgressDialog progressDialog;
	
	// UI Handler
	private Handler mHandler;
	
	// Views
	private Button btnSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_automatic);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Initialize your layout views
		initViews();
		
		// Initialize your manager
		initRequestManager();
		
		// Initialize your progress dialog
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		// Initialize your UI Handler. This is for modifying your UI from a background thread
		mHandler = new Handler();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Set onClick & progress listeners (Progress listeners are completely optional)
		setListeners();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		// Remove listeners when the app isn't visible to prevent any issues
		removeListeners();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// Exit activity when user clicks the action bar
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void initViews()
	{
		btnSend = (Button) findViewById(R.id.btnAutoRequest);
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
		.emailSubject("Icon Request")
		.emailPrecontent("These apps are missing on my phone:\n\n")
		.createAppfilter(true)
		.createZip(true)
		.filterAutomatic(true)
		.build());
	}
	
	private void setListeners()
	{
		// Generic loading listener for displaying load progress
		mRequestManager.addAppLoadListener(this);
		
		// Request listener to let the user know what's happened
		mRequestManager.addSendRequestListener(this);
		
		// OnClick listener for button
		btnSend.setOnClickListener(this);
	}
	
	private void removeListeners()
	{
		mRequestManager.removeAppLoadListener(this);
		mRequestManager.removeSendRequestListener(this);
	}
	
	@Override
	public void onClick(View view)
	{
		// Android has a bug where a horizontal progress dialog won't show text if you change if after showing it. This is a workaround.
		progressDialog.setMessage("");
		
		// Workaround for an issue with the manager. Uncomment if you're not using listeners.
		// mRequestManager.setActivity(this);
		
		// Send the request! (if not already started)
		mRequestManager.sendAutomaticRequestAsync();
		
		// Show the progress dialog
		progressDialog.show();
	}
	
	@Override
	public void onAppPreload()
	{
		// Use your UI handler to run this task since this gets called from a background thread.
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setTitle("Please wait...");
				progressDialog.setMessage("Preparing to gather app data");
				progressDialog.setIndeterminate(true);
			}
		});
	}

	@Override
	public void onAppLoading(final int status, final int progress)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setTitle("Please wait...");
				
				switch(status) {
					case PkRequestManager.STATUS_LOADING_INSTALLED:
						progressDialog.setMessage("Gathering installed application info");
						break;
					case PkRequestManager.STATUS_LOADING_APPFILTER:
						progressDialog.setMessage("Reading appfilter");
						break;
					case PkRequestManager.STATUS_FILTERING:
						progressDialog.setMessage("Filtering applications");
						break;
				}
				
				progressDialog.setIndeterminate(false);
				progressDialog.setMax(PkRequestManager.MAX_PROGRESS);
				progressDialog.setSecondaryProgress(progress);
			}
		});
	}

	@Override
	public void onAppLoaded()
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				progressDialog.setTitle("Please wait...");
				progressDialog.setMessage("Loaded application info");
				progressDialog.setIndeterminate(true);
			}
		});
	}

	@Override
	public void onRequestStart(final boolean automatic)
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				progressDialog.setTitle("Please wait...");
				progressDialog.setMessage("Preparing to build request");
				progressDialog.setIndeterminate(true);
			}
		});
	}

	@Override
	public void onRequestBuild(final boolean automatic, final int progress)
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				progressDialog.setTitle("Please wait...");
				progressDialog.setMessage("Building icon request");
				progressDialog.setIndeterminate(false);
				progressDialog.setMax(PkRequestManager.MAX_PROGRESS);
				progressDialog.setProgress(progress);
			}
		});
	}

	@Override
	public void onRequestFinished(final boolean automatic, final boolean intentSuccessful, final Intent intent) 
	{
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				// Close progress dialog
				progressDialog.dismiss();
				
				// Start the intent manually if it was not successful
				if(!intentSuccessful) {
					startActivity(intent);
				}
			}
		});
	}
}