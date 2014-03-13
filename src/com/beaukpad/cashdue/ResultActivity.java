package com.beaukpad.cashdue;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends Activity {
	private static final double LUNCH_TIPOUT_MULTIPLIER = .0325;
	private static final double DINNER_TIPOUT_MULTIPLIER = .035;
	private static final boolean FAIL = true;
	private static final boolean WIN = false;
	Shift m_shift;
	String titleString;
	String resultString;
	String helpInfoString;
	String dataEditButtonLabel;
	TextView tvPageTitle;
	TextView tvResults;
	TextView tvHelpInfo;
	Button bToggleLunchDinner;
	Button bQuit;
	Button bSaveUnsave;
	Intent intent;
	double sales;
	double due;
	double adjustment;
	Calendar nowCal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results_activity);
		tvPageTitle = (TextView)findViewById(R.id.TVResultsPageTitle);
		tvResults = (TextView)findViewById(R.id.TVCalculations);
		tvHelpInfo = (TextView)findViewById(R.id.TVHelpInfo);
		bToggleLunchDinner = (Button)findViewById(R.id.BToggleLunchDinner);
		bQuit = (Button)findViewById(R.id.BQuitApplication);
		bSaveUnsave = (Button)findViewById(R.id.BDatabaseAction);
		intent = getIntent();
		sales = intent.getDoubleExtra("SALES", 0.0);
		due = intent.getDoubleExtra("DUE", 0.0);
		adjustment = intent.getDoubleExtra("ADJUSTMENT", 0.0);
		nowCal = Calendar.getInstance();
		Calculate(false);
	}

	private void Calculate(boolean forceOther) {
		// TODO Auto-generated method stub
		double adjustedSales = sales + adjustment;
		double charges = sales - due;
		double LunchTip = (adjustedSales * LUNCH_TIPOUT_MULTIPLIER);
		double DinnerTip = (adjustedSales * DINNER_TIPOUT_MULTIPLIER);
		Calendar lunchEndsCal = MyApplication.getInstance().getLunchEndCal();
		boolean isLunch = lunchEndsCal.after(nowCal) && (nowCal.get(Calendar.HOUR_OF_DAY) > 11);
		if(forceOther){
			isLunch = !isLunch;
		}
		m_shift = new Shift(adjustedSales, nowCal);
		String titleString = "Date: " + m_shift.getDateString() + "\n ";
		if(isLunch){
			titleString = titleString + "Lunch";
		}else{
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
		if (isLunch) {
			FinalString = ChargesString + LunchTipString;
		} else {
			FinalString = ChargesString + DinnerTipString;
		}
		if (!isLunch) {
			if (adjustedSales >= 1200) {
				soundPlay(WIN);
			}
			if (adjustedSales <= 400) {
				soundPlay(FAIL);
			}
		} else {
			if (adjustedSales >= 525) {
				soundPlay(WIN);
			}
			if (adjustedSales <= 275) {
				soundPlay(FAIL);
			}
		}
		//wire up displays
		tvPageTitle.setText(titleString);
		tvResults.setText(FinalString);
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
}
