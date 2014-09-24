package com.beaukpad.cashdue;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.graphics.Color;

public class Shift{
	public final static boolean LUNCH = true;
	public final static boolean DINNER = false;
	long DBRow_ID;
	double sales;
	Calendar date;

	public Shift() {
		DBRow_ID = 0;
		sales = 0;
		date = Calendar.getInstance();
	}

	public Shift(double _sales) {
		DBRow_ID = 0;
		sales = _sales;
		date = Calendar.getInstance();
	}

	public Shift(double _sales, Calendar _Cal) {
		DBRow_ID = 0;
		sales = _sales;
		date = _Cal;
	}

	public Shift(double _sales, Calendar _Cal, long _row) {
		DBRow_ID = _row;
		sales = _sales;
		date = _Cal;
	}
	
	public Shift(double _sales, long _dateAsEpoch) {
		sales = _sales;
		Calendar temp = Calendar.getInstance();
		temp.setTimeInMillis(_dateAsEpoch);
		date = temp;
	}
	
	public Shift(Shift seed){
		DBRow_ID = seed.getDBRow();
		sales = seed.getSales();
		date = seed.getDate();
	}

	public double getSales() {
		return sales;
	}

	public Calendar getDate() {
		return date;
	}

	public boolean setSales(double _sales) {
		sales = _sales;
		return true;
	}

	public boolean setDate(Calendar _Cal) {
		date = _Cal;
		return true;
	}

	public boolean setDBRow(long _row) {
		DBRow_ID = _row;
		return true;
	}

	public boolean setIsLunch(boolean isLunch) {
		if (isLunch) {
			date.set(Calendar.HOUR_OF_DAY, 16);
			return true;
		}
		if (!isLunch) {
			date.set(Calendar.HOUR_OF_DAY, 22);
			return true;
		}
		return false;
	}

	public long getDBRow() {
		return DBRow_ID;
	}

	public boolean isLunch() {
		if (date.get(Calendar.HOUR_OF_DAY) < 19
				&& date.get(Calendar.HOUR_OF_DAY) > 11) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isDinner() {
		return (!(isLunch()));
	}
    public int getDayOfWeekInt(){
        int intOfDay = date.get(Calendar.DAY_OF_WEEK);
        // If it is after midnight, but before lunch begins, you're working on
        // last night's shift. Adjust day of week accordingly.
        if (!isLunch() && (date.get(Calendar.HOUR_OF_DAY) < 11)) {
            intOfDay -= 1;
            if (intOfDay == 0) {
                intOfDay = 7;
            }
        }
        return intOfDay;
    }
    
    static String getWeekDayName(int dayNumber){

    	String dayOfWeek = "";
    	switch (dayNumber) {
		case 1:
			dayOfWeek = "Sunday";
			break;
		case 2:
			dayOfWeek = "Monday";
			break;
		case 3:
			dayOfWeek = "Tuesday";
			break;
		case 4:
			dayOfWeek = "Wednesday";
			break;
		case 5:
			dayOfWeek = "Thursday";
			break;
		case 6:
			dayOfWeek = "Friday";
			break;
		case 7:
			dayOfWeek = "Saturday";
			break;

		default:
			dayOfWeek = "error!";
			break;
			}
    	return dayOfWeek;
    }

    public boolean isTheSameShiftAs(Shift theShift){
    	if(sales == theShift.getSales()){
    		if(date.get(Calendar.DAY_OF_YEAR) == theShift.getDate().get(Calendar.DAY_OF_YEAR)){
    			if(this.isLunch() == theShift.isLunch()){
        			return true;
    			}
    		}
    	}
    	return false;
    }
    
    public long getTimeValue(){
    	return date.getTimeInMillis();
    }

	public String getDayOfWeek() {
		int intOfDay = date.get(Calendar.DAY_OF_WEEK);
		String dayOfWeek;
		// If it is after midnight, but before lunch begins, you're working on
		// last night's shift. Adjust day of week accordingly.
		if (!isLunch() && (date.get(Calendar.HOUR_OF_DAY) < 11)) {
			intOfDay -= 1;
			if (intOfDay == 0) {
				intOfDay = 7;
			}
		}
		switch (intOfDay) {
		case 1:
			dayOfWeek = "Sunday";
			break;
		case 2:
			dayOfWeek = "Monday";
			break;
		case 3:
			dayOfWeek = "Tuesday";
			break;
		case 4:
			dayOfWeek = "Wednesday";
			break;
		case 5:
			dayOfWeek = "Thursday";
			break;
		case 6:
			dayOfWeek = "Friday";
			break;
		case 7:
			dayOfWeek = "Saturday";
			break;

		default:
			dayOfWeek = "error!";
			break;
		}
		return dayOfWeek;
	}

	@Override
	public String toString() {
		Date aDate = date.getTime();
		String dString = (DateFormat.getDateInstance(DateFormat.LONG))
				.format(aDate);
		String lString = isLunch() ? "Lunch" : "Dinner";
		return String.format("(" + getDayOfWeek() + "  " + dString + " - " + lString
				+ ")     $%.2f", sales);
	}

	public String getDateString() {
		Date aDate = date.getTime();
		String dString = (DateFormat.getDateInstance(DateFormat.LONG))
				.format(aDate);
		return getDayOfWeek() + ", " + dString;

	}

	static public int getAppropriateColor(double sales, boolean isLunch){
		int redValue;
		int greenValue;
		int blueValue;
		if(isLunch){
			if(sales <= 150.0){
				return Color.rgb(255, 0, 0);
			}
			if(sales >= 600.0){
				return Color.rgb(88, 232, 93);
			}
			if((sales > 150.0) && (sales < 600.0)){
				redValue = (int) (255.0 + ((sales - 150.00) * (-167.0 / 450.0)));
				greenValue = (int) (0.0 + ((sales - 150.0) * (232.0 / 450.0)));
				blueValue = (int) (0.0 + ((sales - 150.0) * (93.0 / 450.0)));
				return Color.rgb(redValue, greenValue, blueValue);
				// red is #FF0000, or 255, 0, 0
				// Green is #58E85D, or 88, 232, 93
				// Difference is -167, 232, 93
			}
		}
		if (sales <= 400.0) {
			return Color.rgb(255, 0, 0);
		}
		if (sales > 1600.0) {
			return Color.rgb(88, 232, 93);
		}
		if ((sales > 400.0) && (sales <= 1600.0)) {
			redValue = (int) (255.0 + ((sales - 400.00) * (-167.0 / 1200.0)));
			greenValue = (int) (0.0 + ((sales - 400.0) * (232.0 / 1200.0)));
			blueValue = (int) (0.0 + ((sales - 400.0) * (93.0 / 1200.0)));
			return Color.rgb(redValue, greenValue, blueValue);
			// red is #FF0000, or 255, 0, 0
			// Green is #58E85D, or 88, 232, 93
			// Difference is -167, 232, 93
		}
		return 0;
		
	}
	public int getAppropriateColor() {

		int redValue;
		int greenValue;
		int blueValue;
		if(isLunch()){
			if(sales <= 150.0){
				return Color.rgb(255, 0, 0);
			}
			if(sales >= 600.0){
				return Color.rgb(88, 232, 93);
			}
			if((sales > 150.0) && (sales < 600.0)){
				redValue = (int) (255.0 + ((sales - 150.00) * (-167.0 / 450.0)));
				greenValue = (int) (0.0 + ((sales - 150.0) * (232.0 / 450.0)));
				blueValue = (int) (0.0 + ((sales - 150.0) * (93.0 / 450.0)));
				return Color.rgb(redValue, greenValue, blueValue);
				// red is #FF0000, or 255, 0, 0
				// Green is #58E85D, or 88, 232, 93
				// Difference is -167, 232, 93
			}
		}
		if (sales <= 400.0) {
			return Color.rgb(255, 0, 0);
		}
		if (sales > 1600.0) {
			return Color.rgb(88, 232, 93);
		}
		if ((sales > 400.0) && (sales <= 1600.0)) {
			redValue = (int) (255.0 + ((sales - 400.00) * (-167.0 / 1200.0)));
			greenValue = (int) (0.0 + ((sales - 400.0) * (232.0 / 1200.0)));
			blueValue = (int) (0.0 + ((sales - 400.0) * (93.0 / 1200.0)));
			return Color.rgb(redValue, greenValue, blueValue);
			// red is #FF0000, or 255, 0, 0
			// Green is #58E85D, or 88, 232, 93
			// Difference is -167, 232, 93
		}
		return 0;
	}
	//The following method will fix the after midnight problem:
	//If paperwork for a dinner shift is done after midnight, the shift will be saved for dinner the following day...
	//This method will check to see if the current time is between midnight and 11AM. If so...
	//it will push the date back one day and make the time 23:59
	static public Shift fixMidnightProblem(Shift problemShift){
		if(!(problemShift.isLunch())){
			Calendar probCal = problemShift.getDate();
			if(probCal.get(Calendar.HOUR_OF_DAY) < 10){
				probCal.add(Calendar.DATE, -1);
				probCal.set(Calendar.HOUR_OF_DAY, 23);
				probCal.set(Calendar.MINUTE, 59);
				probCal.set(Calendar.SECOND, 59);
			}
			problemShift.setDate(probCal);
		}
		return problemShift;
	}
	public void fixMidnightProblem(){
		if(isDinner()){
			if(date.get(Calendar.HOUR_OF_DAY) < 10){
				date.add(Calendar.DATE, -1);
				date.set(Calendar.HOUR_OF_DAY, 23);
				date.set(Calendar.MINUTE, 59);
				date.set(Calendar.SECOND, 59);
			}
		}
	}
}
