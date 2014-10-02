package com.beaukpad.cashdue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatsFrag extends Fragment {
	public static final int SUNDAY = 1;
	public static final int MONDAY = 2;
	public static final int TUESDAY = 3;
	public static final int WEDNESDAY = 4;
	public static final int THURSDAY = 5;
	public static final int FRIDAY = 6;
	public static final int SATURDAY = 7;
	private DataHelperPrime dh;
	TextView tvStats;
	Shift[] allShifts;
	View theView;
	Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		theView = inflater.inflate(R.id.svStatScroll, container, false);
		tvStats = (TextView)theView.findViewById(R.id.TVStats);
		dh = MyApplication.getInstance().getDH();
		allShifts = MyApplication.getInstance().getGlobalArray();
		populateTextView();
		return theView;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	private void populateTextView() {
		// If there is no data (in the allShifts array), don't bother
		if (allShifts.length == 0) {
			tvStats.setText("No save shifts!!");
			return;
		}
		mContext = getActivity(); 
		Typeface ledger = Typeface.createFromAsset(mContext.getAssets(), MyApplication.FONT_PATH_LEDGER);
		tvStats.setTypeface(ledger);
		String resultString;
		resultString = "Averages by day:\n \n   Lunch:\n";
		int x;
		for (x = 1; x < 8; x++) {
			resultString = resultString + getAverageByDayOfWeekAndShiftString(x, true);
		}
		resultString = resultString + "\n   Dinner:\n";
		for (x = 1; x < 8; x++) {
			resultString = resultString + getAverageByDayOfWeekAndShiftString(x, false);
		}
		resultString = resultString + "\n \n";
		resultString = resultString + createMonthlyAverageString();
		resultString = getBestShiftString() + resultString;
		tvStats.setText(resultString);

	}
	public String getBestShiftString() {
		String resultString = "";
		Shift resultShift = new Shift(allShifts[0]);
		for (Shift tempShift : allShifts) {
			if (tempShift.getSales() > resultShift.getSales()) {
				resultShift = new Shift(tempShift);
			}
		}
		resultString = "Best shift on record: \n" + resultShift + "\n \n";
		return resultString;
	}
	public String createMonthlyAverageString() {
		String result = "Averages by Month: \n \n";
		int currentMonth;
		int currentYear;
		String monthNameString;
		double lunchRunningTotal = 0.0;
		int lunchShiftCount = 0;
		double dinnerRunningTotal = 0.0;
		int dinnerShiftCount = 0;
		// Before running the foreach loop, I want to set month/year of earliest
		// item
		currentMonth = allShifts[0].getDate().get(Calendar.MONTH);
		currentYear = allShifts[0].getDate().get(Calendar.YEAR);
		monthNameString = new SimpleDateFormat("MMMMMMMMM yyyy", Locale.US)
				.format(allShifts[0].getDate().getTime()) + "\n";
		result = result + monthNameString;

		for (Shift tempShift : allShifts) {
			if ((tempShift.getDate().get(Calendar.MONTH) == currentMonth)
					&& (tempShift.getDate().get(Calendar.YEAR) == currentYear)) {
				if (tempShift.isLunch()) {
					lunchRunningTotal += tempShift.getSales();
					lunchShiftCount++;
				} else {
					dinnerRunningTotal += tempShift.getSales();
					dinnerShiftCount++;
				}
			} else {
				if (lunchShiftCount > 0) {
					result = result
							+ "      Lunch Average: "
							+ String.format(
									"%1$, .2f",
									(lunchRunningTotal / (double) lunchShiftCount))
							+ "\n";
					lunchRunningTotal = 0.0;
					lunchShiftCount = 0;
				}
				if (dinnerShiftCount > 0) {
					result = result
							+ "      Dinner Average: "
							+ String.format(
									"%1$, .2f",
									(dinnerRunningTotal / (double) dinnerShiftCount))
							+ "\n \n";
					dinnerRunningTotal = 0.0;
					dinnerShiftCount = 0;
				} else {
					result = result + "\n";
				}
				result = result
						+ new SimpleDateFormat("MMMMMMMMM yyyy", Locale.US)
								.format(tempShift.getDate().getTime()) + "\n";
				currentMonth = tempShift.getDate().get(Calendar.MONTH);
				currentYear = tempShift.getDate().get(Calendar.YEAR);
				if (tempShift.isLunch()) {
					lunchRunningTotal = tempShift.getSales();
					lunchShiftCount = 1;
				} else {
					dinnerRunningTotal = tempShift.getSales();
					dinnerShiftCount = 1;
				}
			}
		}
		if (lunchShiftCount > 0) {
			result = result
					+ "      Lunch Average: "
					+ String.format("%1$, .2f",
							(lunchRunningTotal / (double) lunchShiftCount))
					+ "\n";
		}
		if (dinnerShiftCount > 0) {
			result = result
					+ "      Dinner Average: "
					+ String.format("%1$, .2f",
							(dinnerRunningTotal / (double) dinnerShiftCount))
					+ "\n \n";
		}

		return result;
	}
	public double getAverageByDayOfWeekAndShift(int DayOfWeek, boolean isLunch) {
		dh.open();
		Shift[] temp = dh.getByWeekDayandShiftTime(DayOfWeek, isLunch);
		dh.close();
		double result = 0.0;
		int count;
		for (count = 0; count < temp.length; count++) {
			result += temp[count].getSales();
		}
		if (count == 0) {
			return 0.0;
		}
		return result / count;
	}

	public String getAverageByDayOfWeekAndShiftString(int DayOfWeek,
			boolean isLunch) {
		double total = 0.0;
		int count = 0;
		for (Shift workingShift : allShifts) {
			if (workingShift.getDayOfWeekInt() == DayOfWeek)
				if (workingShift.isLunch() == isLunch) {
					total = total + workingShift.getSales();
					count++;
				}
		}
		if (count == 0) {
			return "      No " + Shift.getWeekDayName(DayOfWeek)
					+ "s worked! \n";
		}
		return "      "
				+ Shift.getWeekDayName(DayOfWeek)
				+ ": "
				+ String.format(Locale.US, "%1$, .2f", (total / (double) count))
				+ "\n";
	}
}
