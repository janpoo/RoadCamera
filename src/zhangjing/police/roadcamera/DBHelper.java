package zhangjing.police.roadcamera;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "zhangjing.police.roadcamera";
	public static final String ROADCAMERA_TABLE = "roadcamera";
	private static final int DATABASE_VERSION = 1;


	private static final String SQL_CREATE_TABLE_ROADCAMERA = 
			"CREATE TABLE ["+ ROADCAMERA_TABLE + "]"
			+"([ID] TEXT PRIMARY KEY, "
			+ "[CarmeraName] TEXT, " 
			+ "[AutoAddress] TEXT, "
			+ "[Address] TEXT, "
			+ "[latitudeE6] INTEGER, "
			+ "[longitudeE6] INTEGER, "
			+ "[Sector] INTEGER, "
			+ "[Radius] INTEGER, "
			+ "[Recorder] TEXT, "
			+ "[RecordTime] TIMESTAMP, "
			+ "[Memo] TEXT) ";
		
	
	public DBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {	
		Log.v("database", "正在初始化数据库");				
		db.execSQL(SQL_CREATE_TABLE_ROADCAMERA);
		Log.v("database", SQL_CREATE_TABLE_ROADCAMERA);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	


}
