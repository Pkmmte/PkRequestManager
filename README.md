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

See the Quick Start guides for more information on how to achieve a simple integration:

* [Quick Start](https://github.com/Pkmmte/PkRequestManager/wiki)
* [Quick Start: Loading Apps](https://github.com/Pkmmte/PkRequestManager/wiki)
* [Quick Start: Sending Request](https://github.com/Pkmmte/PkRequestManager/wiki)
* [Quick Start: Listeners](https://github.com/Pkmmte/PkRequestManager/wiki)

*This library requires the `WRITE_EXTERNAL_STORAGE` permission if you want to attach a .zip file containing requested app icons or a generated appfilter.xml!*
```xml
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**Creating an instance_**
To be able to do anything, you first need to create an instance. 
I suggest you make it a global instance for more efficient use and shorter loading times.

Called like this :
```java
    PkRequestManager mRequestManager = PkRequestManager.getInstance(this);
```

**Customize**
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

[See here][3] for more.

**Loading apps_**



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
 [2]: http://pkmmte.com//TODO