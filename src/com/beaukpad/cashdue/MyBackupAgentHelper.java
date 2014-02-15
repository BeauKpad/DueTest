package com.beaukpad.cashdue;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

public class MyBackupAgentHelper extends BackupAgentHelper {

	final String DB_FILE_NAME = "shifts.db";
	
	
	public void onCreate(){
		FileBackupHelper dbs = new FileBackupHelper(this, "../databases" + DB_FILE_NAME);
		addHelper(DB_FILE_NAME, dbs);
	}
	
}
