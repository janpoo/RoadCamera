package zhangjing.police.roadcamera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

public class MainActivity extends Activity {

	final static String TAG = "MainActivity";

	private MapView mMapView = null;
	private MapController mMapController = null;
	private ItemizedOverlay mCameraMarkOverlay = null;

	private MKMapViewListener mMapListener = null;
	private MKMapTouchListener mapTouchListener = null;

	private LocationClient mLocClient;
	private LocationData locData = null;
	private LocationListenner myListener = null;
	private Button locationButton = null;

	private boolean isRequest = false;// �Ƿ��ֶ���������λ
	private boolean isFirstLoc = true;// �Ƿ��״ζ�λ
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

	private DBManager db = null;
	
	private boolean editing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DBManager(MainActivity.this);

		
		
		// ����ͼ��ʼ�����ٶȵ�ͼ��
		RoadCameraApplication app = (RoadCameraApplication) this
				.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(this);
			app.mBMapManager.init(RoadCameraApplication.strKey,
					new RoadCameraApplication.MyGeneralListener());
		}
		setContentView(R.layout.activity_main);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();// ��ȡ��ͼ����
		mMapController.enableClick(true);// ���õ�ͼ��Ӧ����¼�
		mMapController.setZoom(15);// ���õ�ͼ���ż���
		mMapView.setBuiltInZoomControls(true);
		GeoPoint p;
		p = new GeoPoint(30191098, 120204964);
		mMapController.setCenter(p);
		mMapListener = new MainMapViewListener();
		mMapView.regMapViewListener(
				RoadCameraApplication.getInstance().mBMapManager, mMapListener);
		mapTouchListener = new MainMapViewTouchListener();// ����ͼ����¼�
		mMapView.regMapTouchListner(mapTouchListener);

		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.setAK("18E9095fa78e338badacd1c8ef0c8698");
		locData = new LocationData();
		myListener = new LocationListenner();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(500);
		mLocClient.setLocOption(option);
		mLocClient.start();
		this.locationButton = (Button) findViewById(R.id.button2);
		this.locationButton.setOnClickListener(new LocationClickListener());

		// ��ͼ��ʶ��������ʼ��
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

		
	}
	
	


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MainActivity.this.pop.hidePop();
		editing = false;
		if (requestCode == MainActivity.AddRoadCamera) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(MainActivity.this, "����ɹ�", Toast.LENGTH_SHORT)
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
				Toast.makeText(MainActivity.this, "�޸ĳɹ�", Toast.LENGTH_SHORT)
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
		 * MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView������������Activityͬ������activity�ָ�ʱ�����MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/**
		 * MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		 */
		// �˳�ʱ���ٶ�λ
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

		list = MainActivity.this.db.query();

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
		Toast.makeText(MainActivity.this, "���ڶ�λ����", Toast.LENGTH_SHORT).show();
	}

	public class LocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null) {
				Toast.makeText(MainActivity.this, "��λʧ��", Toast.LENGTH_SHORT)
						.show();
				return;
			}

		
			boolean success = false;
			String message = "";
			switch (location.getLocType()) {
			case 61:
				success = true;
				message ="GPS��λ�ɹ�";
				break;
			case 62:				
				message ="��λʧ�ܣ�ɨ�����϶�λ����ʧ��";
				break;
			case 63:
				message ="��λʧ�ܣ������쳣��û�гɹ����������������";
				break;
			case 65:
				success = true;
				message = "��λ�ɹ�(����)";
				break;
			case 161:
				success = true;
				message = "��λ�ɹ�(����)";
				break;			
			default:
				message = "��λ�쳣��������룺"+location.getLocType();
				break;

			}
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
					.show();

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();

			// �������ʾ��λ����Ȧ����accuracy��ֵΪ0����
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();

			// ���ֶ�����������״ζ�λʱ���ƶ�����λ��
			if (success) {
						
				GeoPoint p = new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6));
				OverlayItem item1 = new OverlayItem(p,"��ǰλ��", "");
				item1.setMarker((getResources()
						.getDrawable(R.drawable.nav_turn_via_1)));
				MainActivity.this.mCameraMarkOverlay.addItem(item1);
				// �ƶ���ͼ����λ��
				mMapController.animateTo(p);
				isRequest = false;
				MainActivity.this.mMapView.refresh();
			}
			// �״ζ�λ���
			isFirstLoc = false;
		}

		public void onReceivePoi(BDLocation poiLocation) {

			if (poiLocation == null) {
				Toast.makeText(MainActivity.this, "��λʧ��", Toast.LENGTH_SHORT)
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
			 * �ڴ˴�����ͼ�ƶ���ɻص� ���ţ�ƽ�ƵȲ�����ɺ󣬴˻ص�������
			 */
		}

		@Override
		public void onClickMapPoi(MapPoi mapPoiInfo) {
			/**
			 * �ڴ˴�����ͼpoi����¼� ��ʾ��ͼpoi���Ʋ��ƶ����õ� ���ù���
			 * mMapController.enableClick(true); ʱ���˻ص����ܱ�����
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
			 * �����ù� mMapView.getCurrentMap()�󣬴˻ص��ᱻ���� ���ڴ˱����ͼ���洢�豸
			 */
		}

		@Override
		public void onMapAnimationFinish() {
			/**
			 * ��ͼ��ɴ������Ĳ�������: animationTo()���󣬴˻ص�������
			 */
		}

		/**
		 * �ڴ˴�����ͼ������¼�
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
			if(!editing)
			{
				editing = true;
				Intent intent = null;
				intent = new Intent(MainActivity.this, RoadCaremaInfoActivity.class);
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
			if(editing)
			{				
				return;
			}
			MainActivity.this.pop.hidePop();
			editing = true;
			
			if (index == 0) {
				// �༭
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
				}
			} else if (index == 2) {
				// ɾ��
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

}