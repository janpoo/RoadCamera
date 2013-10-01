package zhangjing.police.roadcamera;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import zhangjing.police.roadcamera.R;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MainActivity extends Activity implements
		SearchView.OnQueryTextListener {

	final static String TAG = "MainActivity";

	private MapView mMapView = null;
	private MapController mMapController = null;
	private ItemizedOverlay mCameraMarkOverlay = null;
	//private ItemizedOverlay mMapOperOverlay = null;	

	private MKMapViewListener mMapListener = null;
	private MKMapTouchListener mapTouchListener = null;

	private LocationClient mLocClient;
	private LocationData locData = null;
	private LocationListenner myListener = null;
	private ImageButton locationButton = null;
	private ImageButton zoominButton = null;
	private ImageButton zoomoutButton = null;

	private boolean isRequest = false;// 是否手动触发请求定位
	private boolean isFirstLoc = true;// 是否首次定位
	private GeoPoint currentPt = null;

	private static int AddRoadCamera = 101;
	private static int EditRoadCamera = 102;
	private static int ViewRoadCamera = 103;

	private PopupOverlay pop = null;
	private ArrayList<OverlayItem> mItems = null;
	private TextView popupText = null;
	private View viewCache = null;
	private View popupInfo = null;
	private View popupLeft = null;
	private View popupRight = null;
	private Button button = null;
	private MapView.LayoutParams layoutParam = null;
	private OverlayItem mCurrentItem = null;
	private SearchView searchView = null;

	ListView listView;

	ArrayAdapter<String> adapter;
	List<RoadCameraEntity> searchResult = null;
	Object[] searchResultNames = new String[]{""};
	private DBManager db = null;

	private boolean editing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DBManager(MainActivity.this);

		initActionbar();

		// 主地图初始化（百度地图）
		RoadCameraApplication app = (RoadCameraApplication) this
				.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(this);
			app.mBMapManager.init(RoadCameraApplication.strKey,
					new RoadCameraApplication.MyGeneralListener());
		}
		setContentView(R.layout.activity_main);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();// 获取地图控制
		mMapController.enableClick(true);// 设置地图响应点击事件
		mMapController.setZoom(15);// 设置地图缩放级别
		mMapView.setBuiltInZoomControls(false);
		GeoPoint p;
		p = new GeoPoint(30191098, 120204964);
		mMapController.setCenter(p);
		mMapListener = new MainMapViewListener();
		mMapView.regMapViewListener(
				RoadCameraApplication.getInstance().mBMapManager, mMapListener);
		mapTouchListener = new MainMapViewTouchListener();// 主地图点击事件
		mMapView.regMapTouchListner(mapTouchListener);

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.setAK("18E9095fa78e338badacd1c8ef0c8698");
		locData = new LocationData();
		myListener = new LocationListenner();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(500);
		mLocClient.setLocOption(option);
		mLocClient.start();
		this.locationButton = (ImageButton) findViewById(R.id.btnLocation);
		this.locationButton.setOnClickListener(new LocationClickListener());
		this.zoominButton =  (ImageButton) findViewById(R.id.btnZoomin);
		this.zoominButton.setOnClickListener(new ZoominClickListener());
		this.zoomoutButton =  (ImageButton) findViewById(R.id.btnZoomout);
		this.zoomoutButton.setOnClickListener(new ZoomoutClickListener());

		// 地图标识点操作层初始化
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupInfo = (View) viewCache.findViewById(R.id.popinfo);
		popupLeft = (View) viewCache.findViewById(R.id.popleft);
		popupRight = (View) viewCache.findViewById(R.id.popright);
		popupRight.setOnClickListener(new DeleteButtonClickListen());
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		button = new Button(this);
		button.setBackgroundResource(R.drawable.popup);
		PopupClickListener popListener = new CameraPopupClickListener();
		pop = new PopupOverlay(mMapView, popListener);

		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(new ArrayAdapter<Object>(getApplicationContext(),				
				R.layout.list_item,
				
				searchResultNames));

		// listView.setTextFilterEnabled(true);
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(false);
		listView.setOnItemClickListener(new SearchResultItemClickListener());
		
		//处理搜索框样式 
		int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
		View searchPlate = searchView.findViewById(searchPlateId);          
        searchPlate.setBackgroundColor(Color.TRANSPARENT );
        EditText search_text = (EditText) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        search_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_medium));
        search_text.setGravity(Gravity.BOTTOM);
	}

	
	public void initActionbar() {
		// 自定义标题栏
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mTitleView = mInflater.inflate(R.layout.custom_action_bar, null);
		getActionBar().setCustomView(
				mTitleView,
				new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT));
		searchView = (SearchView) mTitleView.findViewById(R.id.search_view);
	}
	

	@Override
	public boolean onQueryTextChange(String newText) {
		Log.d("onQueryTextChange","keyword="+newText);
		if (TextUtils.isEmpty(newText)) {
			
			MainActivity.this.listView.setVisibility(View.GONE);
			
		} else {
			
			MainActivity.this.listView.setVisibility(View.VISIBLE);
			searchResult = db.query(newText);
			if (searchResult != null && searchResult.size() > 0) {
				ArrayList<String> textList = new ArrayList<String>();
				for (int i = 0; i < searchResult.size(); i++) {
					String text = searchResult.get(i).CameraName + "("
							+ searchResult.get(i).Address + ")";

					textList.add(text);

				}
				
				updateLayout(textList.toArray());
				
			}
			else
			{
				MainActivity.this.listView.setVisibility(View.GONE);
			}

		}
		

		return false;
	}
	
	/*
	public void showCameraMark(List<RoadCameraEntity> marks )
	{
		for(int i=0;i<MainActivity.this.mCameraMarkOverlay.size();i++)
		{
			OverlayItem item  = MainActivity.this.mCameraMarkOverlay.getItem(i);
			if(marks == null)
			{
				
			}
		}
	}
	*/

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateLayout(Object[] obj) {
		listView.setAdapter(new ArrayAdapter<Object>(getApplicationContext(),
				android.R.layout.simple_expandable_list_item_1, obj));
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MainActivity.this.pop.hidePop();
		editing = false;
		if (requestCode == MainActivity.AddRoadCamera) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT)
						.show();
				int clati = data.getIntExtra("clati", 0);
				int clong = data.getIntExtra("clong", 0);
				GeoPoint p = new GeoPoint(clati, clong);
				OverlayItem item1 = new OverlayItem(p,
						data.getStringExtra("camera"), "");
				item1.setMarker((getResources()
						.getDrawable(R.drawable.icon_marka)));
				MainActivity.this.mCameraMarkOverlay.addItem(item1);
			}
		} else if (requestCode == MainActivity.EditRoadCamera) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT)
						.show();
				MainActivity.this.pop.hidePop();
				MainActivity.this.mCurrentItem.setTitle(data
						.getStringExtra("camera"));

			}
		}
	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		// 退出时销毁定位
		if (mLocClient != null)
			mLocClient.stop();
		if (db != null)
			db.closeDB();
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	private void loadRoadCameraData() {
		List<RoadCameraEntity> list = null;

		this.mCameraMarkOverlay = new CameraMarkOverlay(getResources()
				.getDrawable(R.drawable.icon_marka), this.mMapView);

		list = MainActivity.this.db.query("");

		for (RoadCameraEntity c : list) {
			GeoPoint p1 = new GeoPoint(c.latitudeE6, c.longitudeE6);
			OverlayItem item1 = new OverlayItem(p1, c.CameraName, "");
			item1.setTitle(c.CameraName);
			this.mCameraMarkOverlay.addItem(item1);
		}

		this.mMapView.getOverlays().add(this.mCameraMarkOverlay);

	}

	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
		Toast.makeText(MainActivity.this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	public class LocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null) {
				Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			boolean success = false;
			String message = "";
			switch (location.getLocType()) {
			case 61:
				success = true;
				message = "GPS定位成功";
				break;
			case 62:
				message = "定位失败：扫描整合定位依据失败";
				break;
			case 63:
				message = "定位失败：网络异常，没有成功向服务器发起请求";
				break;
			case 65:
				success = true;
				message = "定位成功(缓存)";
				break;
			case 161:
				success = true;
				message = "定位成功(网络)";
				break;
			default:
				message = "定位异常，错误代码：" + location.getLocType();
				break;

			}
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
					.show();

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();

			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();

			// 是手动触发请求或首次定位时，移动到定位点
			if (success) {

				GeoPoint p = new GeoPoint((int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6));
				OverlayItem item1 = new OverlayItem(p, "当前位置", "");
				item1.setMarker((getResources()
						.getDrawable(R.drawable.nav_turn_via_1)));
				MainActivity.this.mCameraMarkOverlay.addItem(item1);
				// 移动地图到定位点
				mMapController.animateTo(p);
				isRequest = false;
				MainActivity.this.mMapView.refresh();
			}
			// 首次定位完成
			isFirstLoc = false;
		}

		public void onReceivePoi(BDLocation poiLocation) {

			if (poiLocation == null) {
				Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT)
						.show();
				return;
			} else {
				Log.v("test", "" + poiLocation.toJsonString());
			}
		}
	}

	public class MainMapViewListener implements MKMapViewListener {
		@Override
		public void onMapMoveFinish() {
			/**
			 * 在此处理地图移动完成回调 缩放，平移等操作完成后，此回调被触发
			 */
		}

		@Override
		public void onClickMapPoi(MapPoi mapPoiInfo) {
			/**
			 * 在此处理底图poi点击事件 显示底图poi名称并移动至该点 设置过：
			 * mMapController.enableClick(true); 时，此回调才能被触发
			 * 
			 */
			String title = "";
			if (mapPoiInfo != null) {
				title = mapPoiInfo.strText;
				// Toast.makeText(MainActivity.this,title,Toast.LENGTH_SHORT).show();
				mMapController.animateTo(mapPoiInfo.geoPt);
			}
		}

		@Override
		public void onGetCurrentMap(Bitmap b) {
			/**
			 * 当调用过 mMapView.getCurrentMap()后，此回调会被触发 可在此保存截图至存储设备
			 */
		}

		@Override
		public void onMapAnimationFinish() {
			/**
			 * 地图完成带动画的操作（如: animationTo()）后，此回调被触发
			 */
		}

		/**
		 * 在此处理地图载完成事件
		 */
		@Override
		public void onMapLoadFinish() {
			loadRoadCameraData();

		}
	}

	public class MainMapViewTouchListener implements MKMapTouchListener {
		@Override
		public void onMapClick(GeoPoint point) {

			MainActivity.this.pop.hidePop();
		}

		@Override
		public void onMapDoubleClick(GeoPoint point) {

		}

		@Override
		public void onMapLongClick(GeoPoint point) {
			if (!editing) {
				editing = true;
				Intent intent = null;
				intent = new Intent(MainActivity.this,
						RoadCaremaInfoActivity.class);
				intent.putExtra("action", "new");
				intent.putExtra("clati", point.getLatitudeE6());
				intent.putExtra("clong", point.getLongitudeE6());
				intent.putExtra("camera", "");
				intent.putExtra("address", "");

				MainActivity.this.startActivityForResult(intent,
						MainActivity.AddRoadCamera);
			}

		}
	}

	public class CameraMarkOverlay extends ItemizedOverlay {

		public CameraMarkOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			OverlayItem item = getItem(index);
			mCurrentItem = item;

			popupText.setText(getItem(index).getTitle());

			Bitmap[] bitMaps = { BMapUtil.getBitmapFromView(popupLeft),
					BMapUtil.getBitmapFromView(popupInfo),
					BMapUtil.getBitmapFromView(popupRight) };

			pop.showPopup(bitMaps, item.getPoint(), 32);

			return true;
		}
	}

	public class CameraPopupClickListener implements PopupClickListener {
		@Override
		public void onClickedPopup(int index) {
			if (editing) {
				return;
			}
			MainActivity.this.pop.hidePop();

			if (index == 0) {
				// 编辑
				if (MainActivity.this.mCurrentItem != null) {
					Intent intent = null;
					intent = new Intent(MainActivity.this,
							RoadCaremaInfoActivity.class);
					intent.putExtra("action", "edit");
					intent.putExtra("clati", MainActivity.this.mCurrentItem
							.getPoint().getLatitudeE6());
					intent.putExtra("clong", MainActivity.this.mCurrentItem
							.getPoint().getLongitudeE6());
					MainActivity.this.startActivityForResult(intent,
							MainActivity.EditRoadCamera);
					editing = true;
				}
			} else if (index == 2) {
				// 删除
				if (MainActivity.this.mCurrentItem != null) {
					GeoPoint p = MainActivity.this.mCurrentItem.getPoint();
					db.deleteOldCameras(p.getLatitudeE6(), p.getLongitudeE6());

					MainActivity.this.mCameraMarkOverlay
							.removeItem(MainActivity.this.mCurrentItem);

					editing = false;
					mMapView.refresh();
				}
			}
		}
	}

	public class DeleteButtonClickListen implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			if (MainActivity.this.mCurrentItem != null) {
				GeoPoint p = MainActivity.this.mCurrentItem.getPoint();
				DBManager mgr = new DBManager(MainActivity.this);
				mgr.deleteOldCameras(p.getLatitudeE6(), p.getLongitudeE6());
				mgr.closeDB();
				MainActivity.this.mCameraMarkOverlay
						.removeItem(MainActivity.this.mCurrentItem);

			}

		}

	}

	public class LocationClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			// TODO Auto-generated method stub
			requestLocClick();

		}

	}
	
	public class ZoominClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			// TODO Auto-generated method stub
			float level = MainActivity.this.mMapView.getZoomLevel();			 
			MainActivity.this.mMapController.setZoom(level + 1);

		}

	}
	
	public class ZoomoutClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			float level = MainActivity.this.mMapView.getZoomLevel();			 
			MainActivity.this.mMapController.setZoom(level - 1);
		}

	}
	
	
	public class SearchResultItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			MainActivity.this.listView.setVisibility(View.GONE);
			if(MainActivity.this.searchResult != null && MainActivity.this.searchResult.size() > arg2)
			{
				RoadCameraEntity e = MainActivity.this.searchResult.get(arg2);
				GeoPoint p = new GeoPoint(e.latitudeE6,e.longitudeE6);
				ArrayList<OverlayItem> items =  MainActivity.this.mCameraMarkOverlay.getAllItem();
				
				for(int i=0;i<items.size();i++)
				{
					GeoPoint p1= items.get(i).getPoint();
					if(p1.getLatitudeE6() == p.getLatitudeE6() && p1.getLongitudeE6() == p.getLongitudeE6())
					{
						mCurrentItem = items.get(i);

						popupText.setText(mCurrentItem.getTitle());

						Bitmap[] bitMaps = { BMapUtil.getBitmapFromView(popupLeft),
								BMapUtil.getBitmapFromView(popupInfo),
								BMapUtil.getBitmapFromView(popupRight) };

						pop.showPopup(bitMaps, mCurrentItem.getPoint(), 32);
						mMapController.animateTo(p);

					}
				}
			}
		}
		
	}

}
