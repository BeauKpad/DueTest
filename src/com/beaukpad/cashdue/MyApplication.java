package com.beaukpad.cashdue;

import java.util.Calendar;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
	public static final String FONT_PATH_NASHVILLE = "fonts/nashvill.ttf";
	public static final String FONT_PATH_MIRC = "fonts/micrenc.ttf";
	Calendar lunchEnds;
	Shift[] AllShiftsGlobal;
	private DataHelperPrime dh;
	public Context context;
	private static MyApplication singleton;
	public static MyApplication getInstance(){
		return singleton;
	}
	public DataHelperPrime getDH(){
		return dh;
	}
	public Context getContext(){
		return context;
	}
	public Calendar getLunchEndCal() {
		return lunchEnds;		
	}
	//debugger class
	public Shift[] getGlobalArray(){
		updateGlobalArray();
		Shift[] resultArray = new Shift[AllShiftsGlobal.length];
		for(int x = 0; x < AllShiftsGlobal.length; x++){
			resultArray[x] = new Shift(AllShiftsGlobal[x]);
		}
		return resultArray;
	}
	public void updateGlobalArray(){
		AllShiftsGlobal = dh.getAllShifts(singleton);
	}
	@Override
	public final void onCreate(){
		super.onCreate();

		//lunchEnds is set to now. next lines adjust it to proper end time of lunch
		//it leaves the date the same
		lunchEnds = Calendar.getInstance();
		lunchEnds.add(Calendar.DAY_OF_YEAR, 0);
		lunchEnds.set(Calendar.HOUR_OF_DAY, 18);
		lunchEnds.set(Calendar.MINUTE, 0);
		lunchEnds.set(Calendar.SECOND, 0);
		singleton = this;
		context = this;
		dh = new DataHelperPrime(this);
		
	}
}
