package zhangjing.police.roadcamera;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;


public class RoadCameraApplication extends Application {
	private static RoadCameraApplication mInstance = null;
    public boolean m_bKeyRight = true;
    BMapManager mBMapManager = null;
    //18E9095fa78e338badacd1c8ef0c8698
    public static final String strKey = "18E9095fa78e338badacd1c8ef0c8698";
    /*
    	ע�⣺Ϊ�˸��û��ṩ����ȫ�ķ���Android SDK��v2.1.3�汾��ʼ������ȫ�µ�Key��֤��ϵ��
    	��ˣ�����ѡ��ʹ��v2.1.3��֮��汾��SDKʱ����Ҫ���µ�Key����ҳ�����ȫ��Key�����룬
    	���뼰����������ο�����ָ�ϵĶ�Ӧ�½�
    */
	
	@Override
    public void onCreate() {
	    super.onCreate();
		mInstance = this;
		initEngineManager(this);
		
		Log.v("zj", "�ž��ĳ���ʼ");
	}
	
	public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(strKey,new MyGeneralListener())) {
            /*
        	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), 
                    "BMapManager  ��ʼ������!", Toast.LENGTH_LONG).show();
                    */
        }
	}
	
	public static RoadCameraApplication getInstance() {
		return mInstance;
	}
	
	
	// �����¼���������������ͨ�������������Ȩ��֤�����
    static class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
               
            	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), "���������������",
                    Toast.LENGTH_LONG).show();
            }
            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                /*
            	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), "������ȷ�ļ���������",
                        Toast.LENGTH_LONG).show();
                        */
            }
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                //��ȨKey����
            	/*
                Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), 
                        "���� DemoApplication.java�ļ�������ȷ����ȨKey��", Toast.LENGTH_LONG).show();
                */
                RoadCameraApplication.getInstance().m_bKeyRight = false;
            }
        }
    }
}
