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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/** AppInfo object to store basic app package data. */
public class AppInfo
{
	String Name;		// App name
	String Code;		// Component info
	Drawable Image;		// Drawable image
	boolean Selected;	// Selected status
	
	/**
	 * Default Constructor
	 * <p>
	 * All values are initialized with soft null values.
	 */
	public AppInfo() {
		this.Name = "null";
		this.Code = "null";
		this.Image = new ColorDrawable(Color.TRANSPARENT);
		this.Selected = false;
	}
	
	/**
	 * Full Constructor
	 * <p>
	 * Assigns all properties with your assigned values.
	 * 
	 * @param Name
	 * @param Code
	 * @param Image
	 * @param Selected
	 */
	public AppInfo(String Name, String Code, Drawable Image, Boolean Selected) {
		this.Name = Name;
		this.Code = Code;
		this.Image = Image;
		this.Selected = Selected;
	}
	
	public void setName(String Name) {
		this.Name = Name;
	}
	
	public void setCode(String Code) {
		this.Code = Code;
	}
	
	public void setImage(Drawable Image) {
		this.Image = Image;
	}

	public void setSelected(boolean Selected) {
		this.Selected = Selected;
	}

	public String getName() {
		return Name;
	}
	
	public String getCode() {
		return Code;
	}

	public Drawable getImage() {
		return Image;
	}

	public boolean isSelected() {
		return Selected;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof AppInfo))
			return false;
		
		AppInfo appInfo = (AppInfo) obj;
		return this.Code.equals(appInfo.getCode());
	}
	
	@Override
	public int hashCode() {
		return this.Code.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Name: " + this.Name + "\n");
		builder.append("Code: " + this.Code + "\n");
		builder.append("Image: " + this.Image.toString() + "\n");
		builder.append("Selected: " + this.Selected + "\n");
		
		return builder.toString();
	}
}