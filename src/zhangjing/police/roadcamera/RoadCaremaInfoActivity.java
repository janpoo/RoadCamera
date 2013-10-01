package zhangjing.police.roadcamera;

import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionInfo;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.SearchView.OnCloseListener;
import android.widget.TextView;

public class RoadCaremaInfoActivity extends Activity {

	private EditText cameraNameEdit = null;
	private EditText cameraAddressEdit = null;
	private EditText cameraPositionEdit = null;
	private Button saveButton = null;

	private MKSearch mSearch = null;
	private DBManager db = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DBManager(RoadCaremaInfoActivity.this);

		setContentView(R.layout.activity_road_carema_info);

		cameraNameEdit = (EditText) this.findViewById(R.id.editName);
		cameraAddressEdit = (EditText) this.findViewById(R.id.editAddr);
		cameraPositionEdit = (EditText) this.findViewById(R.id.editPoistion);
		saveButton = (Button) this.findViewById(R.id.button1);
		saveButton.setOnClickListener(new SaveButtonClickListener());

		initPositionSearch();
		initView();

	}

	private void initView() {
		Intent intent = RoadCaremaInfoActivity.this.getIntent();
		String action = intent.getStringExtra("action");
		if ("new".equals(action)) {
			int clati = intent.getIntExtra("clati", 0);
			int clong = intent.getIntExtra("clong", 0);
			String s = clati + "," + clong;

			this.cameraPositionEdit.setText(s);

			GeoPoint p = new GeoPoint(clati, clong);
			mSearch.reverseGeocode(p);
			Toast.makeText(RoadCaremaInfoActivity.this, "正在检索到当前位置所处的街道及商圈...",
					Toast.LENGTH_SHORT).show();
		} else if ("edit".equals(action)) {
			int clati = intent.getIntExtra("clati", 0);
			int clong = intent.getIntExtra("clong", 0);
			String s = clati + "," + clong;
			this.cameraPositionEdit.setText(s);
			RoadCameraEntity entity = db.getCameraInfo(clati, clong);
			if (entity != null) {
				this.cameraNameEdit.setText(entity.CameraName);
				this.cameraAddressEdit
						.setText(entity.Address);
			} else {
				Toast.makeText(RoadCaremaInfoActivity.this, "在数据库里找不到要编辑的点!!!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void initPositionSearch() {
		// 初始化搜索模块，注册搜索事件监听
		RoadCameraApplication app = (RoadCameraApplication) this
				.getApplication();
		mSearch = new MKSearch();
		mSearch.init(app.mBMapManager, new MKSearchListener() {
			@Override
			public void onGetPoiDetailSearchResult(int type, int error) {

			}

			public void onGetPoiResult(MKPoiResult res, int type, int error) {

			}

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
				if (error != 0) {
					Toast.makeText(RoadCaremaInfoActivity.this,
							"没有检索到当前位置所处的街道及商圈", Toast.LENGTH_SHORT).show();
				} else {
					RoadCaremaInfoActivity.this.cameraNameEdit
							.setText(res.strBusiness);
					RoadCaremaInfoActivity.this.cameraAddressEdit
							.setText(res.strAddr);
				}
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			/**
			 * 更新建议列表
			 */
			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {

			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}
		});
	}

	class SaveButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = RoadCaremaInfoActivity.this.getIntent();
			String action = intent.getStringExtra("action");
			if ("new".equals(action)) {
				RoadCameraEntity entity = new RoadCameraEntity();
				entity.Address = RoadCaremaInfoActivity.this.cameraAddressEdit
						.getText().toString();
				entity.CameraName = RoadCaremaInfoActivity.this.cameraNameEdit
						.getText().toString();
				int clati = intent.getIntExtra("clati", 0);
				int clong = intent.getIntExtra("clong", 0);

				entity.latitudeE6 = clati;
				entity.longitudeE6 = clong;

				RoadCaremaInfoActivity.this.db.addCameraInfo(entity);

				intent.putExtra("address", entity.Address);
				intent.putExtra("camera", entity.CameraName);

			} else if ("edit".equals(action)) {
				RoadCameraEntity entity = new RoadCameraEntity();
				entity.Address = RoadCaremaInfoActivity.this.cameraAddressEdit
						.getText().toString();
				entity.CameraName = RoadCaremaInfoActivity.this.cameraNameEdit
						.getText().toString();
				int clati = intent.getIntExtra("clati", 0);
				int clong = intent.getIntExtra("clong", 0);

				entity.latitudeE6 = clati;
				entity.longitudeE6 = clong;

				RoadCaremaInfoActivity.this.db.updateCameraInfo(entity);

				intent.putExtra("address", entity.Address);
				intent.putExtra("camera", entity.CameraName);
			}

			RoadCaremaInfoActivity.this.setResult(RESULT_OK, intent);
			RoadCaremaInfoActivity.this.finish();

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.road_carema_info, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		if (db != null)
			db.closeDB();
		super.onDestroy();

	}
}
