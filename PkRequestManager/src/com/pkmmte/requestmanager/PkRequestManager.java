/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Pkmmte Xeleon
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.pkmmte.requestmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;


public class PkRequestManager extends Static
{
	//	TODO
	//	- Completely rewrite RequestActivity to use this new manager
	//	- Proper dialog for request options
	//	- Package this manager into a .jar file and separate open source project
	
	// General Public Constants
	public static final CompressFormat PNG = CompressFormat.PNG;
	public static final CompressFormat JPEG = CompressFormat.JPEG;
	public static final CompressFormat WEBP = CompressFormat.WEBP;
	public static final String PLAY_LINK_BASE = "https://play.google.com/store/apps/details?id=";
	public static final int MAX_PROGRESS = 100;
	public static final int STATUS_PRELOAD = 0;
	public static final int STATUS_LOADING_INSTALLED = 1;
	public static final int STATUS_LOADING_APPFILTER = 2;
	public static final int STATUS_FILTERING = 3;
	public static final int STATUS_LOADED = 4;
	
	// Keep a single instance throughout the app for simplicity
	private static PkRequestManager mInstance = null;
	
	// For issue tracking purposes
	private boolean debugEnabled;
	private static final String LOG_TAG = "RequestManager";
	
	// Custom request configuration data
	private RequestSettings mSettings;
	
	// Context is always useful for some reason.
	private Context mContext;
	
	// Keep an activity instance for those moments you really need it
	private Activity mActivity;
	
	// List of installed app info and already defined apps as well as filtered apps
	private List<AppInfo> mApps;
	private List<AppInfo> mInstalledApps;
	private List<String>  mDefinedApps;
	
	// Background threads
	private AsyncTask<Void, Void, Void> loadTask;
	private AsyncTask<Void, Void, Void> sendTask;
	private AsyncTask<Void, Void, Void> autoTask;
	
	// Listeners for various loading events
	private List<InstalledAppLoadListener> mInstalledAppLoadListeners;
	private List<AppFilterListener> mAppFilterListeners;
	private List<AppLoadListener> mAppLoadListeners;
	private List<SendRequestListener> mSendRequestListeners;
	
	/**
	 * Creates a global RequestManager instance.
	 * 
	 * @param context
	 */
	public static void createInstance(Context context)
	{
		if (mInstance == null)
			mInstance = new PkRequestManager(context.getApplicationContext());
	}
	
	/**
	 * Returns the global instance of this RequestManager.
	 * If you don't remember whether or not you already created 
	 * a previous instance, add the context as a parameter. 
	 * 
	 * @return
	 */
	public static PkRequestManager getInstance()
	{
		return mInstance;
	}
	
	/**
	 * Returns the global instance of this RequestManager.
	 * 
	 * @param context
	 * @return
	 */
	public static PkRequestManager getInstance(Context context)
	{
		if (mInstance == null)
			mInstance = new PkRequestManager(context.getApplicationContext());
		
		return mInstance;
	}
	
	/**
	 * Standard RequestManager constructor.
	 * 
	 * @param context
	 */
	public PkRequestManager(Context context)
	{
		this.debugEnabled = false;
		this.mSettings = new RequestSettings();
		this.mContext = context;
		this.mApps = new ArrayList<AppInfo>();
		this.mInstalledApps = new ArrayList<AppInfo>();
		this.mDefinedApps = new ArrayList<String>();
		this.mInstalledAppLoadListeners = new ArrayList<InstalledAppLoadListener>();
		this.mAppFilterListeners = new ArrayList<AppFilterListener>();
		this.mAppLoadListeners = new ArrayList<AppLoadListener>();
		this.mSendRequestListeners = new ArrayList<SendRequestListener>();
		this.initLoadingTask();
		this.initSendTask();
		this.initAutomaticTask();
	}
	
	/**
	 * Loads all apps installed on the device. This will also filter those 
	 * already in the appfilter if you have that enabled in settings. 
	 * All progress is reported via listeners.
	 * <p>
	 * This can take a while to process so make sure you're running it on 
	 * a background thread. You can also call the "loadAppsAsync()" method.
	 */
	public void loadApps()
	{
		// Loads already apps installed on device
		loadInstalledAppInfo();
		
		// If enabled, also load apps in the appfilter and... filter them
		if(mSettings.getFilterDefined()) {
			loadDefinedAppInfo();
			filterAppInfo();
		}
	}
	
	/**
	 * Exactly like "loadApps()" but runs in it's own parallel background thread.
	 * <p>
	 * Loads all apps installed on the device asynchronously. This will also filter 
	 * those already in the appfilter if you have that enabled in settings. 
	 * All progress is reported via listeners.
	 */
	public void loadAppsAsync()
	{
		loadAppsAsync(true);
	}
	
	/**
	 * Exactly like "loadApps()" but runs in it's own background thread.
	 * <p>
	 * Loads all apps installed on the device asynchronously. This will also filter 
	 * those already in the appfilter if you have that enabled in settings. 
	 * All progress is reported via listeners.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void loadAppsAsync(boolean parallel)
	{
		if(loadTask.getStatus() == AsyncTask.Status.PENDING) {
			// Execute task if it's ready to go!
			loadTask.executeOnExecutor(parallel ? AsyncTask.THREAD_POOL_EXECUTOR : AsyncTask.SERIAL_EXECUTOR);
		}
		else if(loadTask.getStatus() == AsyncTask.Status.RUNNING && debugEnabled) {
			// Don't execute if already running
			Log.d(LOG_TAG, "Task is already running...");
		}
		else if(loadTask.getStatus() == AsyncTask.Status.FINISHED) {
			// Okay, this is not supposed to happen. Reset and recall.
			if(debugEnabled)
				Log.d(LOG_TAG, "Uh oh, it appears the task has finished without being reset. Resetting task...");
			
			initLoadingTask();
			loadAppsAsync(parallel);
		}
	}
	
	/**
	 * Loads all apps if they have not yet been loaded. 
	 * This method keeps in mind your current settings.
	 */
	public void loadAppsIfEmpty()
	{
		if(!appsLoaded())
			loadApps();
	}
	

	/**
	 * Exactly like "loadAppsIfEmpty()" but runs in it's own background thread.
	 * <p>
	 * Loads all apps if they have not yet been loaded. 
	 * This method keeps in mind your current settings.
	 */
	public void loadAppsIfEmptyAsync()
	{
		if(!appsLoaded())
			loadAppsAsync();
	}
	
	/**
	 * Sends a request via email. This will return prematurely if no emails have 
	 * been set in your settings or if no apps were selected. Data sent depends on 
	 * your settings. All progress is reported via listeners.
	 * <p>
	 * Note: The send intent won't run immediately on some devices. As a hacky workaround, 
	 * make sure to "setActivity()" right before running this. The listener also passes the 
	 * send intent along with all the data attached to it. Manually launch the intent if you 
	 * prefer not to attach the activity.
	 */
	public void sendRequest()
	{
		sendRequest(false, false);
	}
	
	/**
	 * Sends a request via email. This will return prematurely if no emails have 
	 * been set in your settings or if no apps were selected. Data sent depends on 
	 * your settings. All progress is reported via listeners.
	 * <p>
	 * Do not pass any parameters unless you know what you're doing! 
	 * <p>
	 * Note: The send intent won't run immediately on some devices. As a hacky workaround, 
	 * make sure to "setActivity()" right before running this. The listener also passes the 
	 * send intent along with all the data attached to it. Manually launch the intent if you 
	 * prefer not to attach the activity.
	 * 
	 * @param selectAll
	 * @param automatic
	 */
	public void sendRequest(boolean selectAll, boolean automatic)
	{
		if(debugEnabled)
			Log.d(LOG_TAG, "Sending request...");
		
		// Retrieve proper apps
		final List<AppInfo> mAppList = automatic ? getAutomaticApps() : getApps();
		
		// Store used settings into local variables for slightly better performance
		final String[] emailAddresses = mSettings.getEmailAddresses();
		final String emailSubject = mSettings.getEmailSubject();
		final String emailPrecontent = mSettings.getEmailPrecontent();
		final String saveLoc = mSettings.getSaveLocation();
		final String saveLoc2 = mSettings.getSaveLocation2();
		final CompressFormat compressFormat = mSettings.getCompressFormat();
		final boolean appendInformation = mSettings.getAppendInformation();
		final boolean createAppfilter = mSettings.getCreateAppfilter();
		final boolean createZip = mSettings.getCreateZip();
		final int compressQuality = mSettings.getCompressQuality();
		
		// Check to see if data being set is valid.
		if(mAppList == null || mAppList.size() == 0) {
			if(debugEnabled)
				Log.d(LOG_TAG, "App List is either null or empty! Canceling send request...");
			return;
		}
		
		// Check to see if email is set, return if not. No point in doing work without anywhere to send it.
		if(emailAddresses == null) {
			if(debugEnabled)
				Log.d(LOG_TAG, "No proper email addresses were set! Canceling send request...");
			return;
		}
		
		// Loop through all listeners notifying them
        for(SendRequestListener mListener : mSendRequestListeners) {
        	mListener.onRequestStart(automatic);
        }
		
		// Create file instances based on save locations
		File saveLocation = new File(saveLoc);
		File saveLocation2 = new File(saveLoc2);
		
		// Delete previous zips and files, if any.
		deleteDirectory(saveLocation);
		deleteDirectory(saveLocation2);
		
		// Recreate directories
		saveLocation.mkdirs();
		saveLocation2.mkdirs();
		
		// Initialize email builder and xml builder (For generating optional appfilter.xml values)
		StringBuilder emailBuilder = new StringBuilder();
		StringBuilder xmlBuilder = new StringBuilder();
		
		// Append email precontent (if enabled)
		if(debugEnabled)
			Log.d(LOG_TAG, "Appending email precontent" + (appendInformation ? " and device information" : ""));
		
		emailBuilder.append(emailPrecontent);
		
		// Loop through all selected app packages
		if(debugEnabled)
			Log.d(LOG_TAG, "Generating data...");
		final int numApps = mAppList.size();
		int numSelected = 0;
		int progress = 0;
		for(AppInfo mAppInfo : mAppList) {
			// Loop through all listeners notifying them
			progress = (int) (mAppList.indexOf(mAppInfo) * MAX_PROGRESS) / numApps;
	        for(SendRequestListener mListener : mSendRequestListeners) {
	        	mListener.onRequestBuild(automatic, progress);
	        }
			
			// Only deal with selected apps
			if(selectAll || mAppInfo.isSelected()) {
				// Build email content
				emailBuilder.append("Name: " + mAppInfo.getName() + "\n");
				emailBuilder.append("Code: " + mAppInfo.getCode() + "\n");
				emailBuilder.append("Link: " + PLAY_LINK_BASE + mAppInfo.getCode().split("/")[0] + "\n");
				emailBuilder.append("\n\n");
				
				// Generate appfilter content (if enabled)
				if(createAppfilter) {
					xmlBuilder.append("<!-- " + mAppInfo.getName() + " -->\n");
					xmlBuilder.append("<item component=\"ComponentInfo{" + mAppInfo.getCode() + "}\" drawable=\"" + convertDrawableName(mAppInfo.getName()) + "\"/>" + "\n");			
				}
				
				// Save drawable if we're going to compress into a zip later
				if(createZip) {
					// Convert drawable into a bitmap before saving it
					Bitmap bitmap = drawableToBitmap(mAppInfo.getImage());
					
					// Write bitmap to storage as PNG
					try {
						String bmDir = saveLoc2 + "/" + mAppInfo.getCode().split("/")[0] + "_" + mAppInfo.getCode().split("/")[1]+ ".png";
						new File(bmDir).getParentFile().mkdirs();
						FileOutputStream fOut = new FileOutputStream(bmDir);
						bitmap.compress(compressFormat, compressQuality, fOut);
						fOut.flush();
						fOut.close();
					}
					catch (FileNotFoundException e) {
						if(debugEnabled) {
							Log.e(LOG_TAG, "FileNotFoundException! Make sure the file isn't open in another app and you have writing permissions in your manifest.");
							e.printStackTrace();
						}
					}
					catch (IOException e) {
						if(debugEnabled) {
							Log.e(LOG_TAG, "IOException! Make sure you have the appropriate permissions.");
							e.printStackTrace();
						}
					}
				}
				
				// Increase our counter for number of apps selected
				numSelected++;
			}
		}
		
		// Append device information (if enabled)
		if(appendInformation) {
			emailBuilder.append("\nOS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")");
			emailBuilder.append("\nOS API Level: " + Build.VERSION.SDK_INT);
			emailBuilder.append("\nDevice: " + Build.DEVICE);
			emailBuilder.append("\nManufacturer: " + Build.MANUFACTURER);
			emailBuilder.append("\nModel (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")");
		}
		
		// Return if no apps were selected
		if(numSelected == 0) {
			if(debugEnabled)
				Log.d(LOG_TAG, "No apps were selected. Can't send request data without any data!");
			return;
		}
		else if(debugEnabled)
				Log.d(LOG_TAG, "Successfully collected data for " + numSelected + " apps!");
		
		// Write appfilter.xml to storage, if enabled
		if(createAppfilter) {
			if(debugEnabled)
				Log.d(LOG_TAG, "Writing appfilter.xml...");
			try {
				new File(saveLoc2 + "/appfilter.xml").getParentFile().mkdirs();
				FileWriter fstream = new FileWriter(saveLoc2 + "/appfilter.xml");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(xmlBuilder.toString());
				out.close();
				
				if(debugEnabled)
					Log.d(LOG_TAG, "Succesfully wrote appfilter.xml to " + saveLoc2 + "/appfilter.xml!");
			}
			catch (Exception e) {
				if(debugEnabled) {
					Log.d(LOG_TAG, "Error writing generated appfilter.xml! Make sure you have writing permissions in your AndroidManifest.xml");
					e.printStackTrace();
				}
			}
		}
		
		// Create a zip file with the current date, if enabled
		String zipName = "mZip";
		if(createZip) {
			if(debugEnabled)
				Log.d(LOG_TAG, "Zipping files...");
			SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.getDefault());
			zipName = date.format(new Date());
			createZipFile(saveLoc2, true, saveLoc + "/" + zipName + ".zip");
		}
		
		// Initialize send intent with proper address, subject, and body values
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, emailAddresses);
		intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
		intent.putExtra(Intent.EXTRA_TEXT, emailBuilder.toString());
		
		// Attach zip/appfilter.xml if enabled
		if(createZip) {
			intent.setType("application/zip");
			final Uri uri = Uri.parse("file://" + saveLoc + "/" + zipName + ".zip");
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		else if(createAppfilter) {
			intent.setType("text/plain");
			final Uri uri = Uri.parse("file://" + saveLoc2 + "/appfilter.xml");
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		
		if(debugEnabled)
			Log.d(LOG_TAG, "Successfully collected data!");
		// Start the intent!
		boolean intentSuccessful;
		try {
			mActivity.startActivity(Intent.createChooser(intent, "Send Email"));
			intentSuccessful = true;
		}
		catch (ActivityNotFoundException localActivityNotFoundException) {
			if(debugEnabled)
				Log.d(LOG_TAG, "No email app has been found!");
			intentSuccessful = false;
		}
		catch (Exception e) {
			if(debugEnabled) {
				Log.d(LOG_TAG, "There was an error starting the intent!");
				e.printStackTrace();
			}
			intentSuccessful = false;
		}
		
		// Loop through all listeners notifying them
        for(SendRequestListener mListener : mSendRequestListeners) {
        	mListener.onRequestFinished(automatic, intentSuccessful, intent);
        }
	}
	
	/**
	 * Exactly like "sendRequest()" but runs in it's own parallel background thread.
	 * <p>
	 * Sends a request via email. This will return prematurely if no emails have 
	 * been set in your settings or if no apps were selected. Data sent depends on 
	 * your settings. All progress is reported via listeners.
	 * <p>
	 * Note: The send intent won't run immediately on some devices. As a hacky workaround, 
	 * make sure to "setActivity()" right before running this. The listener also passes the 
	 * send intent along with all the data attached to it. Manually launch the intent if you 
	 * prefer not to attach the activity.
	 */
	public void sendRequestAsync()
	{
		sendRequestAsync(true);
	}
	
	/**
	 * Exactly like "sendRequest()" but runs in it's own background thread.
	 * <p>
	 * Sends a request via email. This will return prematurely if no emails have 
	 * been set in your settings or if no apps were selected. Data sent depends on 
	 * your settings. All progress is reported via listeners.
	 * <p>
	 * Note: The send intent won't run immediately on some devices. As a hacky workaround, 
	 * make sure to "setActivity()" right before running this. The listener also passes the 
	 * send intent along with all the data attached to it. Manually launch the intent if you 
	 * prefer not to attach the activity.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void sendRequestAsync(boolean parallel)
	{
		if(sendTask.getStatus() == AsyncTask.Status.PENDING) {
			// Execute task if it's ready to go!
			sendTask.executeOnExecutor(parallel ? AsyncTask.THREAD_POOL_EXECUTOR : AsyncTask.SERIAL_EXECUTOR);
		}
		else if(sendTask.getStatus() == AsyncTask.Status.RUNNING && debugEnabled) {
			// Don't execute if already running
			Log.d(LOG_TAG, "Task is already running...");
		}
		else if(sendTask.getStatus() == AsyncTask.Status.FINISHED) {
			// Okay, this is not supposed to happen. Reset and recall.
			if(debugEnabled)
				Log.d(LOG_TAG, "Uh oh, it appears the task has finished without being reset. Resetting task...");
			
			initSendTask();
			sendRequestAsync(parallel);
		}
	}
	
	/**
	 * Automatically loads all app information (if not already) and sends it. 
	 * Data being sent depends on your settings.
	 * <p>
	 * Warning: Do not run this on the main UI thread or you might get an ANR 
	 * (Application Not Responding)! Use a background thread or call "sendAutomaticRequestAsync()" instead
	 */
	public void sendAutomaticRequest()
	{
		// Load list of apps, if not already loaded
		if(mInstalledApps.size() == 0 || (mSettings.getFilterAutomatic() && (mApps.size() == 0 || mDefinedApps.size() == 0))) {
			loadInstalledAppInfo();
			
			if(mSettings.getFilterAutomatic()) {
				loadDefinedAppInfo();
				filterAppInfo();
			}
		}
		
		// Send request, marking all apps as selected
		sendRequest(true, true);
	}
	
	/**
	 * Exactly like "sendAutomaticRequest()" but runs on it's own parallel background thread.
	 * <p>
	 * Automatically loads all app information (if not already) and sends it. 
	 * Data being sent depends on your settings.
	 */
	public void sendAutomaticRequestAsync()
	{
		sendAutomaticRequestAsync(true);
	}
	
	/**
	 * Exactly like "sendAutomaticRequest()" but runs on it's own background thread.
	 * <p>
	 * Automatically loads all app information (if not already) and sends it. 
	 * Data being sent depends on your settings.
	 * 
	 * @param parallel	Boolean indicating whether to run serially or in parallel. 
	 * 					True for parallel, False for serial.
	 */
	public void sendAutomaticRequestAsync(boolean parallel)
	{
		if(autoTask.getStatus() == AsyncTask.Status.PENDING) {
			// Execute task if it's ready to go!
			autoTask.executeOnExecutor(parallel ? AsyncTask.THREAD_POOL_EXECUTOR : AsyncTask.SERIAL_EXECUTOR);
		}
		else if(autoTask.getStatus() == AsyncTask.Status.RUNNING && debugEnabled) {
			// Don't execute if already running
			Log.d(LOG_TAG, "Task is already running...");
		}
		else if(autoTask.getStatus() == AsyncTask.Status.FINISHED) {
			// Okay, this is not supposed to happen. Reset and recall.
			if(debugEnabled)
				Log.d(LOG_TAG, "Uh oh, it appears the task has finished without being reset. Resetting task...");
			
			initAutomaticTask();
			sendAutomaticRequestAsync(parallel);
		}
	}
	
	/**
	 * Adds an InstalledAppLoadListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addInstalledAppLoadListener(InstalledAppLoadListener listener)
	{
		mInstalledAppLoadListeners.add(listener);
	}
	
	/**
	 * Removes an InstalledAppLoadListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeInstalledAppLoadListener(InstalledAppLoadListener listener)
	{
		mInstalledAppLoadListeners.remove(listener);
	}
	
	/**
	 * Removes all InstalledAppLoadListeners from this global instance.
	 */
	public void removeAllInstalledAppLoadListeners()
	{
		mInstalledAppLoadListeners.clear();
	}
	
	/**
	 * Adds an AppFilterListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addAppFilterListener(AppFilterListener listener)
	{
		mAppFilterListeners.add(listener);
	}
	
	/**
	 * Removes an AppFilterListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeAppFilterListener(AppFilterListener listener)
	{
		mAppFilterListeners.remove(listener);
	}
	
	/**
	 * Removes all AppFilterListeners from this global instance.
	 */
	public void removeAllAppFilterListeners()
	{
		mAppFilterListeners.clear();
	}
	
	/**
	 * Adds an AppLoadListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addAppLoadListener(AppLoadListener listener)
	{
		mAppLoadListeners.add(listener);
	}
	
	/**
	 * Removes an AppLoadListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeAppLoadListener(AppLoadListener listener)
	{
		mAppLoadListeners.remove(listener);
	}
	
	/**
	 * Removes all AppFilterListeners from this global instance.
	 */
	public void removeAllAppInfoLoadListeners()
	{
		mAppLoadListeners.clear();
	}
	
	/**
	 * Adds an SendRequestListener to this global instance.
	 * 
	 * @param listener
	 */
	public void addSendRequestListener(SendRequestListener listener)
	{
		mSendRequestListeners.add(listener);
	}
	
	/**
	 * Removes an SendRequestListener from this global instance.
	 * 
	 * @param listener
	 */
	public void removeSendRequestListener(SendRequestListener listener)
	{
		mSendRequestListeners.remove(listener);
	}
	
	/**
	 * Removes all SendRequestListeners from this global instance.
	 */
	public void removeAllSendRequestListeners()
	{
		mSendRequestListeners.clear();
	}
	
	/**
	 * Removes all listeners from this global instance.
	 */
	public void removeAllListeners()
	{
		mInstalledAppLoadListeners.clear();
		mAppFilterListeners.clear();
		mAppLoadListeners.clear();
		mSendRequestListeners.clear();
	}
	
	/**
	 * Deletes previous request data stored on the user's device. 
	 * This may include zips and files.
	 */
	public void deleteRequestData()
	{
		// Create file instances based on save locations
		File saveLocation = new File(mSettings.getSaveLocation());
		File saveLocation2 = new File(mSettings.getSaveLocation2());
		
		// Delete previous zips and files, if any.
		deleteDirectory(saveLocation);
		deleteDirectory(saveLocation2);
	}
	
	/**
	 * Attempts to cancel execution of this task. 
	 * This attempt will fail if the task has already completed, 
	 * already been cancelled, or could not be cancelled for some other reason. 
	 * If the task has already started, then the mayInterruptIfRunning parameter 
	 * determines whether the thread executing this task should be interrupted 
	 * in an attempt to stop the task.
	 * <p>
	 * This applies only if you called the <b>Async</b> variant.
	 * 
	 * @param mayInterruptIfRunning
	 */
	public void cancelLoadingTask(boolean mayInterruptIfRunning)
	{
		this.loadTask.cancel(mayInterruptIfRunning);
		this.initLoadingTask();
	}

	/**
	 * Attempts to cancel execution of this task. 
	 * This attempt will fail if the task has already completed, 
	 * already been cancelled, or could not be cancelled for some other reason. 
	 * If the task has already started, then the mayInterruptIfRunning parameter 
	 * determines whether the thread executing this task should be interrupted 
	 * in an attempt to stop the task.
	 * <p>
	 * This applies only if you called the <b>Async</b> variant.
	 * 
	 * @param mayInterruptIfRunning
	 */
	public void cancelSendTask(boolean mayInterruptIfRunning)
	{
		this.sendTask.cancel(mayInterruptIfRunning);
		this.initSendTask();
	}

	/**
	 * Attempts to cancel execution of this task. 
	 * This attempt will fail if the task has already completed, 
	 * already been cancelled, or could not be cancelled for some other reason. 
	 * If the task has already started, then the mayInterruptIfRunning parameter 
	 * determines whether the thread executing this task should be interrupted 
	 * in an attempt to stop the task.
	 * <p>
	 * This applies only if you called the <b>Async</b> variant.
	 * 
	 * @param mayInterruptIfRunning
	 */
	public void cancelAutomaticTask(boolean mayInterruptIfRunning)
	{
		this.autoTask.cancel(mayInterruptIfRunning);
		this.initAutomaticTask();
	}
	
	/**
	 * Returns a boolean letting you know if apps have been loaded. 
	 * This keeps in mind the settings you have set.
	 * 
	 * @return
	 */
	public boolean appsLoaded()
	{
		return !(mInstalledApps.size() == 0 || (mSettings.getFilterDefined() && (mApps.size() == 0 || mDefinedApps.size() == 0)));
	}
	
	/**
	 * Returns an ArrayList of AppInfo objects. 
	 * If apps have no been filtered, it'll return all installed apps. 
	 * Otherwise, this will return a list of filtered apps.
	 * 
	 * @return
	 */
	public List<AppInfo> getApps()
	{
		if(mApps.size() == 0 || !mSettings.getFilterDefined())
			return this.mInstalledApps;
		else
			return this.mApps;
	}
	
	/**
	 * Returns an ArrayList of AppInfo objects. 
	 * If apps have no been filtered, it'll return all installed apps. 
	 * Otherwise, this will return a list of filtered apps.
	 * <p>
	 * Unless you're implementing your own automatic method, 
	 * call "getApps()" instead.
	 * @return
	 */
	public List<AppInfo> getAutomaticApps()
	{
		if(mApps.size() == 0 || !mSettings.getFilterAutomatic())
			return this.mInstalledApps;
		else
			return this.mApps;
	}
	
	/**
	 * Returns an ArrayList of AppInfo objects. 
	 * All apps in this list are those installed on the device.
	 * 
	 * @return
	 */
	public List<AppInfo> getInstalledApps()
	{
		return this.mInstalledApps;
	}
	
	/**
	 * Returns a String ArrayList containing component info 
	 * gathered from the appfilter. This is used to filter 
	 * out already-supported apps but you might find it useful 
	 * for something else.
	 * 
	 * @return
	 */
	public List<String> getDefinedApps()
	{
		return this.mDefinedApps;
	}
	
	/**
	 * Returns an integer count with the total number of 
	 * apps currently selected. This keeps in mind your settings 
	 * and will not work if you don't have the proper reference.
	 * 
	 * @return int
	 */
	public int getNumSelected()
	{
		int count = 0;
		List<AppInfo> mAppList = getApps();
		for(AppInfo mApp : mAppList) {
			if(mApp.isSelected())
				count++;
		}
		
		return count;
	}
	
	/**
	 * Returns a RequestSettings object with all values set.
	 * 
	 * @return
	 */
	public RequestSettings getSettings()
	{
		return this.mSettings;
	}
	
	/**
	 * Applies new settings using your own custom RequestSettings object.
	 * 
	 * @param settings
	 */
	public void setSettings(RequestSettings settings)
	{
		this.mSettings = settings;
	}
	
	/**
	 * References your current activity. This is only used as a hacky workaround 
	 * for when the send intent doesn't automatically open on some devices. 
	 * <p>
	 * You can also use a OnRequestSendListener to catch the send intent and 
	 * start it manually instead of using this workaround.
	 * 
	 * @param activity
	 */
	public void setActivity(Activity activity)
	{
		this.mActivity = activity;
	}
	
	/**
	 * Set the debug status for this manager.
	 * If true, it will periodically print logs of current 
	 * progress so you can see what's going on.
	 * <p>
	 * I suggest you disable this during production as it
	 * will consume unnecessary processing power. Besides,
	 * you don't want to spam your users' logs.
	 * 
	 * @param debug
	 */
	public void setDebugging(boolean debug)
	{
		this.debugEnabled = debug;
	}
	
	/**
	 * Loads and parses the appfilter.xml for already-
	 * defined app info. This helps filter out apps already 
	 * supported in your theme.
	 */
	private void loadDefinedAppInfo()
	{
		try{
			// Initialize XmlPullParser and set the appfilter.xml (from assets) as the input
			XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
			XmlPullParser mParser = xmlFactoryObject.newPullParser();
			AssetManager assManager = mContext.getAssets();
			InputStream inputStream = assManager.open(mSettings.getAppfilterName());
			mParser.setInput(inputStream, null);
			
			// Loop through all listeners notifying them
	        for(AppFilterListener mListener : mAppFilterListeners) {
	        	mListener.onAppPrefilter();
	        }
			
			// Read number of elements for progress
			int numElements = 0;
			try {
				InputStream testStream = assManager.open(mSettings.getAppfilterName());
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(testStream);
				NodeList list = doc.getElementsByTagName("item");
				numElements = list.getLength();
			}
			catch (Exception e) {
				if(debugEnabled) {
					Log.d(LOG_TAG, "Error finding the number of appfilter elements!");
					e.printStackTrace();
				}
			}
			
			if(debugEnabled)
				Log.d(LOG_TAG, "" + numElements + " items found in appfilter.");
			
			// Clear defined apps before adding more
			mDefinedApps.clear();
			
			// Keep track of the event type. Also, reuse one string for memory purposes
			String mAppCode = null;
			int eventType = mParser.getEventType();
			
			// Loop through ALL the appfilter
			int count = 0;
			int progress = 0;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG)
				{
					String elementName= mParser.getName();
					if (elementName.equals("item")) {
						count++;
						progress = (int) (count * MAX_PROGRESS) / numElements;
						// Loop through all listeners notifying them
						for(AppLoadListener mListener : mAppLoadListeners) {
				        	mListener.onAppLoading(STATUS_LOADING_APPFILTER, (int) ((MAX_PROGRESS / 3) + (progress / 3)));
				        }
				        for(AppFilterListener mListener : mAppFilterListeners) {
				        	mListener.onAppFiltering(progress, 0);
				        }
						try	{
							// Read package and activity name
							mAppCode = mParser.getAttributeValue(null, "component");
							mAppCode = mAppCode.substring(14, mAppCode.length() - 1);
							
							// Add new info to our ArrayList and reset the object. Log commented out to reduce logcat spam.
							mDefinedApps.add(mAppCode);
							//if(debugEnabled)
							//	Log.d(LOG_TAG, "Added appfilter app:\n" + mAppCode);
							mAppCode = null;
							
						}
						catch(Exception e) {
							// Log and ignore whatever individual error was found. 
							if(debugEnabled) {
								Log.e(LOG_TAG, "Error adding parsed appfilter item!");
								e.printStackTrace();
							}
						}
					}
				}
				eventType = mParser.next();
			}
		}
		catch(IOException eIO) {
			if(debugEnabled)
				Log.e(LOG_TAG, "Unable to read appfilter.xml! Are you sure you have in it your assets folder?");
		}
		catch(XmlPullParserException eXPPE) {
			if(debugEnabled) {
				Log.e(LOG_TAG, "Unknown XmlPullParserException! This may be caused by a malformed appfilter.xml. Make sure it's fine.");
				eXPPE.printStackTrace();
			}
		}
	}
	
	/**
	 * Loads locally installed apps. These are the ones that will 
	 * eventually be requested.
	 */
	private void loadInstalledAppInfo()
	{
		// Create package manager and sort it
		PackageManager pm = mContext.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));
		
		// Loop through all listeners notifying them
		for(AppLoadListener mListener : mAppLoadListeners) {
        	mListener.onAppLoading(STATUS_PRELOAD, 0);
        }
        for(AppLoadListener mListener : mAppLoadListeners) {
        	mListener.onAppPreload();
        }
		
		// Loop through all listeners notifying them
        for(InstalledAppLoadListener mListener : mInstalledAppLoadListeners) {
        	mListener.onInstalledAppsPreload();
        }
		
		// Clear installed apps before reloading them
		mInstalledApps.clear();
		
		// Reuse app info object for memory purposes. Same goes for other string values.
		AppInfo mAppInfo = null;
		String launchIntent = null;
		String appCode = null;

		// Loops through the package manager to find all installed apps
		final boolean filterDefined = mSettings.getFilterDefined();
		final int numPackages = packages.size();
		int progress = 0;
		for (ApplicationInfo packageInfo : packages) {
			// Examine only valid packages
			if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null
					&& !pm.getLaunchIntentForPackage(packageInfo.packageName)
							.equals("")) {
				// Loop through all listeners notifying them
				progress = (int) (packages.indexOf(packageInfo) * MAX_PROGRESS) / numPackages;
		        for(AppLoadListener mListener : mAppLoadListeners) {
		        	mListener.onAppLoading(STATUS_LOADING_INSTALLED, filterDefined ? (int)(progress / 3) : progress);
		        }
		        for(InstalledAppLoadListener mListener : mInstalledAppLoadListeners) {
		        	mListener.onInstalledAppsLoading(progress);
		        }
				
				// Initialize reusable AppInfo object with default values
				mAppInfo = new AppInfo();
				
				// Set app information from the package manager
				mAppInfo.setImage(pm.getApplicationIcon(packageInfo));
				mAppInfo.setName(pm.getApplicationLabel(packageInfo).toString());
				
				// Get app launch intent and trim it
				launchIntent = pm.getLaunchIntentForPackage(packageInfo.packageName).toString().split("cmp=")[1];
				appCode = launchIntent.substring(0, launchIntent.length() - 1);
				if (appCode.split("/")[1].startsWith("."))
					appCode = appCode.split("/")[0] + "/"
							+ appCode.split("/")[0] + appCode.split("/")[1];
				appCode = appCode.trim();
				
				// Set the trimmed app code
				mAppInfo.setCode(appCode);

				// Add new info to our ArrayList. Log commented out to reduce logcat spam.
				mInstalledApps.add(mAppInfo);
				//if(debugEnabled)
				//	Log.d(LOG_TAG, "Added installed app:\n" + mAppInfo.toString());

				// Nullify objects for memory
				mAppInfo = null;
				appCode = null;
			}
		}
		
		// Loop through all listeners notifying them
        for(InstalledAppLoadListener mListener : mInstalledAppLoadListeners) {
        	mListener.onInstalledAppsLoaded();
        }
        if(!filterDefined) {
        	for(AppLoadListener mListener : mAppLoadListeners) {
            	mListener.onAppLoading(STATUS_LOADED, MAX_PROGRESS);
            }
        }
	}
	
	/**
	 * Filters apps based on your appfilter components. 
	 * All filtered apps are copied over to the "mApps" ArrayList.
	 */
	private void filterAppInfo()
	{
		// Clear list of filtered apps before adding more
		mApps.clear();
		
		// Loop through all installed apps
		final int numApps = mInstalledApps.size();
		int progress = 0;
		for(AppInfo mAppInfo : mInstalledApps) {
			// Loop through all listeners notifying them
			progress = (int) (mInstalledApps.indexOf(mAppInfo) * MAX_PROGRESS) / numApps;
			for(AppLoadListener mListener : mAppLoadListeners) {
	        	mListener.onAppLoading(STATUS_FILTERING, (int) ((MAX_PROGRESS / 3 * 2) + (progress / 3)));
	        }
			for(AppFilterListener mListener : mAppFilterListeners) {
	        	mListener.onAppFiltering(MAX_PROGRESS, progress);
	        }
			
			// Check if app is already defined and filter it accordingly
			if(!mDefinedApps.contains(mAppInfo.getCode())) {
				mApps.add(mAppInfo);
				if(debugEnabled)
					Log.d(LOG_TAG, "Filtered IN : " + mAppInfo.getName());
			}
			else if(debugEnabled)
				Log.d(LOG_TAG, "Filtered OUT : " + mAppInfo.getName());
		}
		
		if(debugEnabled)
			Log.d(LOG_TAG, "Finished filtering apps!");
		
		// Loop through all listeners notifying them
		for(AppLoadListener mListener : mAppLoadListeners) {
        	mListener.onAppLoading(STATUS_LOADED, MAX_PROGRESS);
        }
		for(AppLoadListener mListener : mAppLoadListeners) {
        	mListener.onAppLoaded();
        }
		for(AppFilterListener mListener : mAppFilterListeners) {
        	mListener.onAppFiltered();
        }
	}
	
	/**
	 * Created a .zip file... nuff said.
	 * 
	 * @param path
	 * @param keepDirectoryStructure
	 * @param outputFile
	 * @return
	 */
	private boolean createZipFile(String path, boolean keepDirectoryStructure, String outputFile)
	{
		File f = new File(path);
		if (!f.canRead() || !f.canWrite()) {
			if(debugEnabled)
				Log.d(LOG_TAG, path + " cannot be compressed due to file permissions!");
			return false;
		}
		try {
			ZipOutputStream zip_out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile), mSettings.getByteBuffer()));
			if (keepDirectoryStructure) {
				zipFile(path, zip_out, "");
			}
			else {
				final File files[] = f.listFiles();
				for (final File file : files) {
					zipFolder(file, zip_out);
				}
			}
			zip_out.close();
		} 
		catch (FileNotFoundException e) {
			if(debugEnabled)
				Log.e("File not found: ", e.getMessage());
			
			return false;
		} 
		catch (IOException e) {
			if(debugEnabled)
				Log.e("IOException: ", e.getMessage());
			
			return false;
		}
		return true;
	}
	
	/**
	 * Zips a file. Does this really need any explanation?
	 * 
	 * @param path
	 * @param out
	 * @param relPath
	 * @throws IOException
	 */
	private void zipFile(String path, ZipOutputStream out, String relPath) throws IOException 
	{
		File file = new File(path);
		if(!file.exists()) {
			if(debugEnabled)
				Log.d(LOG_TAG, file.getName() + " does NOT exist!");
			return;
		}
		
		final byte[] buffer = new byte[mSettings.getByteBuffer()];
		final String[] files = file.list();
		if(file.isFile()) {   
			FileInputStream in = new FileInputStream(file.getAbsolutePath()); 

			try {
				out.putNextEntry(new ZipEntry(relPath + file.getName()));
				int len; 
				while ((len = in.read(buffer)) > 0)  { 
					out.write(buffer, 0, len); 
				}
				
				out.closeEntry(); 
				in.close();
			}
			catch (ZipException zipE) {
				if(debugEnabled)
					Log.e(LOG_TAG, zipE.getMessage());
			}
			finally {
				if(out != null)
					out.closeEntry(); 
				if(in != null)
					in.close();
			}
		}
		else if (files.length > 0) // non-empty folder
		{
			for (int i = 0, length = files.length; i < length; ++i)
			{
				zipFile(path + "/" + files[i], out, relPath + file.getName() + "/");
			}
		}
	}
	
	/**
	 * Zips a folder...
	 * 
	 * @param file
	 * @param zout
	 * @throws IOException
	 */
	private void zipFolder(File file, ZipOutputStream zout) throws IOException
	{
		byte[] data = new byte[mSettings.getByteBuffer()];
		if(file.isFile()) {
			ZipEntry entry = new ZipEntry(file.getName());
			zout.putNextEntry(entry);
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(file));
			int read;
			
			while((read = instream.read(data, 0, mSettings.getByteBuffer())) != -1) {
				zout.write(data, 0, read);
			}
			
			zout.closeEntry();
			instream.close();
		}
		else if (file.isDirectory()) {
			String[] list = file.list();
			int len = list.length;
			
			for(int i = 0; i < len; i++)
				zipFolder(new File(file.getPath() +"/"+ list[i]), zout);
		}
	}
	
	/**
	 * Deletes files one by one until the entire 
	 * directory is deleted.
	 * 
	 * @param path
	 * @return
	 */
	private boolean deleteDirectory(File path)
	{
		if(path.exists()) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		
		return(path.delete());
	}
	
	/**
	 * Converts a Drawable object into a Bitmap object.
	 * 
	 * @param drawable
	 * @return
	 */
	private Bitmap drawableToBitmap(Drawable drawable)
	{
		if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	
	/**
	 * Formats drawable names for your generated appfilter.xml
	 * 
	 * @param appName
	 * @return
	 */
	private String convertDrawableName(String appName)
	{
		return (appName
				.replaceAll("[^a-zA-Z0-9\\p{Z}]", "")	// Remove all special characters and symbols
				.replaceFirst("^[0-9]+(?!$)", "")		// Remove all leading numbers unless they're all numbers
				.toLowerCase(Locale.US)					// Turn everything into lower case
				.replaceAll("\\p{Z}", "_"));			// Replace all kinds of spaces with underscores
	}
	
	/**
	 * Initializes our loading thread.
	 */
	private void initLoadingTask()
	{
		this.loadTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				loadApps();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void p)
			{
				initLoadingTask();
			}
		};
	}
	
	/**
	 * Initializes our send request thread.
	 */
	private void initSendTask()
	{
		this.sendTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				sendRequest();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void p)
			{
				initSendTask();
			}
		};
	}
	
	/**
	 * Initializes our send request thread.
	 */
	private void initAutomaticTask()
	{
		this.autoTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				sendAutomaticRequest();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void p)
			{
				initAutomaticTask();
			}
		};
	}
}
