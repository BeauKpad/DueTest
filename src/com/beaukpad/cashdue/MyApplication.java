package com.beaukpad.cashdue;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


public class MyApplication extends Application {
	Calendar lunchEnds;
	Shift[] AllShiftsGlobal;
	public DataHelperPrime dh;
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
	public Calendar getCal() {
		return lunchEnds;		
	}
	//debugger class
	public class myAlertDialog extends AlertDialog.Builder {
		AlertDialog.Builder singleton;
		@Override
		public AlertDialog show(){
			return singleton.show();
		}
		public myAlertDialog(Context _context, String message) {
			super(_context);
			singleton = new AlertDialog.Builder(_context);
			singleton.setMessage(message);
			singleton.setPositiveButton("ok", new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	public Shift[] getGlobalArray(){
		Shift[] resultArray = new Shift[AllShiftsGlobal.length];
		for(int x = 0; x < AllShiftsGlobal.length; x++){
			resultArray[x] = new Shift(AllShiftsGlobal[x]);
		}
		return resultArray;
	}
	public void updateGlobalArray(){
		AllShiftsGlobal = dh.getAllShifts();
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
