package com.beaukpad.cashdue;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DataHelperSecondary {
	protected static final String DATABASE_NAME = "backup.db";
	private static final String TABLE_NAME = "table1";
	public static final String KEY_ID = "_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_SALES = "sale";
	public static final String KEY_ISLUNCH = "islunch";
	public static final int COLUMN_ID = 0;
	public static final int COLUMN_DATE = 1;
	public static final int COLUMN_SALES = 2;
	private SQLiteDatabase db;
	private String fullPath;
	// SQL database creation string
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ " (" + KEY_ID + " integer primary key autoincrement, " + KEY_DATE
			+ " long, " + KEY_SALES + " double);";

	static String getDBName() {
		return DATABASE_NAME;
	}

	// constructors
	public DataHelperSecondary(String dbFullPath) throws SQLiteException {
		fullPath = dbFullPath;
		db = SQLiteDatabase.openDatabase(fullPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	// close the database
	public void close() {
		db.close();
	}

	// open a database
	public void open() throws SQLiteException {
		db = SQLiteDatabase.openDatabase(fullPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	// insert a collection of shifts. This can cause duplicate copies of shifts
	public int insertShifts(Shift[] shiftsArray) {
		int count = 0;
		for (Shift tempShift : shiftsArray) {
			insertShift(tempShift);
			count++;
		}
		return count;
	}

	// insert a collection of shifts, deduping first
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
				arrayCount++;
			}
		}
		returnedCount = arrayCount;
		for (Shift newShift : newShifts) {
			finalShifts[arrayCount] = new Shift(newShift);
			arrayCount++;
		}
		finalShifts = trimArray(finalShifts);
		if ((finalShifts.length >= newShiftsCount)
				&& (finalShifts.length >= oldShiftsCount)) {
			deleteAll();
			insertShifts(finalShifts);
		}
		return returnedCount;
	}

	// insert a shift using raw values. date parameter is Unix Epoch
	public long insert(Long date, Double sales, Boolean isLunch) {
		ContentValues newShiftValues = new ContentValues();
		newShiftValues.put(KEY_DATE, date);
		newShiftValues.put(KEY_SALES, sales);
		return db.insert(TABLE_NAME, null, newShiftValues);
	}

	// insert shift using a Shift object
	public long insertShift(Shift newShift) {
		ContentValues newShiftValues = new ContentValues();
		newShiftValues.put(KEY_DATE, (Long) newShift.getDate()
				.getTimeInMillis());
		newShiftValues.put(KEY_SALES, (Double) newShift.getSales());
		return newShift.DBRow_ID = db.insert(TABLE_NAME, null, newShiftValues);
	}

	// delete shift by index
	public boolean removeShift(long rowIndex) {
		return (db.delete(TABLE_NAME, KEY_ID + "=" + rowIndex, null) > 0);
	}

	// delete shift by object
	public boolean removeShift(Shift _shift) {
		if (_shift.getDBRow() != 0) {
			return removeShift(_shift.getDBRow());
		} else {
			return false;
		}
	}

	// update a sales of a shift
	public boolean updateShift(long rowIndex, Double _sales) {
		ContentValues newValue = new ContentValues();
		newValue.put(KEY_SALES, _sales);
		return (db.update(TABLE_NAME, newValue, KEY_ID + "=" + rowIndex, null) > 0);
	}

	// update a shift
	public boolean updateShift(Shift _shift) {
		ContentValues newValue = new ContentValues();
		newValue.put(KEY_SALES, _shift.getSales());
		newValue.put(KEY_DATE, _shift.getDate().getTimeInMillis());
		return (db.update(TABLE_NAME, newValue,
				KEY_ID + "=" + (_shift.getDBRow()), null) > 0);
	}

	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);
	}

	// get all shifts as a cursor
	public Cursor getAllShiftsCursor() {
		return db.query(TABLE_NAME,
				new String[] { KEY_ID, KEY_DATE, KEY_SALES }, null, null, null,
				null, KEY_DATE);
	}

	// get all shifts in an ArrayList<String>
	public ArrayList<String> getAllShiftsArray() {
		ArrayList<String> result = new ArrayList<String>();
		String tempString;
		Cursor temp = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
				KEY_SALES }, null, null, null, null, null);
		if (temp.moveToFirst()) {
			do {
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
				tempString = (new Shift(temp.getDouble(COLUMN_SALES), tempCal,
						temp.getLong(COLUMN_ID))).toString();
				result.add(tempString);
			} while (temp.moveToNext());
		}
		return result;
	}

	// get all shifts in a Shift array
	public Shift[] getAllShifts() {
		Shift[] result = {};
		Shift tempShift;
		Cursor temp = getAllShiftsCursor();
		if (temp.moveToFirst()) {
			int x = 0;
			result = new Shift[temp.getCount()];
			do {
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
				tempShift = (new Shift(temp.getDouble(COLUMN_SALES), tempCal,
						temp.getLong(COLUMN_ID)));
				result[x++] = tempShift;
			} while (temp.moveToNext());
		}
		return result;
	}

	// get all lunch shifts in a shift array
	public Shift[] getAllLunchShifts() {
		Shift[] result = {};
		Shift tempShift;
		Cursor temp = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
				KEY_SALES }, null, null, null, null, KEY_DATE);
		if (temp.moveToFirst()) {
			int x = 0;
			result = new Shift[temp.getCount()];
			do {
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
				tempShift = (new Shift(temp.getDouble(COLUMN_SALES), tempCal,
						temp.getLong(COLUMN_ID)));
				if (!tempShift.isLunch()) {
					continue;
				}
				result[x++] = tempShift;
			} while (temp.moveToNext());
		}
		return result;
	}

	// get all Dinner shifts in a shift array
	public Shift[] getAllDinnerShifts() {
		Shift[] result = {};
		Shift tempShift;
		Cursor temp = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
				KEY_SALES }, null, null, null, null, KEY_DATE);
		if (temp.moveToFirst()) {
			int x = 0;
			result = new Shift[temp.getCount()];
			do {
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
				tempShift = (new Shift(temp.getDouble(COLUMN_SALES), tempCal,
						temp.getLong(COLUMN_ID)));
				if (tempShift.isLunch()) {
					continue;
				}
				result[x++] = tempShift;
			} while (temp.moveToNext());
		}
		return trimArray(result);
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

	// get all shifts as an array of Strings
	public String[] getAllStringArray() {
		String[] result = {};
		String tempString;
		Calendar tempCal = Calendar.getInstance();
		Cursor temp = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_DATE,
				KEY_SALES }, null, null, null, null, null);
		if (temp.moveToFirst()) {
			result = new String[temp.getCount()];
			int x = 0;
			do {
				tempCal.setTimeInMillis(temp.getLong(COLUMN_DATE));
				tempString = (new Shift(temp.getDouble(COLUMN_SALES), tempCal))
						.toString();
				result[x++] = tempString;
			} while (temp.moveToNext());
		}
		return result;
	}

	// get a single shift as a cursor
	public Cursor setCursorToShift(long _rowIndex) {
		Cursor result = db.query(true, TABLE_NAME, null, KEY_ID + "="
				+ _rowIndex, null, null, null, null, null);
		if ((result.getCount() == 0) || !result.moveToFirst()) {
			throw new SQLiteException("No shifts found for row: " + _rowIndex);
		}
		return result;
	}

	// get a single shift as a shift
	public Shift getShift(long _rowIndex) throws SQLException {
		Cursor cursor = db.query(true, TABLE_NAME, new String[] { KEY_ID,
				KEY_DATE, KEY_SALES }, KEY_ID + "=" + _rowIndex, null, null,
				null, null, null);
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No shift found for row: " + _rowIndex);
		}
		// Could be trouble. How can I guarantee the following indexes?
		// A:Determined by order created in table creation, apparently
		double _sales = cursor.getDouble(COLUMN_SALES);
		long millis = cursor.getLong(COLUMN_DATE);
		long _id = cursor.getLong(COLUMN_ID);
		Calendar aCal = Calendar.getInstance();
		aCal.setTimeInMillis(millis);
		return new Shift(_sales, aCal, _id);
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
		;
		Shift[] Result = new Shift[x];
		int y = 0;
		while (y < x) {
			Result[y] = new Shift(list[y]);
			y++;
		}
		return Result;
	}

}
