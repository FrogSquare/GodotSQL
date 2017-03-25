
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.godotengine.godot.data.AESObfuscator;
import org.godotengine.godot.data.Base64;
import org.godotengine.godot.data.Base64DecoderException;

import java.sql.Timestamp;

public class KeyValueStorage {

	public static void set_context(Context act) {
		context = act;
	}

	private static String getDeviceId() {
		String android_id = Settings.Secure.getString(
		context.getContentResolver(), Settings.Secure.ANDROID_ID);

		if (android_id == null) {
			android_id = "GODOT_FAKE_ID";
		}

		//String deviceId = md5(android_id).toUpperCase(Locale.US);
		return android_id;
        }

	public static String getValue(String key) {
		Log.d(TAG, "Fetching value for " + key);

		key = getAESObfuscator().obfuscateString(key);
		String val = getDatabase().getKeyVal(key);

		if (val != null && !TextUtils.isEmpty(val)) {
			try {
				val = getAESObfuscator().unobfuscateToString(val);
			} catch (AESObfuscator.ValidationException e) {
				Log.e(TAG, e.getMessage());
				val = "0";
			}

			Log.d(TAG, "The fetched value is " + val);
		} else { val = "0"; }

	return val;
	}

	public static void setNonEncryptedKeyValue(String key, String val) {
		Log.d(TAG, "Setting " + val + " for key: " + key);

		if (val.equals("false")) { val = "0"; }
		else if  (val.equals("true")) { val = "1"; }

		val = getAESObfuscator().obfuscateString(val);

		getDatabase().setKeyVal(key, val);
	}

	public static void deleteNonEncryptedKeyValue(String key) {
		Log.d(TAG, "Deleting " + key);

		getDatabase().deleteKeyVal(key);
	}

	public static String getNonEncryptedKeyValue(String key) {
		Log.d(TAG, "Fetching value for " + key);

		String val = getDatabase().getKeyVal(key);

		if (val != null && !TextUtils.isEmpty(val)) {
			try {
				val = getAESObfuscator().unobfuscateToString(val);
			} catch (AESObfuscator.ValidationException e) {
				Log.e(TAG, e.getMessage());
				val = "0";
			}

			Log.d(TAG, "The fetched value is " + val);
		} else { val = "0"; }

		return val;
	}

	public static HashMap<String, String> getNonEncryptedQueryValues(String query) {
		HashMap<String, String> vals = getDatabase().getQueryVals(query);
		HashMap<String, String> results = new HashMap<String, String>();

		for(String key : vals.keySet()) {
			String val = vals.get(key);

			if (val != null && !TextUtils.isEmpty(val)) {
				try {
					val = getAESObfuscator().unobfuscateToString(val);
					results.put(key, val);
				} catch (AESObfuscator.ValidationException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}

		Log.d(TAG, "Fetched " + results.size() + " results");

		return results;
	}

	public static String getOneForNonEncryptedQuery(String query) {
		String val = getDatabase().getQueryOne(query);
		if (val != null && !TextUtils.isEmpty(val)) {
			try {
				val = getAESObfuscator().unobfuscateToString(val);
				return val;
			} catch (AESObfuscator.ValidationException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	}

	public static int getCountForNonEncryptedQuery(String query) {
		return getDatabase().getQueryCount(query);
	}

	public static List<String> getEncryptedKeys() {
		List<String> encryptedKeys = getDatabase().getAllKeys();
		List<String> resultKeys = new ArrayList<String>();

		for (String encryptedKey : encryptedKeys) {
			try {
				String unencryptedKey =
					getAESObfuscator().unobfuscateToString(encryptedKey);
				resultKeys.add(unencryptedKey);
			} catch (AESObfuscator.ValidationException e) {
				Log.d(TAG, e.getMessage());
			} catch (RuntimeException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		return resultKeys;
	}

	public static void setValue(String key, String val) {
		Log.d(TAG, "Setting " + val + " for key: " + key);

		if (val.equals("false")) { val = "0"; }
		else if  (val.equals("true")) { val = "1"; }

		key = getAESObfuscator().obfuscateString(key);
		val = getAESObfuscator().obfuscateString(val);

		getDatabase().setKeyVal(key, val);
	}

	public static void deleteKeyValue(String key) {
		Log.d(TAG, "Deleting " + key);

		key = getAESObfuscator().obfuscateString(key);

		getDatabase().deleteKeyVal(key);
	}

	public static void purge() {
		Log.d(TAG, "Purging database");

		getDatabase().purgeDatabaseEntries(context);
	}

	private static synchronized KeyValDatabase getDatabase() {
		if (mKvDatabase == null) {
			mKvDatabase = new KeyValDatabase(context);
		}

		return mKvDatabase;
	}

	private static AESObfuscator getAESObfuscator() {
		if (mObfuscator == null) {
			mObfuscator = new AESObfuscator(
			obfuscationSalt, context.getPackageName(), getDeviceId());
		}

		return mObfuscator;
	}

	private static final String TAG = "SQLBridge";

	public static final byte[] obfuscationSalt =
	new byte[] { 64, -24, -123, -57, 38, -52, 87, -112, -65, -117, 89, 51, -113, -35, 30, 57, -55,
	75, -26, 4 };

	private static AESObfuscator mObfuscator;
	private static KeyValDatabase mKvDatabase;

	private static Context context;
	private static String package_name;
}
