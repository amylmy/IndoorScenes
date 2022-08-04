/*
 * Copyright 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.edu.whu.indoorscene.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cn.edu.whu.indoorscene.R;

/**
 * Utilities for creating dialogs.
 * 
 * @author Jimmy Shih
 */
public class DialogUtils {

	private DialogUtils() {
	}

	/**
	 * Sets the dialog title divider.
	 * 
	 * @param context
	 *            the context
	 * @param dialog
	 *            the dialog
	 */
	public static void setDialogTitleDivider(Context context, Dialog dialog) {

		try {
			ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
			if (decorView == null) {
				return;
			}
			FrameLayout windowContentView = (FrameLayout) decorView
					.getChildAt(0);
			if (windowContentView == null) {
				return;
			}
			FrameLayout contentView = (FrameLayout) windowContentView
					.getChildAt(0);
			if (contentView == null) {
				return;
			}
			LinearLayout parentPanel = (LinearLayout) contentView.getChildAt(0);
			if (parentPanel == null) {
				return;
			}
			LinearLayout topPanel = (LinearLayout) parentPanel.getChildAt(0);
			if (topPanel == null) {
				return;
			}
			View titleDivider = topPanel.getChildAt(2);
			if (titleDivider == null) {
				return;
			}
			titleDivider.setBackgroundColor(context.getResources().getColor(
					R.color.holo_orange_dark));
		} catch (Exception e) {
			// Can safely ignore
		}
	}
}
