
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;

public class SQLBridge extends Godot.SingletonBase {

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) { }

	protected void onMainPause () { }
	protected void onMainResume () { }
	protected void onMainDestroy () { }

	// singletons will always miss first onGLSurfaceChanged call
	//protected void onGLSurfaceChanged (GL10 gl, int width, int height) {  }
	//protected void onGLDrawFrame (GL10 gl) {  }

	static public Godot.SingletonBase initialize (Activity p_activity) {
		return new SQLBridge(p_activity);
	}

	public SQLBridge (Activity p_activity) {
		registerClass ("SQLBridge", new String[] {"setValue", "getValue"});

		activity = p_activity;
		KeyValueStorage.set_context(activity.getApplicationContext());

		Log.d("godot", "This is Working..!");
	}

	public void setValue(String key, String value) {
		KeyValueStorage.setValue(key, value);
	}

	public String getValue(String key) {
		return KeyValueStorage.getValue(key);
	}

	private static Activity activity;
	private static final String TAG = "SQLBridge";
}
