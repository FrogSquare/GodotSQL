
package org.godotengine.godot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyValDatabase {

	public KeyValDatabase(Context context) {
		mDatabaseHelper = new DatabaseHelper(context);
		mStoreDB = mDatabaseHelper.getWritableDatabase();
	}

	public synchronized void close() {
		mDatabaseHelper.close();
	}

	public void purgeDatabase(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

	public void purgeDatabaseEntries(Context context) {
		mStoreDB.delete(KEYVAL_TABLE_NAME, null, null);
	}

	public synchronized void setKeyVal(String key, String val) {
		ContentValues values = new ContentValues();
		values.put(KEYVAL_COLUMN_VAL, val);

		int affected = mStoreDB.update(KEYVAL_TABLE_NAME, values, KEYVAL_COLUMN_KEY + "='"
				+ key + "'", null);

		if (affected == 0) {
			values.put(KEYVAL_COLUMN_KEY, key);
			mStoreDB.replace(KEYVAL_TABLE_NAME, null, values);
		}
	}

	public synchronized String getKeyVal(String key) {
		Cursor cursor = mStoreDB.query(KEYVAL_TABLE_NAME, KEYVAL_COLUMNS,
			KEYVAL_COLUMN_KEY + "='" + key + "'", null, null, null, null);

		if (cursor != null && cursor.moveToNext()) {
			int valColIdx = cursor.getColumnIndexOrThrow(KEYVAL_COLUMN_VAL);
			String ret = cursor.getString(valColIdx);
			cursor.close();
			return ret;
		} else { Log.i(TAG, "return Null.!"); }

		if(cursor != null) { cursor.close(); }

		return null;
	}

	public synchronized void deleteKeyVal(String key) {
		mStoreDB.delete(KEYVAL_TABLE_NAME, KEYVAL_COLUMN_KEY + "=?", new String[] { key });
	}

	public synchronized HashMap<String, String> getQueryVals(String query) {
		query = query.replace('*', '%');
		Cursor cursor = mStoreDB.query(KEYVAL_TABLE_NAME, KEYVAL_COLUMNS, KEYVAL_COLUMN_KEY
								+ " LIKE '" + query + "'",
								null, null, null, null);

		HashMap<String, String> ret = new HashMap<String, String>();
		while (cursor != null && cursor.moveToNext()) {
			try {
				int valColIdx = cursor.getColumnIndexOrThrow(KEYVAL_COLUMN_VAL);
				int keyColIdx = cursor.getColumnIndexOrThrow(KEYVAL_COLUMN_KEY);
				ret.put(cursor.getString(keyColIdx), cursor.getString(valColIdx));
			} catch (IllegalArgumentException exx) { }
		}

		if(cursor != null) { cursor.close(); }

		return ret;
	}

	public synchronized String getQueryOne(String query) {
		query = query.replace('*', '%');
		Cursor cursor = mStoreDB.query(KEYVAL_TABLE_NAME, KEYVAL_COLUMNS, KEYVAL_COLUMN_KEY
								+ " LIKE '" + query + "'",
								null, null, null, null, "1");

		if(cursor != null) {
			boolean moved = cursor.moveToFirst();
			if (moved) {
				int valColIdx = cursor.getColumnIndexOrThrow(KEYVAL_COLUMN_VAL);
				String ret = cursor.getString(valColIdx);

				cursor.close();

				return ret;
			}
		}

		return null;
	}

	public synchronized int getQueryCount(String query) {
		query = query.replace('*', '%');
		Cursor cursor = mStoreDB.rawQuery("SELECT COUNT(" + KEYVAL_COLUMN_VAL + ") from " +
		KEYVAL_TABLE_NAME + " WHERE " + KEYVAL_COLUMN_KEY + " LIKE '" + query +"'", null);

		if(cursor != null) {
			boolean moved = cursor.moveToFirst();

			if (moved) {
				int count = cursor.getInt(0);
				cursor.close();

				return count;
			}
		}

		return 0;
	}

	public synchronized List<String> getAllKeys() {
		Cursor cursor = mStoreDB.query(KEYVAL_TABLE_NAME, new String[] { KEYVAL_COLUMN_KEY },
								null, null, null, null, null);

		List<String> ret = new ArrayList<String>();
		while (cursor != null && cursor.moveToNext()) {

			try {
				int keyColIdx = cursor.getColumnIndexOrThrow(KEYVAL_COLUMN_KEY);
				ret.add(cursor.getString(keyColIdx));
			} catch (IllegalArgumentException exx) { }
		}

		if(cursor != null) { cursor.close(); }

		return ret;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			if (!sqLiteDatabase.isReadOnly()){
			sqLiteDatabase.execSQL("PRAGMA foreign_key=ON");
			}

//			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
//			KEYVAL_TABLE_NAME + "(" + KEYVAL_COLUMN_KEY + " TEXT PRIMARY KEY, " +
//			KEYVAL_COLUMN_VAL + " TEXT)");

			sqLiteDatabase.execSQL(CREATE_TABLE_KEYVAL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
			// Nothing to do here ...
		}

	}

	private static final String KEYVAL_TABLE_NAME = "kv_store";
	public static final String KEYVAL_COLUMN_KEY = "key";
	public static final String KEYVAL_COLUMN_VAL = "val";

	public static final String CREATE_TABLE_KEYVAL = "CREATE TABLE IF NOT EXISTS "
			+ KEYVAL_TABLE_NAME + "(" + KEYVAL_COLUMN_KEY + " TEXT PRIMARY KEY, "
			+ KEYVAL_COLUMN_VAL + " TEXT)";

	private static final String[] KEYVAL_COLUMNS = { KEYVAL_COLUMN_KEY, KEYVAL_COLUMN_VAL };

	private static final String TAG = "SQLBridge";
	private static final String DATABASE_NAME  = "godot.kv.db.custom";
	private SQLiteDatabase mStoreDB;
	private DatabaseHelper mDatabaseHelper;
}
