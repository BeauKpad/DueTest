package com.beaukpad.cashdue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NewStats extends Activity {
	public static final int SUNDAY = 1;
	public static final int MONDAY = 2;
	public static final int TUESDAY = 3;
	public static final int WEDNESDAY = 4;
	public static final int THURSDAY = 5;
	public static final int FRIDAY = 6;
	public static final int SATURDAY = 7;
	private DataHelperPrime dh;
	Shift[] allShifts;
	MyAdapter lunchAdapter;
	MyAdapter dinnerAdapter;
	ListView lunchList;
	ListView dinnerList;
	Double[] lunchAverageSalesArray;
	Double[] dinnerAverageSalesArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newstats);
		lunchList = (ListView)findViewById(R.id.lunchList);
		dinnerList = (ListView)findViewById(R.id.dinnerList);
		dh = MyApplication.getInstance().getDH();
		allShifts = dh.getAllShifts(MyApplication.getInstance());
		populateArrays();
		lunchAdapter = new MyAdapter(lunchAverageSalesArray, this, true);
		lunchList.setAdapter(lunchAdapter);
		dinnerAdapter = new MyAdapter(dinnerAverageSalesArray, this, false);
		dinnerList.setAdapter(dinnerAdapter);
	}

	public void populateArrays() {
		lunchAverageSalesArray = new Double[7];
		dinnerAverageSalesArray = new Double[7];
		int i;
		for(i=0;i < 7;i++){
			lunchAverageSalesArray[i] = getAverageByDayOfWeekAndShift(i+1, true);
		}
		for(i=0;i < 7;i++){
			dinnerAverageSalesArray[i] = getAverageByDayOfWeekAndShift(i+1, false);
		}
		//TODO I was here last 
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

	//
	public double getAverageByDayOfWeekAndShift(int DayOfWeek, boolean isLunch) {
		dh.open();
		Shift[] temp = dh.getByWeekDayAndShiftTime(DayOfWeek, isLunch);
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

	public class MyAdapter extends BaseAdapter {
		Double[] theValuesList;
		Activity mContext;
		boolean isLunch;

		public MyAdapter(Double[] shiftsArray, Activity context, boolean Lunch){
			theValuesList = shiftsArray;
			mContext = context;
			isLunch = Lunch;
		}
		
		public int getCount() {
			return theValuesList.length;
		}

		public Object getItem(int position) {
			return (Double)theValuesList[position];
		}

		private class ViewHolder {
			TextView tvDayOfWeek;
			TextView tvSalesValue;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			Double m_Value = theValuesList[position];
			LayoutInflater inflater = mContext.getLayoutInflater();
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.statlistitem, parent,
						false);
				vh = new ViewHolder();
				vh.tvDayOfWeek = (TextView) convertView
						.findViewById(R.id.daylabel);
				vh.tvSalesValue = (TextView) convertView
						.findViewById(R.id.salesvalue);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			vh.tvDayOfWeek.setText(Shift.getWeekDayName(position + 1));
			vh.tvSalesValue.setText("" + m_Value);
			vh.tvSalesValue.setTextColor(Shift.getAppropriateColor(m_Value, isLunch));

			return convertView;
		}

		public long getItemId(int position) {
			return 0;
		}

	}

}
