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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

/** Custom settings data. I find this much neater  than having it all stored in the Manager itself. */
public class RequestSettings
{
	private String[] emailAddresses;
	private String emailSubject;
	private String emailPrecontent;
	private String saveLocation;
	private String saveLocation2;
	private String appfilterName;
	private CompressFormat compressFormat;
	private boolean appendInformation;
	private boolean createAppfilter;
	private boolean createZip;
	private boolean filterAutomatic;
	private boolean filterDefined;
	private int byteBuffer;
	private int compressQuality;
	
	public RequestSettings() {
		this.emailAddresses = null;
		this.emailSubject = "No Subject";
		this.emailPrecontent = "";
		this.saveLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.icon_request";
		this.saveLocation2 = this.saveLocation + "/files";
		this.appfilterName = "appfilter.xml";
		this.compressFormat = CompressFormat.PNG;
		this.appendInformation = true;
		this.createAppfilter = true;
		this.createZip = true;
		this.filterAutomatic = true;
		this.filterDefined = true;
		this.byteBuffer = 2048;
		this.compressQuality = 100;
	}
	
	private RequestSettings(Builder builder) {
		this.emailAddresses = builder.emailAddresses.toArray(new String[builder.emailAddresses.size()]);
		this.emailSubject = builder.emailSubject;
		this.emailPrecontent = builder.emailPrecontent;
		this.saveLocation = builder.saveLocation;
		this.saveLocation2 = builder.saveLocation2;
		this.appfilterName = builder.appfilterName;
		this.compressFormat = builder.compressFormat;
		this.appendInformation = builder.appendInformation;
		this.createAppfilter = builder.createAppfilter;
		this.createZip = builder.createZip;
		this.filterAutomatic = builder.filterAutomatic;
		this.filterDefined = builder.filterDefined;
		this.byteBuffer = builder.byteBuffer;
		this.compressQuality = builder.compressQuality;
	}
	
	public void setEmailAddresses(String[] emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
	
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	
	public void setEmailPrecontent(String emailPrecontent) {
		this.emailPrecontent = emailPrecontent;
	}
	
	public void setSaveLocation(String saveLocation) {
		this.saveLocation = saveLocation;
		this.saveLocation2 = this.saveLocation + "/files";
	}
	
	public void setAppfilterName(String appfilterName) {
		this.appfilterName = appfilterName;
	}
	
	public void setCompressFormat(CompressFormat compressFormat) {
		this.compressFormat = compressFormat;
	}
	
	public void setAppendInformation(boolean appendInformation) {
		this.appendInformation = appendInformation;
	}
	
	public void setCreateAppsfilter(boolean createAppfilter) {
		this.createAppfilter = createAppfilter;
	}
	
	public void setCreateZip(boolean createZip) {
		this.createZip = createZip;
	}
	
	public void setFilterAutomatic(boolean filterAutomatic) {
		this.filterAutomatic = filterAutomatic;
	}
	
	public void setFilterDefined(boolean filterDefined) {
		this.filterDefined = filterDefined;
	}
	
	public void setByteBuffer(int byteBuffer) {
		this.byteBuffer = byteBuffer;
	}
	
	public void setCompressQuality(int compressQuality) {
		this.compressQuality = compressQuality;
	}
	
	public String[] getEmailAddresses() {
		return this.emailAddresses;
	}
	
	public String getEmailSubject() {
		return this.emailSubject;
	}
	
	public String getEmailPrecontent() {
		return this.emailPrecontent;
	}
	
	public String getSaveLocation() {
		return this.saveLocation;
	}
	
	public String getSaveLocation2() {
		return this.saveLocation2;
	}
	
	public String getAppfilterName() {
		return this.appfilterName;
	}
	
	public CompressFormat getCompressFormat() {
		return this.compressFormat;
	}
	
	public boolean getAppendInformation() {
		return this.appendInformation;
	}
	
	public boolean getCreateAppfilter() {
		return this.createAppfilter;
	}
	
	public boolean getCreateZip() {
		return this.createZip;
	}
	
	public boolean getFilterAutomatic() {
		return this.filterAutomatic;
	}
	
	public boolean getFilterDefined() {
		return this.filterDefined;
	}
	
	public int getByteBuffer() {
		return this.byteBuffer;
	}
	
	public int getCompressQuality() {
		return this.compressQuality;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Email Addresses: " + (this.emailAddresses == null ? "null" : this.emailAddresses.toString()) + "\n");
		builder.append("Email Subject: " + this.emailSubject + "\n");
		builder.append("Email Precontent: " + this.emailPrecontent + "\n");
		builder.append("Save Location: " + this.saveLocation + "\n");
		builder.append("Save Location 2: " + this.saveLocation2 + "\n");
		builder.append("Appfilter Name: " + this.appfilterName + "\n");
		builder.append("Compress Format: " + this.compressFormat.toString() + "\n");
		builder.append("Append Information: " + this.appendInformation + "\n");
		builder.append("Create Appfilter: " + this.createAppfilter + "\n");
		builder.append("Create Zip: " + this.createZip + "\n");
		builder.append("Filter Automatic: " + this.filterAutomatic + "\n");
		builder.append("Filter Defined: " + this.filterDefined + "\n");
		builder.append("Byte Buffer: " + this.byteBuffer + "\n");
		builder.append("Compress Quality: " + this.compressQuality + "\n");
		
		return builder.toString();
	}
	
	public static class Builder
	{
		private List<String> emailAddresses;
		private String emailSubject;
		private String emailPrecontent;
		private String saveLocation;
		private String saveLocation2;
		private String appfilterName;
		private CompressFormat compressFormat;
		private boolean appendInformation;
		private boolean createAppfilter;
		private boolean createZip;
		private boolean filterAutomatic;
		private boolean filterDefined;
		private int byteBuffer;
		private int compressQuality;
		
		/**
		 * Creates a RequestSettings Builder object for easily 
		 * assigning custom settings.
		 */
		public Builder() {
			this.emailAddresses = new ArrayList<String>();
			this.emailSubject = "No Subject";
			this.emailPrecontent = "";
			this.saveLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.icon_request";
			this.saveLocation2 = this.saveLocation + "/files";
			this.appfilterName = "appfilter.xml";
			this.compressFormat = CompressFormat.PNG;
			this.appendInformation = true;
			this.createAppfilter = true;
			this.createZip = true;
			this.filterAutomatic = true;
			this.filterDefined = true;
			this.byteBuffer = 2048;
			this.compressQuality = 100;
		}
		
		/**
		 * This is where your icon request will be sent to. You can add 
		 * multiple emails by repeating this call.
		 * <p>
		 * <b>Default:</b> <code>null</code>
		 * 
		 * @param emailAddress
		 * @return
		 */
		public Builder addEmailAddress(String emailAddress) {
			this.emailAddresses.add(emailAddress);
			return this;
		}
		
		/**
		 * Subject for the email request.
		 * <p>
		 * <b>Default:</b> <code>"No Subject"</code>
		 * 
		 * @param emailSubject
		 * @return
		 */
		public Builder emailSubject(String emailSubject) {
			this.emailSubject = emailSubject;
			return this;
		}
		
		/**
		 * Content text ahead of the main email. Set this to whatever you want... 
		 * or nothing at all. This may be instructions for your user to hit "send" 
		 * or simply text letting you know what this is.
		 * <p>
		 * <b>Default:</b> <code>""</code>
		 * 
		 * @param emailPrecontent
		 * @return
		 */
		public Builder emailPrecontent(String emailPrecontent) {
			this.emailPrecontent = emailPrecontent;
			return this;
		}
		
		/**
		 * The location where sent zips and temporary files are located. 
		 * These get deleted every time a new request is sent but you can 
		 * also manually delete them using the <b>deleteRequestData<b> method.
		 * <p>
		 * <b>Default:</b> <code>Environment.getExternalStorageDirectory().getAbsolutePath() + "/.icon_request"</code>
		 * 
		 * @param saveLocation
		 * @return
		 */
		public Builder saveLocation(String saveLocation) {
			this.saveLocation = saveLocation;
			this.saveLocation2 = this.saveLocation + "/files";
			return this;
		}
		
		/**
		 * Specify exactly how your appfilter file is called. You normally 
		 * don't need to touch this setting.
		 * <p>
		 * <b>Default:</b> <code>"appfilter.xml"</code>
		 * 
		 * @param appfilterName
		 * @return
		 */
		public Builder appfilterName(String appfilterName) {
			this.appfilterName = appfilterName;
			return this;
		}
		
		/**
		 * Specify a format to compress all attached app icons. Formats 
		 * compatible:
		 * <ul>
		 * <li><code>PkRequestManager.PNG</code>
		 * <li><code>PkRequestManager.JPEG</code>
		 * <li><code>PkRequestManager.WEBP</code>
		 * </ul>
		 * <p>
		 * <b>Default:</b> <code>PkRequestManager.PNG</code>
		 * 
		 * @param compressFormat
		 * @return
		 */
		public Builder compressFormat(CompressFormat compressFormat) {
			this.compressFormat = compressFormat;
			return this;
		}
		
		/**
		 * Attach user's device information near the beginning of 
		 * the request email. This may include things such as OS version, 
		 * model number, manufacturer, build, etc.
		 * <p>
		 * <b>Default:</b> <code>true</code>
		 * 
		 * @param appendInformation
		 * @return
		 */
		public Builder appendInformation(boolean appendInformation) {
			this.appendInformation = appendInformation;
			return this;
		}
		
		/**
		 * Automatically generate appfilter.xml values for each app. 
		 * This is later attached in the request email.
		 * <p>
		 * <b>Default:</b> <code>true</code>
		 * 
		 * @param createAppfilter
		 * @return
		 */
		public Builder createAppfilter(boolean createAppfilter) {
			this.createAppfilter = createAppfilter;
			return this;
		}
		
		/**
		 * Automatically create a .zip folder containing all requested 
		 * app icons. This may be useful but can take a while to create and attach.
		 * <p>
		 * <b>Default:</b> <code>true</code>
		 * 
		 * @param createZip
		 * @return
		 */
		public Builder createZip(boolean createZip) {
			this.createZip = createZip;
			return this;
		}
		
		/**
		 * Filter apps already defined in your appfilter. This setting only 
		 * applies when sending an automatic request.
		 * <p>
		 * <b>Default:</b> <code>true</code>
		 * 
		 * @param filterAutomatic
		 * @return
		 */
		public Builder filterAutomatic(boolean filterAutomatic) {
			this.filterAutomatic = filterAutomatic;
			return this;
		}
		
		/**
		 * Filter apps already defined in your appfilter.
		 * <p>
		 * <b>Default:</b> <code>true</code>
		 * 
		 * @param filterDefined
		 * @return
		 */
		public Builder filterDefined(boolean filterDefined) {
			this.filterDefined = filterDefined;
			return this;
		}
		
		/**
		 * Buffer size in bytes. This is for writing to memory.
		 * <p>
		 * <b>Default:</b> <code>2048</code>
		 * 
		 * @param byteBuffer
		 * @return
		 */
		public Builder byteBuffer(int byteBuffer) {
			this.byteBuffer = byteBuffer;
			return this;
		}
		
		/**
		 * Compress quality (0 to 100) for attached app icons. Higher 
		 * quality takes up more space. This setting is ignored for 
		 * lossless image formats such as PNG.
		 * <p>
		 * <b>Default:</b> <code>100</code>
		 * 
		 * @param compressQuality
		 * @return
		 */
		public Builder compressQuality(int compressQuality) {
			this.compressQuality = compressQuality;
			return this;
		}
		
		/**
		 * Build a RequestSettings object based on this builder.
		 * 
		 * @return
		 */
		public RequestSettings build() {
			return new RequestSettings(this);
		}
	}
}