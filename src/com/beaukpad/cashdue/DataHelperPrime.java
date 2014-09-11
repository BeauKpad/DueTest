package com.beaukpad.cashdue;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelperPrime {
	public static class OpenHelper extends SQLiteOpenHelper {
		// SQL database creation string
		public static final String DATABASE_CREATE = "create table "
				+ TABLE_NAME + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_DATE + " long, "
				+ KEY_SALES + " double);";

		public OpenHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("OpenHelper",
					"Upgrading database, this will drop tables and recreate");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	public static final int COLUMN_DATE = 1;
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_SALES = 2;
	protected static final String DATABASE_NAME = "shifts.db";
	private static final int DATABASE_VERSION = 1;
	public static final String KEY_DATE = "date";
	public static final String KEY_ID = "_id";
	public static final String KEY_ISLUNCH = "islunch";
	public static final String KEY_SALES = "sale";
	private static final String TABLE_NAME = "table1";
	static String getDBName() {
		return DATABASE_NAME;
	}

	private SQLiteDatabase db;

	private OpenHelper dbHelper;

	// constructor
	public DataHelperPrime(Context context) {
		dbHelper = new OpenHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	// close the database
	public void close() {
		if (db == null) {
			return;
		}
		db.close();
	}

	public void deleteAll() {
		open();
		this.db.delete(TABLE_NAME, null, null);
		close();
		MyApplication.getInstance().updateGlobalArray();
	}

	// get all Dinner shifts in a shift array
	public Shift[] getAllDinnerShifts() {
		Shift[] masterList = MyApplication.getInstance().getGlobalArray();
		Shift[] dinnerArray = new Shift[masterList.length];
		int index = 0;
		for(Shift tempShift : masterList){
			if(tempShift.isDinner()){
				dinnerArray[index] = new Shift(tempShift);
				index++;
			}
		}
		return trimArray(dinnerArray);
	}
	// get all lunch shifts in a shift array
	public Shift[] getAllLunchShifts() {
		Shift[] masterList = MyApplication.getInstance().getGlobalArray();
		Shift[] lunchArray = new Shift[masterList.length];
		int index = 0;
		for(Shift tempShift : masterList){
			if(tempShift.isLunch()){
				lunchArray[index] = new Shift(tempShift);
				index++;
			}
		}
		return trimArray(lunchArray);
	}
	// get all shifts in a Shift array
	private Shift[] getAllShifts() {
		Shift[] result = {};
		Shift tempShift;
		Cursor allShiftsCursor;
		open();
		allShiftsCursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
				KEY_SALES }, null, null, null, null, KEY_DATE + " DESC");
		if (allShiftsCursor.moveToFirst()) {
			int x = 0;
			result = new Shift[allShiftsCursor.getCount()];
			do {
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(allShiftsCursor.getLong(COLUMN_DATE));
				tempShift = (new Shift(allShiftsCursor.getDouble(COLUMN_SALES), tempCal,
						allShiftsCursor.getLong(COLUMN_ID)));
				result[x++] = tempShift;
			} while (allShiftsCursor.moveToNext());
		}
		allShiftsCursor.close();
		close();
		return result;
	}

	public Shift[] getAllShifts(MyApplication app){
		return getAllShifts();
	}

	public Shift[] getByWeekDayandShiftTime(int iDayOfWeek, boolean isLunch) {
		Shift[] tempAllShifts = isLunch ? (getAllLunchShifts())
				: (getAllDinnerShifts());
		Shift[] result = new Shift[tempAllShifts.length];
		int placeCount = 0;
		for (Shift workingShift : tempAllShifts) {
			if (workingShift.getDayOfWeekInt() == iDayOfWeek) {
				result[placeCount] = new Shift(workingShift);
				placeCount++;
			}
		}
		return trimArray(result);
	}

	// insert a shift using raw values. date parameter is Unix Epoch
	private long insert(Long date, Double sales, Boolean isLunch) {
		return insertShift(new Shift(sales, date));
//		ContentValues newShiftValues = new ContentValues();
//		newShiftValues.put(KEY_DATE, date);
//		newShiftValues.put(KEY_SALES, sales);
//		open();
//		long resultID = db.insert(TABLE_NAME, null, newShiftValues);
//		close();
//		return updateGlobalArray(resultID);
	}
	public long insertByValue(Long date, Double sales, Boolean isLunch) {
		return insert(date, sales, isLunch);
	}

	// insert shift using a Shift object
	//This needs to be the only gateway to insertion
	public long insertShift(Shift newShift) {
		// TODO begin drunk coding
		Shift lastShift;
		int globalSize = MyApplication.getInstance().AllShiftsGlobal.length;
		lastShift = MyApplication.getInstance().AllShiftsGlobal[globalSize - 1];
		if(newShift.isTheSameShiftAs(lastShift)){
			return lastShift.DBRow_ID;
		}
		// end drunk coding
		 ContentValues newShiftValues = new ContentValues();
		 newShiftValues.put(KEY_DATE, newShift.getDate()
		 .getTimeInMillis());
		 newShiftValues.put(KEY_SALES, newShift.getSales());
		 open();
		 newShift.DBRow_ID = updateGlobalArray(db.insert(TABLE_NAME,
		 null, newShiftValues));
		 close();
		 return updateGlobalArray(newShift.DBRow_ID);
	}
	public int insertShifts(Shift[] shiftsArray) {
		int count = 0;
		for (Shift tempShift : shiftsArray) {
			insertShift(tempShift);
			count++;
		}
		return count;
	}
	public int insertShiftsDeDupe(Shift[] newShifts) {
		Shift[] oldShifts = getAllShifts();
		int oldShiftsCount = oldShifts.length;
		int newShiftsCount = newShifts.length;
		int returnedCount = 0;
		Shift[] finalShifts = new Shift[oldShifts.length + newShifts.length];
		int arrayCount = 0;
		for (Shift oldShift : oldShifts) {
			boolean isUnique = true;
			for (Shift newShift : newShifts) {
				if (newShift.isTheSameShiftAs(oldShift)) {
					isUnique = false;
					break;
				}
			}
			if (isUnique) {
				finalShifts[arrayCount] = new Shift(oldShift);
				arrayCount++;}
		}
		returnedCount = arrayCount;
		for (Shift newShift : newShifts) {
			finalShifts[arrayCount] = new Shift(newShift);
			arrayCount++;
		}
		finalShifts = trimArray(finalShifts);
		//sanity check. On failure, return 0 
		if (!((finalShifts.length >= newShiftsCount)
				&& (finalShifts.length >= oldShiftsCount))) {
			return 0;
		}
		deleteAll();
		insertShifts(finalShifts);
		return returnedCount;
	}

	// open a database
	public void open() throws SQLiteException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}

	// get all shifts as a cursor
	// This function taught me the hard way: DON'T PASS AROUND CURSORS!!!
//	private Cursor getAllShiftsCursor() {
//		open();
//		Cursor returnCursor;
//		returnCursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
//				KEY_SALES }, null, null, null, null, KEY_DATE);
//		// If I close the database now the cursor is useless
//		// close();
//		return returnCursor;
//	}

	// get all shifts in an ArrayList<String>
//	private ArrayList<String> getAllShiftsArrayList() {
//		ArrayList<String> result = new ArrayList<String>();
//		String tempString;
//		open();
//		Cursor temp = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
//				KEY_SALES }, null, null, null, null, null);
//		close();
//		if (temp.moveToFirst()) {
//			do {
//				Calendar tempCal = Calendar.getInstance();
//				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
//				tempString = (new Shift(temp.getDouble(COLUMN_SALES), tempCal,
//						temp.getLong(COLUMN_ID))).toString();
//				result.add(tempString);
//			} while (temp.moveToNext());
//		}
//		return result;
//	}
	
	// delete shift by index
	//gatekeeper for deletion
	private boolean removeShift(long rowIndex) {
		boolean returnValue;
		open();
		returnValue = updateGlobalArray(db.delete(TABLE_NAME, KEY_ID + "="
				+ rowIndex, null) > 0);
		close();
		return returnValue;
	}
	// delete shift by object
	public boolean removeShift(Shift _shift) {
		if (_shift.getDBRow() != 0) {
			return removeShift(_shift.getDBRow());
		} else {
			return false;
		}
	}

	public boolean removeShiftByID(long rowIndex) {
		return removeShift(rowIndex);
	}

	private Shift[] trimArray(Shift[] list) {
		int x = 0;
		int size = list.length;
		while (x < size) {
			if (list[x] == null) {
				break;
			}
			x++;
		}
		Shift[] Result = new Shift[x];
		int y = 0;
		while (y < x) {
			Result[y] = new Shift(list[y]);
			y++;
		}
		return Result;
	}

	// Get all shifts in a shift cursor by day of week and shift time
	// public Shift[] getByWeekDayandShiftTime(int iDayOfWeek, boolean isLunch){
	// Shift[] result = {};
	// Shift tempShift;
	// Cursor tempCursor = getAllShiftsCursor();
	// if(tempCursor.moveToFirst()){
	// int x = 0;
	// result = new Shift[tempCursor.getCount()];
	// do{
	// Calendar tempCal = Calendar.getInstance();
	// tempCal.setTimeInMillis(tempCursor.getLong(COLUMN_DATE));
	// tempShift = (new Shift(tempCursor.getDouble(COLUMN_SALES), tempCal,
	// tempCursor.getLong(COLUMN_ID)));
	// if(tempShift.isLunch() != isLunch){continue;}
	// if(tempShift.getDayOfWeekInt() != iDayOfWeek){continue;}
	// result[x] = tempShift;
	// x++;
	// }
	// while (tempCursor.moveToNext());
	// }
	// return trimArray(result);
	// }

	public boolean updateGlobalArray(boolean mBoolean) {
		MyApplication.getInstance().updateGlobalArray();
		return mBoolean;
	}

	// get all shifts as an array of Strings
//	private String[] getAllStringArray() {
//		String[] result = {};
//		String tempString;
//		Calendar tempCal = Calendar.getInstance();
//		open();
//		Cursor allShiftsCursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
//				KEY_SALES }, null, null, null, null, null);
//		if (allShiftsCursor.moveToFirst()) {
//			result = new String[allShiftsCursor.getCount()];
//			int x = 0;
//			do {
//				tempCal.setTimeInMillis(allShiftsCursor.getLong(COLUMN_DATE));
//				tempString = (new Shift(allShiftsCursor.getDouble(COLUMN_SALES), tempCal))
//						.toString();
//				result[x++] = tempString;
//			} while (allShiftsCursor.moveToNext());
//		}
//		allShiftsCursor.close();
//		close();
//		return result;
//	}

//	// get a single shift as a cursor
//	public Cursor setCursorToShift(long _rowIndex) {
//		open();
//		Cursor result = db.query(true, TABLE_NAME, null, KEY_ID + "="
//				+ _rowIndex, null, null, null, null, null);
//		if ((result.getCount() == 0) || !result.moveToFirst()) {
//			throw new SQLiteException("No shifts found for row: " + _rowIndex);
//		}
//		return result;
//	}

	// get a single shift as a shift
//	private Shift getShift(long _rowIndex) throws SQLException {
//		open();
//		Cursor cursor = db.query(true, TABLE_NAME, new String[] { KEY_ID,
//				KEY_DATE, KEY_SALES }, KEY_ID + "=" + _rowIndex, null, null,
//				null, null, null);
//		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
//			throw new SQLException("No shift found for row: " + _rowIndex);
//		}
//		// Could be trouble. How can I guarantee the following indexes?
//		// A:Determined by order created in table creation, apparently
//		double _sales = cursor.getDouble(COLUMN_SALES);
//		long millis = cursor.getLong(COLUMN_DATE);
//		long _id = cursor.getLong(COLUMN_ID);
//		cursor.close();
//		close();
//		Calendar aCal = Calendar.getInstance();
//		aCal.setTimeInMillis(millis);
//		return new Shift(_sales, aCal, _id);
//	}

	// convenience methods for updating global shift array
	public long updateGlobalArray(long mLong) {
		MyApplication.getInstance().updateGlobalArray();
		return mLong;
	}

	// update a sales of a shift
	public boolean updateShift(long rowIndex, Double _sales) {
		Shift workingShift = new Shift(_sales);
		workingShift.setDBRow(rowIndex);
		return updateShift(workingShift);
	}

	// update a shift
	//Gatekeeper to update shifts
	private boolean updateShift(Shift _shift) {
		ContentValues newValue = new ContentValues();
		double _sales = _shift.getSales();
		long _date = _shift.getDate().getTimeInMillis();
		if(_sales > 0){
			newValue.put(KEY_SALES, _shift.getSales());
		}
		if(_date > 0){
			newValue.put(KEY_DATE, _shift.getDate().getTimeInMillis());
		}
		boolean returnValue;
		open();
		returnValue = updateGlobalArray(db.update(TABLE_NAME, newValue, KEY_ID
				+ "=" + (_shift.getDBRow()), null) > 0);
		close();
		return returnValue;
	}

	public boolean updateThisShift(Shift _shift){
		return updateShift(_shift);
	}
}
