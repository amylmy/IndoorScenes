/*
 * Copyright 2008 Google Inc.
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

package cn.edu.whu.indoorscene;

import com.google.android.gms.maps.model.LatLng;

/**
 * Common constants.
 * 
 * @author Leif Hendrik Wilden
 */
public class Constants {

	private Constants() {
	}

	/**
	 * The google account type.
	 */
	public static final String ACCOUNT_TYPE = "com.google";

	/**
	 * Maximum number of waypoints that will be loaded at one time.
	 */
	public static final int MAX_LOADED_WAYPOINTS_POINTS = 10000;

	/**
	 * The settings file name.
	 */
	public static final String SETTINGS_NAME = "SettingsActivity";
	
	public static float _EARTH_RADIUS = 6371000;
	public static int _SEC_2_MILLISEC = 1000;
	//	public static LatLng TAMUCC = new LatLng(27.7150831, -97.3286947);
	public static LatLng TAMUCC = new LatLng(30.5264000000,114.3564860000);
	public static int UNDEFINED_ACT = 7;
}
