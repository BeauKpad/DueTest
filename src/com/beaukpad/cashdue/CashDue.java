package com.beaukpad.cashdue;

import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CashDue extends Activity implements
		android.view.View.OnClickListener {
	private static final double LUNCH_TIPOUT_MULTIPLIER = .0325;
	private static final double DINNER_TIPOUT_MULTIPLIER = .035;
	private static final boolean FAIL = true;
	private static final boolean WIN = false;
	EditText editTextSales;
	EditText editTextDue;
	EditText editTextAdjust;
	TextView textViewAdjust;
	LinearLayout llMain;
	Calendar lastNow;
	DataHelper dh;
	boolean isLunch;
	boolean autoSave;
	CheckBox checkBoxAdjust;
	public static String MY_PREFS = "MY_PREFS";
	long lastInsertedShiftDBRow = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dh = MyApplication.getInstance().dh;
		dh.open();
		setContentView(R.layout.main);
		llMain = (LinearLayout) findViewById(R.id.MainLayout);
		setBackGround();
		textViewAdjust = (TextView) findViewById(R.id.TextViewAdjust);
		checkBoxAdjust = (CheckBox) findViewById(R.id.adjustCheckBox);
		Button buttonCalculate = (Button) findViewById(R.id.Button01);
		Button buttonClear = (Button) findViewById(R.id.Button02);
		Button buttonPastShifts = (Button)findViewById(R.id.bPastShifts);
		Button buttonStats = (Button)findViewById(R.id.bStatistics);
		editTextSales = (EditText) findViewById(R.id.EditTextSales);
		editTextSales.setText("");
		editTextDue = (EditText) findViewById(R.id.EditTextDue);
		editTextDue.setText("");
		editTextAdjust = (EditText) findViewById(R.id.adjustEditText);
		editTextAdjust.setVisibility(View.INVISIBLE);
		editTextAdjust.setText("");
		textViewAdjust.setVisibility(View.INVISIBLE);
		buttonPastShifts.setOnClickListener(this);
		buttonStats.setOnClickListener(this);
		buttonClear.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				editTextSales.setText("");
				editTextDue.setText("");
				editTextAdjust.setText("");
			}
		});
		buttonCalculate.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Calculate(false);
			}

		});
		checkBoxAdjust
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							textViewAdjust.setVisibility(View.VISIBLE);
							editTextAdjust.setVisibility(View.VISIBLE);
							editTextAdjust.requestFocus();
						} else {
							textViewAdjust.setVisibility(View.INVISIBLE);
							editTextAdjust.setVisibility(View.INVISIBLE);
						}
					}
				});
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

	public long insertAShift() {
		long result;
		double bSales = Double.valueOf(editTextSales.getText().toString());
		// adjust if user requested
		double adjustment = 0.0;
		if (checkBoxAdjust.isChecked()) {
			try {
				adjustment = Double
						.valueOf(editTextAdjust.getText().toString());
			} catch (NumberFormatException e) {
			}
		}
		bSales = bSales + adjustment;
		Calendar bCalendar = lastNow;
		Shift bShift = new Shift(bSales, bCalendar);
		bShift.fixMidnightProblem();
		dh.open();
		result = dh.insertShift(bShift);
		BackupManager.dataChanged(getBaseContext().getPackageName());
		return result;
	}

	public void soundPlay(boolean fail) {
		MediaPlayer mp = null;
		int x;
		if (fail) {
			x = 5;
		} else {
			x = 6;
		}
		int y = new Random().nextInt(x);
		if (fail) {
			switch (y) {
			case 0:
				mp = MediaPlayer.create(CashDue.this, R.raw.fail1);
				break;
			case 1:
				mp = MediaPlayer.create(CashDue.this, R.raw.fail2);
				break;
			case 2:
				mp = MediaPlayer.create(CashDue.this, R.raw.fail3);
				break;
			case 3:
				mp = MediaPlayer.create(CashDue.this, R.raw.fail4);
				break;
			case 4:
				mp = MediaPlayer.create(CashDue.this, R.raw.fail5);
				break;
			default:
				return;
			}
		} else {
			switch (y) {
			case 0:
				mp = MediaPlayer.create(CashDue.this, R.raw.win1);
				break;
			case 1:
				mp = MediaPlayer.create(CashDue.this, R.raw.win2);
				break;
			case 2:
				mp = MediaPlayer.create(CashDue.this, R.raw.win3);
				break;
			case 3:
				mp = MediaPlayer.create(CashDue.this, R.raw.win4);
				break;
			case 4:
				mp = MediaPlayer.create(CashDue.this, R.raw.win5);
				break;
			case 5:
				mp = MediaPlayer.create(CashDue.this, R.raw.win6);
				break;
			default:
				return;
			}
		}
		if (mp != null) {
			mp.start();
		}
		return;
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
		return true;
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
		default:
			break;
		}
		return true;
	}

	public void switchAutosave() {
		// get a preference editor to edit SharedPreferences
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS, mode)
				.edit();
		autoSave = !autoSave;
		editor.putBoolean("autosave", autoSave);
		editor.commit();
	}

	public void loadPreferences() {
		// get stored preferences
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences myPrefs = getSharedPreferences(MY_PREFS, mode);

		// get savedPref values and set instance variable(s)
		autoSave = myPrefs.getBoolean("autosave", true);
	}

	public void Calculate(final boolean forceOther) {
		// forceOther means force other shift (show lunch during dinner or
		// vice/versa)
		// lunch ends provides an end time for lunch shift
		Calendar lunchEnds = MyApplication.getInstance().getCal();
		// determine if it is currently lunchtime
		isLunch = ((lunchEnds.after(lastNow = Calendar.getInstance())) && (Calendar
				.getInstance().get(Calendar.HOUR_OF_DAY) > 11));
		// Change the shift if told so by parameter
		if (forceOther) {
			isLunch = !isLunch;
		}
		double Sales;
		double Due;
		double Adjustment = 0.0;
		double AdjustedSales;
		// load preferences to set autosave value
		loadPreferences();
		// try to parse user input. On fail, return to user input screen
		try {
			Sales = Double.valueOf(editTextSales.getText().toString());
		} catch (NumberFormatException e) {
			return;
		}
		try {
			Due = Double.valueOf(editTextDue.getText().toString());
		} catch (NumberFormatException d) {
			return;
		}
		if (Due > Sales) {
			Toast.makeText(CashDue.this,
					"Sales must be greater than Cash due.", Toast.LENGTH_SHORT)
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
						"Invalid adjustment value. Sales not adjusted",
						Toast.LENGTH_SHORT).show();
			}
		} else
			Adjustment = 0.0;
		AdjustedSales = (Sales + Adjustment);
		if (AdjustedSales < 0.0) {
			Toast.makeText(CashDue.this, "Invalid adjustment value.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// the actual calculations. Should probably use constants
		double Charges = (Sales - Due);
		double LunchTip = (AdjustedSales * LUNCH_TIPOUT_MULTIPLIER);
		double DinnerTip = (AdjustedSales * DINNER_TIPOUT_MULTIPLIER);
		// build the string
		String FinalString;
		String salesString = String.format("Sales: %.2f", Sales) + ".\n";
		String cashDueString = String.format("Cash Due: %.2f", Due) + ".\n";
		String ChargesString = String.format("Total Charges: %.2f", Charges)
				+ ".\n";
		String LunchTipString = String.format("Lunch tip-out: %.2f", LunchTip)
				+ " + roller.\n";
		String DinnerTipString = String.format("Dinner tip-out: %.2f",
				DinnerTip) + " + roller.\n";
		if (checkBoxAdjust.isChecked()) {
			LunchTipString = ("Adjusted ") + LunchTipString;
			DinnerTipString = ("Adjusted ") + DinnerTipString;
		}
		if (isLunch) {
			FinalString = ChargesString + LunchTipString;
		} else {
			FinalString = ChargesString + DinnerTipString;
		}
		FinalString = salesString + cashDueString + FinalString;
		// play win or fails sounds
		// get context for mediaplayer
		Context context = CashDue.this;
		if (!isLunch) {
			if (AdjustedSales >= 1200) {
				soundPlay(WIN);
			}
			if (AdjustedSales <= 400) {
				soundPlay(FAIL);
			}
		} else {
			if (AdjustedSales >= 525) {
				soundPlay(WIN);
			}
			if (AdjustedSales <= 275) {
				soundPlay(FAIL);
			}
		}
		// result dialog
		// make strings for dialog
		String title = "Results";
		String buttonSaveString = "Save and Go Back";
		if (autoSave) {
			buttonSaveString = "Go Back / Unsave";
		}
		String buttonQuitString = "Quit";
		String buttonChangeString;
		if (isLunch) {
			buttonChangeString = "Show Dinner";
		} else {
			buttonChangeString = "Show Lunch";
		}
		// build the dialog and wire up the buttons
		AlertDialog.Builder resultDialog = new AlertDialog.Builder(context);
		resultDialog.setTitle(title);
		resultDialog.setMessage(FinalString);
		resultDialog.setPositiveButton(buttonSaveString, new OnClickListener() {
	
			public void onClick(DialogInterface dialog, int which) {
				lastNow = Calendar.getInstance();
				// if saves are done manually OR a previously saved shift does
				// not exist, save as a new shift and go back...
				// ...(by falling through.) Set rowid returned as last saved
				// shift...
				if (!autoSave || lastInsertedShiftDBRow == 0) {
					lastInsertedShiftDBRow = insertAShift();
					BackupManager
							.dataChanged(getBaseContext().getPackageName());
					// ... elsewise, autosave is set, and last saved shift is
					// known. Delete last saved shift, and go back (by falling
					// thru)
				} else {
					dh.removeShift(lastInsertedShiftDBRow);
					lastInsertedShiftDBRow = 0;
					BackupManager
							.dataChanged(getBaseContext().getPackageName());
				}
			}
		});
		resultDialog.setNegativeButton(buttonQuitString, new OnClickListener() {
	
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		resultDialog.setNeutralButton(buttonChangeString,
				new OnClickListener() {
	
					public void onClick(DialogInterface dialog, int which) {
						dh.removeShift(lastInsertedShiftDBRow);
						lastInsertedShiftDBRow = 0;
						BackupManager.dataChanged(getBaseContext()
								.getPackageName());
						Calculate(!forceOther);
					}
				});
		// if autosave is set, save this shift
		if (autoSave) {
			lastInsertedShiftDBRow = insertAShift();
			BackupManager.dataChanged(getBaseContext().getPackageName());
		}
		// show the dialog
		resultDialog.show();
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(MyApplication.getInstance().getGlobalArray().length == 0){
			Toast emptyToast = Toast.makeText(getApplicationContext(), "No saved shifts!", Toast.LENGTH_SHORT);
			emptyToast.show();
			return;
		}
		Intent tempIntent;
		switch (v.getId()) {
		case R.id.bPastShifts:
			tempIntent = new Intent(this, DataActivity.class);
			startActivity(tempIntent);
			break;
		case R.id.bStatistics:
			tempIntent = new Intent(this, Stats.class);
			startActivity(tempIntent);

		default:
			break;
		}

	}
}
