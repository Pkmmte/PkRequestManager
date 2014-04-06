PkRequestManager
================

A powerful, yet simple, tool which helps make it easy to load and sending application icon requests for Android

For more information, please see [the wiki][1]

Try out the sample application:

<a href="https://play.google.com/store/apps/details?id=com.pk">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" />
</a>

Download
--------

Download [the latest JAR][2] or grab via Maven:

// TODO


Usage & Integration
--------
Using the library is really simple, just look at the source code of the provided samples:
* [Basic][4]
* [Intermediate][5]
* [Advanced][6]
* [Automatic][7]

See the [Quick Start][3] guide for more information on how to achieve a simple integration.


**Important:** *This library requires the `WRITE_EXTERNAL_STORAGE` permission if you want to attach a .zip file containing requested app icons or a generated appfilter.xml!*
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

###Creating an instance
To be able to do anything, you first need to create an instance. 
I suggest you make it a global instance for more efficient use and shorter loading times.

Called like this :
```java
PkRequestManager mRequestManager = PkRequestManager.getInstance(this);
```

###Customize
This Manager class was made to be as flexible as possible. The only requirement is to set your email address(es). Everything else is set to default.
```java
mRequestManager.setSettings(new RequestSettings.Builder()
	.addEmailAddress("iconrequests@example.net")	// Email where the request will be sent to
	.addEmailAddress("example@gmail.com")	// You can specify multiple emails to send it to
	.emailSubject("[MyIconPack] App Icon Request")	// Email Subject
	.emailPrecontent("These apps are missing on my phone:\n\n")	// Text before the main app information
	.saveLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mytheme/.icon_request")	// Location to where the .zips and temporary files will be saved
	.appfilterName("appfilter.xml")	// Specify your appfilter.xml name if it's different from the standard. This will be used to filter out apps from the list.
	.compressFormat(PkRequestManager.PNG)	// Compression format for the attached app icons
	.appendInformation(true)	// Choose whether or not you'd like to receive information about the user's device such as OS version, manufacturer, model number, build, etc.
	.createAppfilter(true)	// True if you'd like to automatically generate an appfilter.xml for the requested apps
	.createZip(true)	// True if you want to receive app icons with the email
	.filterAutomatic(true)	// True if you want apps you support in your appfilter.xml to be filtered out from automatic requests
	.filterDefined(true)	// True if you don't want apps you already defined in your appfilter.xml to show up in the app list
	.byteBuffer(2048)	// Buffer size in bytes for writing to memory.
	.compressQuality(100)	// Compression quality for attached app icons
	.build());
```

[See here][8] for more.


###Loading
To load a list of apps, all you need to do is call the `loadApps()` method like so:
```java
mRequestManager.loadApps();
```
This will keep in mind your settings and filter out any apps if you have `filterDefined` enabled.
Loading can take a while if the user has hundreds or thousands of apps installed so make sure to call it from a background thread. You can also call the `Async` variant of the method.
```java
mRequestManager.loadAppsAsync();
```
This will load apps in a parallel background thread. It's safe to call this multiple times at once. The PkRequestManager only executes it if it's not already running.

You can also choose to load apps only if they're not already loaded with the following line of code:
```java
mRequestManager.loadAppsIfEmpty();
```


After all is loaded, you can retrieve the list of apps like this:
```java
mRequestManager.getApps();
```
You can also call `getInstalledApps()` to get a list of unfiltered apps or `getDefinedApps()` to get a String list of apps defined in your appfilter.


**Note:** I highly recommend loading apps asynchronously using the global instance from your MainActivity. That way the apps are already loaded when your request activity starts!


###Sending Request
Just like loading, sending a request only requires one line of code.
```java
mRequestManager.sendRequest();
```
Building up the request may take a while depending on the number of selected apps. I suggest you run this in a background thread or call `sendRequestAsync()` instead.
Sending a request with this method works only if you have selected apps on the ArrayList you get from the `getApps()` method.

You may also use the following to automatically load and send the request:
```java
mRequestManager.sendAutomaticRequest();
```
Be sure to set any listeners to check the progress on this. As usual, I recommend you use `sendAutomaticRequestAsync()` instead.

**Important:** Due to issues starting the sendRequest intent from a background thread, please use `mRequestManager.setActivity(this)` right before sending the request or manually start the intent from the `onRequestFinished` interface.


###Event Listeners
These are completely optional but very very useful. You can add event listeners to check for events.  They can be specially helpful when working with `Async` methods.
The following event listeners are available:
* AppLoadListener
* InstalledAppLoadListener
* AppFilterListener
* SendRequestListener

See more information about them in the [wiki][9].


###Debugging
You can enable debug log messages using this line:
```java
mRequestManager.setDebugging(true);
```
This shows significant events in your logcat and explains errors that may happen. (If any)
Please don't leave this enable during production. You don't want to spam your user's logcat, do you?


Developed By
--------

Pkmmte Xeleon - www.pkmmte.com

<a href="https://plus.google.com/102226057091361048952">
  <img alt="Follow me on Google+"
       src="http://data.pkmmte.com/temp/social_google_plus_logo.png" />
</a>
<a href="https://www.linkedin.com/pub/pkmmte-xeleon/7a/409/b4b/">
  <img alt="Follow me on LinkedIn"
       src="http://data.pkmmte.com/temp/social_linkedin_logo.png" />
</a>

License
--------

    The MIT License (MIT)
    
    Copyright (c) 2014 Pkmmte Xeleon
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.


 [1]: http://pkmmte.com//TODO
 [2]: https://github.com/Pkmmte/PkRequestManager/releases/download/v0.9/pkrequestmanager-0.9.jar
 [3]: https://github.com/Pkmmte/PkRequestManager/wiki
 [4]: https://github.com/Pkmmte/PkRequestManager/blob/master/PkRequestManager-Sample/src/com/pk/requestmanager/sample/BasicActivity.java
 [5]: https://github.com/Pkmmte/PkRequestManager/blob/master/PkRequestManager-Sample/src/com/pk/requestmanager/sample/IntermediateActivity.java
 [6]: https://github.com/Pkmmte/PkRequestManager/blob/master/PkRequestManager-Sample/src/com/pk/requestmanager/sample/AdvancedActivity.java
 [7]: https://github.com/Pkmmte/PkRequestManager/blob/master/PkRequestManager-Sample/src/com/pk/requestmanager/sample/AutomaticActivity.java
 [8]: http://pkmmte.com//TODO
 [9]: https://github.com/Pkmmte/PkRequestManager/wiki