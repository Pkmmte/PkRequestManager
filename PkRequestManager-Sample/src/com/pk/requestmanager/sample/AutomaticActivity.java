package com.pk.requestmanager.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pk.requestmanager.AppLoadListener;
import com.pk.requestmanager.PkRequestManager;
import com.pk.requestmanager.RequestSettings;
import com.pk.requestmanager.SendRequestListener;

public class AutomaticActivity extends Activity implements AppLoadListener, SendRequestListener
{
	// Request Manager
	private PkRequestManager mRequestManager;
	
	// UI Handler
	private Handler mHandler;
	
	// Views
	private TextView txtProgress;
	private ProgressBar progressBar;
	
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
		
		// Initialize your UI Handler. This is for modifying your UI from a background thread
		mHandler = new Handler();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// Add listeners to let the user know what's going on
		setListeners();
		
		// Send an automatic request.
		mRequestManager.setActivity(this);
		mRequestManager.sendAutomaticRequestAsync();
		
		// Easy, right? This line of code is all you need.
		// The rest is just extra to let the user know what's happening.
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
		txtProgress = (TextView) findViewById(R.id.txtProgress);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
	}
	
	private void removeListeners()
	{
		mRequestManager.removeAppLoadListener(this);
		mRequestManager.removeSendRequestListener(this);
	}

	@Override
	public void onAppPreload()
	{
		// Use your UI handler to run this task since this gets called from a background thread.
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				// TODO
			}
		});
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
}