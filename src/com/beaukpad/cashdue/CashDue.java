package com.beaukpad.cashdue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CashDue extends Activity implements
		android.view.View.OnClickListener, OnCheckedChangeListener {
	EditText editTextSales;
	EditText editTextDue;
	EditText editTextAdjust;
	TextView textViewAdjust;
	TextView labelSales;
	TextView labelCashDue;
	LinearLayout llMain;
	Calendar lastNow;
	DataHelperPrime dh;
	boolean isLunch;
	boolean autoSave;
	CheckBox checkBoxAdjust;
	public static String MY_PREFS = "MY_PREFS";
	public static void exportDatabase() throws IOException {

		// Open local db file as input stream
		File dbFile = MyApplication.getInstance().getDatabasePath(
				DataHelperPrime.getDBName());
		FileInputStream fis = new FileInputStream(dbFile);

		String outFileName = Environment.getExternalStorageDirectory()
				+ "/shifts.db";
		// open the empty db as the output stream
		OutputStream output = new FileOutputStream(outFileName);
		// transfer bytes from inputfile to outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}
		// close the streams
		output.flush();
		output.close();
		fis.close();
	}
	public static void importDataBase() throws IOException {
		// Open local backup as input stream
		File dbFile = MyApplication.getInstance().getDatabasePath(
				DataHelperPrime.getDBName());
		String inFileName = Environment.getExternalStorageDirectory()
				+ "/shifts.db";
		File backupFile = new File(inFileName);
		FileInputStream fis = new FileInputStream(backupFile);

		// open the internal app db as the output stream
		OutputStream output = new FileOutputStream(dbFile);
		// transfer bytes from input file to output file
		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0) {
			output.write(buffer, 0, length);
		}
		// close the streams
		output.flush();
		output.close();
		fis.close();
		MyApplication.getInstance().updateGlobalArray();
	}


	Shift[] allShiftsArray;

	public void beginCalc(final boolean forceOther) {
		double Sales;
		double Due;
		double Adjustment = 0.0;
		double AdjustedSales;
		Intent intent;
		// try to parse user input. On fail, return to user input screen
		try {
			Sales = Double.valueOf(editTextSales.getText().toString());
		} catch (NumberFormatException e) {
			return;
		}
		try {
			Due = Double.valueOf(editTextDue.getText().toString());
		} catch (NumberFormatException d) {
			Toast.makeText(CashDue.this,
					"Invalid Sales entered!", Toast.LENGTH_LONG)
					.show();
			return;
		}
		if (Due > Sales) {
			Toast.makeText(CashDue.this,
					"Sales must be greater than Cash due.", Toast.LENGTH_LONG)
					.show();
			return;
		}
		// handle adjustments to sales. These should be used only in calculating
		// tipout, and as final saved sales
		if (checkBoxAdjust.isChecked()) {
			try {
				Adjustment = Double
						.valueOf(editTextAdjust.getText().toString());
			} catch (NumberFormatException e) {
				Adjustment = 0.0;
				Toast.makeText(CashDue.this,
						"Invalid adjustment value. Tipout not adjusted",
						Toast.LENGTH_LONG).show();
			}
		} else
			Adjustment = 0.0;
		AdjustedSales = (Sales + Adjustment);
		if (AdjustedSales < 0.0) {
			Toast.makeText(CashDue.this, "Invalid adjustment value. Tipout not adjusted",
					Toast.LENGTH_LONG).show();
			return;
		}
		intent = new Intent(this, ResultActivity.class);
		intent.putExtra("SALES", Sales);
		intent.putExtra("DUE", Due);
		intent.putExtra("ADJUSTMENT", Adjustment);
		startActivity(intent);
	}

	public void loadPreferences() {
		// get stored preferences
		int mode = Context.MODE_PRIVATE;
		SharedPreferences myPrefs = getSharedPreferences(MY_PREFS, mode);

		// get savedPref values and set instance variable(s)
		autoSave = myPrefs.getBoolean("autosave", true);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.adjustCheckBox:
		{

			if (isChecked) {
				textViewAdjust.setVisibility(View.VISIBLE);
				editTextAdjust.setVisibility(View.VISIBLE);
				editTextAdjust.requestFocus();
			} else {
				textViewAdjust.setVisibility(View.INVISIBLE);
				editTextAdjust.setVisibility(View.INVISIBLE);
			}
		
		}
			break;

		default:
			break;
		}
		
	}

	public void onClick(View v) {
		Intent tempIntent;
		switch (v.getId()) {
		case R.id.bPastShifts:
			if (MyApplication.getInstance().getGlobalArray().length == 0) {
				Toast emptyToast = Toast.makeText(getApplicationContext(),
						"No saved shifts!", Toast.LENGTH_SHORT);
				emptyToast.show();
				return;
			}
			tempIntent = new Intent(this, DataActivity.class);
			startActivity(tempIntent);
			break;
		case R.id.bStatistics:
			if (MyApplication.getInstance().getGlobalArray().length == 0) {
				Toast emptyToast = Toast.makeText(getApplicationContext(),
						"No saved shifts!", Toast.LENGTH_SHORT);
				emptyToast.show();
				return;
			}
			tempIntent = new Intent(this, NewStats.class);
			startActivity(tempIntent);
			break;
		case R.id.ButtonClearValues:
			editTextSales.setText("");
			editTextDue.setText("");
			editTextAdjust.setText("");
			break;
		case R.id.ButtonCalculate:
			beginCalc(false);
			break;
		default:
			break;
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dh = MyApplication.getInstance().getDH();
		allShiftsArray = MyApplication.getInstance().getGlobalArray();
		setContentView(R.layout.main);
		llMain = (LinearLayout) findViewById(R.id.MainLayout);
		setBackGround();
		Typeface nashvilleFont = Typeface.createFromAsset(getAssets(), MyApplication.FONT_PATH_NASHVILLE);
		Typeface checkBookFont = Typeface.createFromAsset(getAssets(), MyApplication.FONT_PATH_CHECKBOOK);
		textViewAdjust = (TextView) findViewById(R.id.TextViewAdjust);
		textViewAdjust.setTypeface(nashvilleFont);
		checkBoxAdjust = (CheckBox) findViewById(R.id.adjustCheckBox);
		checkBoxAdjust.setTypeface(nashvilleFont);
		Button buttonCalculate = (Button) findViewById(R.id.ButtonCalculate);
		Button buttonClear = (Button) findViewById(R.id.ButtonClearValues);
		Button buttonPastShifts = (Button) findViewById(R.id.bPastShifts);
		Button buttonStats = (Button) findViewById(R.id.bStatistics);
		editTextSales = (EditText) findViewById(R.id.EditTextSales);
		editTextSales.setTypeface(checkBookFont);
		editTextSales.setText("");
		editTextDue = (EditText) findViewById(R.id.EditTextDue);
		editTextDue.setTypeface(checkBookFont);
		editTextDue.setText("");
		editTextAdjust = (EditText) findViewById(R.id.adjustEditText);
		editTextAdjust.setVisibility(View.INVISIBLE);
		editTextAdjust.setTypeface(checkBookFont);
		editTextAdjust.setText("");
		textViewAdjust.setVisibility(View.INVISIBLE);
		labelSales = (TextView)findViewById(R.id.TVSalesLabel);
		labelSales.setTypeface(nashvilleFont);
		labelCashDue = (TextView)findViewById(R.id.TVCashDueLabel);
		labelCashDue.setTypeface(nashvilleFont);
		buttonPastShifts.setOnClickListener(this);
		buttonStats.setOnClickListener(this);
		buttonClear.setOnClickListener(this);
		buttonCalculate.setOnClickListener(this);
		checkBoxAdjust.setOnCheckedChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		loadPreferences();
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		MenuItem menuAuto = menu.findItem(R.id.autosave);
		if (autoSave) {
			menuAuto.setTitle("Autosave is on");
		} else {
			menuAuto.setTitle("Autosave is off");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.autosave:
			switchAutosave();
			if (autoSave) {
				item.setTitle("Autosave is on");
			} else {
				item.setTitle("Autosave is off");
			}
			break;
		case R.id.exportDB:
			try {
				exportDatabase();
			} catch (IOException e) {
				Toast failExportToast = Toast.makeText(this,
						"No Database Found! " + e.getMessage(),
						Toast.LENGTH_LONG);
				e.printStackTrace();
				failExportToast.show();
			}
			break;
		case R.id.importDB:
			try {
				importDataBase();
			} catch (IOException e) {
				Toast failImportToast = Toast
						.makeText(
								this,
								"No Exported Database Found! \n Database must be called 'shifts.db' and be on the root of your sd internal card",
								Toast.LENGTH_LONG);
				failImportToast.show();
			}
			break;
		case R.id.mergeIntoInternal: {
			String dbPath = Environment.getExternalStorageDirectory()
					+ "/shifts.db";
			DataHelperSecondary dhExternal;
			try {
				dhExternal = new DataHelperSecondary(dbPath);
			} catch (SQLiteException e) {
				Toast failImportToast = Toast
						.makeText(
								this,
								"No Exported Database Found! \n Database must be called 'shifts.db' and be on the root of your sd internal card",
								Toast.LENGTH_LONG);
				failImportToast.show();
				break;
			}
			Shift[] newShiftsArray = dhExternal.getAllShifts();
			dh.open();
			int recordChangeCount = dh.insertShiftsDeDupe(newShiftsArray);
			Toast showUpdateCount = Toast.makeText(this, "" + recordChangeCount
					+ "shift(s) updated!", Toast.LENGTH_LONG);
			showUpdateCount.show();
			break;
		}
		case R.id.mergeIntoBackup: {
			String backupPath = Environment.getExternalStorageDirectory()
					+ "/shifts.db";
			DataHelperSecondary dhExternal;
			try {
				dhExternal = new DataHelperSecondary(backupPath);
			} catch (SQLiteException e) {
				Toast failToast = Toast.makeText(this,
						"No Exported Database Found! Creating new one!",
						Toast.LENGTH_LONG);
				failToast.show();
				try {
					exportDatabase();
				} catch (IOException e1) {
					Toast failExportToast = Toast.makeText(this,
							"That didn't work either! Doing nothing...",
							Toast.LENGTH_LONG);
					failExportToast.show();
					break;
				}
				break;
			}
			Shift[] newShiftsArray = MyApplication.getInstance().getGlobalArray();
			int recordChangeCount = dhExternal.insertShiftsDeDupe(newShiftsArray);
			Toast showUpdateCount = Toast.makeText(this, "" + recordChangeCount
					+ "shift(s) updated in backup!", Toast.LENGTH_LONG);
			showUpdateCount.show();

			break;
		}
		default:
			break;
		}
		return true;
	}

	private void setBackGround() {
		int x = new Random().nextInt(7);
		switch (x) {
		case 0:
			llMain.setBackgroundResource(R.drawable.kevin);
			break;
		case 1:
			llMain.setBackgroundResource(R.drawable.iggy);
			break;
		case 2:
			llMain.setBackgroundResource(R.drawable.russels);
			break;
		case 3:
			llMain.setBackgroundResource(R.drawable.tuna);
			break;
		case 4:
			llMain.setBackgroundResource(R.drawable.wineroom);
			break;
		case 5:
			llMain.setBackgroundResource(R.drawable.bar2);
			break;
		case 6:
			llMain.setBackgroundResource(R.drawable.kevocto);
		}
	}


	public void switchAutosave() {
		// get a preference editor to edit SharedPreferences
		int mode = Context.MODE_PRIVATE;
		SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS, mode)
				.edit();
		autoSave = !autoSave;
		editor.putBoolean("autosave", autoSave);
		editor.commit();
	}
}
