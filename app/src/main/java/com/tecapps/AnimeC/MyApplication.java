package com.tecapps.AnimeC;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.example.util.AnalyticsTrackers;
import com.example.util.TypefaceUtil;
import com.onesignal.OneSignal;

public class MyApplication extends Application{

	public static MyApplication instance;
	public SharedPreferences LastPositionHolder;
	public SharedPreferences preferences;
	public String prefName = "video";

	public static MyApplication getAppInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		OneSignal.startInit(this)
				.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
				.init();
		AnalyticsTrackers.initialize(this);
		AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
		TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "myfonts/custom.ttf");
	}

	public MyApplication() {
		instance = this;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	public void saveIsRemember(boolean flag) {
		preferences = this.getSharedPreferences(prefName, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("IsLoggedRemember", flag);
		editor.apply();
	}

	public boolean getIsRemember() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getBoolean("IsLoggedRemember", false);
	}
	public void saveRemember(String email, String password) {
		preferences = this.getSharedPreferences(prefName, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("remember_email", email);
		editor.putString("remember_password", password);
		editor.apply();
	}

	public String getRememberEmail() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getString("remember_email", "");
	}

	public String getRememberPassword() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getString("remember_password", "");
	}

	public void saveIsLogin(boolean flag) {
		preferences = this.getSharedPreferences(prefName, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("IsLoggedIn", flag);
		editor.apply();
	}

	public boolean getIsLogin() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getBoolean("IsLoggedIn", false);
	}
	public void saveLogin(String user_id, String user_name, String email) {
		preferences = this.getSharedPreferences(prefName, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("user_id", user_id);
		editor.putString("user_name", user_name);
		editor.putString("email", email);
		editor.apply();
	}

	public String getUserId() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getString("user_id", "0");
	}

	public String getUserName() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getString("user_name", "");
	}

	public String getUserEmail() {
		preferences = this.getSharedPreferences(prefName, 0);
		return preferences.getString("email", "");
	}
}
