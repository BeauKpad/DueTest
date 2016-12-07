package com.beaukpad.cashdue;

import java.util.Calendar;
import java.util.Random;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ResultActivity extends Activity implements OnClickListener {

	private static final double LUNCH_TIPOUT_MULTIPLIER = .035;
	private static final double DINNER_TIPOUT_MULTIPLIER = .0375;
	private static final boolean FAIL = true;
	private static final boolean WIN = false;
	public static String MY_PREFS = "MY_PREFS";
	Shift m_shift;
	String titleString;
	String resultString;
	String helpInfoString;
	String dataEditButtonLabel;
	TextView tvPageTitle;
	TextView tvResults;
	TextView tvHelpInfo;
	TextView tvSaveInfo;
	Button bToggleLunchDinner;
	Button bQuit;
	Button bSaveUnsave;
	Intent intent;
	boolean autoSave;
	double sales;
	double due;
	double adjustment;
	DataHelperPrime dh;
	long lastInsertedShiftDBRow;
	RelativeLayout mLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results_activity);
		Calendar nowCal;
		loadPreferences();
		lastInsertedShiftDBRow = -1;
		tvSaveInfo = (TextView)findViewById(R.id.tvIsSaved);
		tvPageTitle = (TextView) findViewById(R.id.TVResultsPageTitle);
		tvResults = (TextView) findViewById(R.id.TVCalculations);
		tvHelpInfo = (TextView) findViewById(R.id.TVHelpInfo);
		bToggleLunchDinner = (Button) findViewById(R.id.BToggleLunchDinner);
		bToggleLunchDinner.setOnClickListener(this);
		bSaveUnsave = (Button) findViewById(R.id.BDatabaseAction);
		bSaveUnsave.setOnClickListener(this);
		mLayout = (RelativeLayout)findViewById(R.id.RLResultsLayout);
		intent = getIntent();
		sales = intent.getDoubleExtra("SALES", 0.0);
		due = intent.getDoubleExtra("DUE", 0.0);
		adjustment = intent.getDoubleExtra("ADJUSTMENT", 0.0);
		nowCal = Calendar.getInstance();
		dh = MyApplication.getInstance().getDH();
		m_shift = new Shift(sales + adjustment, nowCal);
		m_shift.fixMidnightProblem();
		Calculate();
	}

	private void Calculate() {
		
		double adjustedSales = sales + adjustment;
		double charges = sales - due;
		mLayout.setBackgroundColor(m_shift.getAppropriateColor());
		double LunchTip = (adjustedSales * LUNCH_TIPOUT_MULTIPLIER);
		double DinnerTip = (adjustedSales * DINNER_TIPOUT_MULTIPLIER);
		if(m_shift.isLunch()){
			bToggleLunchDinner.setText("Show dinner results");
		}else{
			bToggleLunchDinner.setText("Show lunch results");
		}
		String titleString = "Date: " + m_shift.getDateString() + "\n ";
		if (m_shift.isLunch()) {
			titleString = titleString + "Lunch";
		} else {
			titleString = titleString + "Dinner";
		}
		String FinalString;
		String salesString = String.format("Sales: %.2f", sales) + ".\n";
		String cashDueString = String.format("Cash Due: %.2f", due) + ".\n";
		String ChargesString = String.format("Total Charges: %.2f", charges)
				+ ".\n";
		String LunchTipString = String.format("Lunch tip-out: %.2f", LunchTip)
				+ " + roller.\n";
		String DinnerTipString = String.format("Dinner tip-out: %.2f",
				DinnerTip) + " + roller.\n";
		if (adjustedSales != sales) {
			LunchTipString = ("Adjusted ") + LunchTipString;
			DinnerTipString = ("Adjusted ") + DinnerTipString;
		}
		if (m_shift.isLunch()) {
			FinalString = salesString + cashDueString + ChargesString
					+ LunchTipString;
		} else {
			FinalString = salesString + cashDueString + ChargesString
					+ DinnerTipString;
		}
		// wire up displays
		tvPageTitle.setText(titleString);
		tvResults.setText(FinalString);

		if (autoSave) {
			lastInsertedShiftDBRow = insertAShift();
		}
		if(lastInsertedShiftDBRow == -1){
			tvSaveInfo.setText("Shift is NOT saved");
			bSaveUnsave.setText("Save Shift");
		}else {
			tvSaveInfo.setText("Shift is saved");
			bSaveUnsave.setText("Unsave Shift");
		}
		
		if (m_shift.isLunch()) {
			if (adjustedSales >= 525) {
				new soundPlayTask().execute(WIN);
			}
			if (adjustedSales <= 275) {
				new soundPlayTask().execute(FAIL);
			}
		} else {
			if (adjustedSales >= 1200) {
				new soundPlayTask().execute(WIN);
			}
			if (adjustedSales <= 400) {
				new soundPlayTask().execute(FAIL);
			}
		}
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
				mp = MediaPlayer.create(ResultActivity.this, R.raw.fail1);
				break;
			case 1:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.fail2);
				break;
			case 2:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.fail3);
				break;
			case 3:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.fail4);
				break;
			case 4:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.fail5);
				break;
			default:
				return;
			}
		} else {
			switch (y) {
			case 0:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win1);
				break;
			case 1:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win2);
				break;
			case 2:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win3);
				break;
			case 3:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win4);
				break;
			case 4:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win5);
				break;
			case 5:
				mp = MediaPlayer.create(ResultActivity.this, R.raw.win6);
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

	public void loadPreferences() {
		// get stored preferences
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences myPrefs = getSharedPreferences(MY_PREFS, mode);

		// get savedPref values and set instance variable(s)
		autoSave = myPrefs.getBoolean("autosave", true);
	}

	public long insertAShift() {
		// TODO :This shit is fucked up right here. I'm using the dedupe insert
		//method from 'dh', but it only currently supports Shift array params
		//and a return of the final size of the master array. I MUST write a function
		//in the dh class that inserts a shift singally and returns the row or -1
		//long result;
		
		//Shift toAdd[] = new Shift[]{m_shift};
		//int countBefore = MyApplication.getInstance().AllShiftsGlobal.length;
		//dh.insertShiftsDeDupe(toAdd);
		//int countAfter = MyApplication.getInstance().AllShiftsGlobal.length;
		//if(countBefore < countAfter){
		//	result = MyApplication.getInstance().AllShiftsGlobal[countAfter - 1].getDBRow();
		//}else {
		//	result = -1;
		//}
		//return result;
		return dh.insertShift(m_shift);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.BToggleLunchDinner:
			m_shift.setIsLunch((!m_shift.isLunch()));
			if(lastInsertedShiftDBRow != -1){
				dh.removeShiftByID(lastInsertedShiftDBRow);
				lastInsertedShiftDBRow = -1;
			}
			Calculate();
			break;
		case R.id.BDatabaseAction:
			if (lastInsertedShiftDBRow == -1) {
				lastInsertedShiftDBRow = insertAShift();
				bSaveUnsave.setText("Unsave Shift");
				tvSaveInfo.setText("Shift is saved");
				break;
			} else {
				boolean success = dh.removeShiftByID(lastInsertedShiftDBRow);
				if (success) {
					lastInsertedShiftDBRow = -1;
					bSaveUnsave.setText("Save Shift");
					tvSaveInfo.setText("Shift is NOT saved");
				}
				break;
			}
		default:
			break;
		}
	}
	public class soundPlayTask extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected Void doInBackground(Boolean... fail) {
			MediaPlayer mp = null;
			int x;
			if (fail[0]) {
				x = 5;
			} else {
				x = 6;
			}
			int y = new Random().nextInt(x);
			if (fail[0]) {
				switch (y) {
				case 0:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.fail1);
					break;
				case 1:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.fail2);
					break;
				case 2:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.fail3);
					break;
				case 3:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.fail4);
					break;
				case 4:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.fail5);
					break;
				default:
					return null;
				}
			} else {
				switch (y) {
				case 0:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win1);
					break;
				case 1:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win2);
					break;
				case 2:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win3);
					break;
				case 3:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win4);
					break;
				case 4:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win5);
					break;
				case 5:
					mp = MediaPlayer.create(ResultActivity.this, R.raw.win6);
					break;
				default:
					return null;
				}
			}
			if (mp != null) {
				mp.start();
			}			
			return null;
		}

	}

}
