package zhangjing.police.roadcamera;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;
	private static String TAG = "database";

	public DBManager(Context context) {
		helper = new DBHelper(context);

		db = helper.getWritableDatabase();
	}

	public void addCameraInfo(RoadCameraEntity cameras) {
		Log.v(DBManager.TAG, "addCameraInfo->" + cameras.toString());
		db.beginTransaction(); 
		try {

			String sql = "INSERT INTO "
					+ DBHelper.ROADCAMERA_TABLE
					+ "(ID,Address,CarmeraName,latitudeE6,longitudeE6) VALUES(?,?, ?, ?, ?)";
			db.execSQL(sql, new Object[] { UUID.randomUUID().toString(),
					cameras.Address, cameras.CameraName, cameras.latitudeE6,
					cameras.longitudeE6 });
			db.setTransactionSuccessful(); 

		} catch (Exception ex) {
			Log.e("ERROR", ex.getMessage());
		} finally {
			db.endTransaction(); 
		}
	}

	public RoadCameraEntity getCameraInfo(int clati, int clong) {
		RoadCameraEntity result = new RoadCameraEntity();
		Log.v(DBManager.TAG,
				"getCameraInfo->"
						+ String.format("clati=%d;clong=%d ", clati, clong));
		Cursor c = db.rawQuery(
				"SELECT ID,Address,CarmeraName,latitudeE6,longitudeE6 FROM "
						+ DBHelper.ROADCAMERA_TABLE + " where latitudeE6= "
						+ clati + "  and longitudeE6=" + clong, null);
		c.moveToFirst();
		Log.v(DBManager.TAG,
				"getCameraInfo->" + String.format("coutn=%d ", c.getCount()));
		if (c.getCount() > 0) {
			result.ID = c.getString(0);
			result.Address = c.getString(1);
			result.CameraName = c.getString(2);
			result.latitudeE6 = c.getInt(3);
			result.longitudeE6 = c.getInt(4);
			
			if (result != null)
				Log.v(DBManager.TAG, "getCameraInfo->" + result.toString());
		}
		c.close();
		return result;
	}


	public void updateCameraInfo(RoadCameraEntity cameras) {
		Log.v(DBManager.TAG, "update->" + cameras.toString());
		ContentValues cv = new ContentValues();
		cv.put("CarmeraName", cameras.CameraName);
		cv.put("address", cameras.Address);
		db.update(DBHelper.ROADCAMERA_TABLE, cv, "latitudeE6=? and longitudeE6=?",
				new String[] { String.valueOf(cameras.latitudeE6), String.valueOf(cameras.longitudeE6) });
	}


	public void deleteOldCameras(int clati, int clong) {
		Log.v(DBManager.TAG,
				"delete->" + String.format("clati=%d;clong=%d ", clati, clong));
		db.delete(DBHelper.ROADCAMERA_TABLE, "latitudeE6=? and longitudeE6=?",
				new String[] { String.valueOf(clati), String.valueOf(clong) });
	}


	public List<RoadCameraEntity> query(String keyword) {
		ArrayList<RoadCameraEntity> cameras = new ArrayList<RoadCameraEntity>();
		Cursor c = queryTheCursor(keyword);
		Log.v(DBManager.TAG,
				"query->" + String.format("count= %d", c.getCount()));
		while (c.moveToNext()) {
			RoadCameraEntity entity = new RoadCameraEntity();

			entity.ID = c.getString(0);
			entity.Address = c.getString(1);
			entity.CameraName = c.getString(2);
			entity.latitudeE6 = c.getInt(3);
			entity.longitudeE6 = c.getInt(4);

			cameras.add(entity);
			Log.v(DBManager.TAG, "query->" + entity.toString());
		}
		c.close();
		return cameras;
	}


	public Cursor queryTheCursor(String keyWord) {
		Cursor c = db.rawQuery(
				"SELECT ID,Address,CarmeraName,latitudeE6,longitudeE6 FROM "
						+ DBHelper.ROADCAMERA_TABLE  + " where CarmeraName like '%"+keyWord+"%' or Address like '%"+keyWord+"%'", null);
		return c;
	}

	
	public void closeDB() {
		db.close();
	}
}