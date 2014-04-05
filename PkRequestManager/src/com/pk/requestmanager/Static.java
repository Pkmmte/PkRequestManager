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

package com.pk.requestmanager;

import java.util.List;

class Static {
	// TODO Create static convenience methods here
	public static void meow(){
		
	}
	
	/**
	 * <bold>Convenience Method</bold><p>
	 * 
	 * Simple function that returns an integer count 
	 * with the total number of apps currently selected.
	 * 
	 * @param mApps
	 * @return int
	 */
	public static int getNumSelected(List<AppInfo> mApps)
	{
		int count = 0;
		for(AppInfo mApp : mApps) {
			if(mApp.isSelected())
				count++;
		}
		
		return count;
	}
}
