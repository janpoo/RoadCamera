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
    	注意：为了给用户提供更安全的服务，Android SDK自v2.1.3版本开始采用了全新的Key验证体系。
    	因此，当您选择使用v2.1.3及之后版本的SDK时，需要到新的Key申请页面进行全新Key的申请，
    	申请及配置流程请参考开发指南的对应章节
    */
	
	@Override
    public void onCreate() {
	    super.onCreate();
		mInstance = this;
		initEngineManager(this);
		
		Log.v("zj", "张静的程序开始");
	}
	
	public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(strKey,new MyGeneralListener())) {
            /*
        	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), 
                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
                    */
        }
	}
	
	public static RoadCameraApplication getInstance() {
		return mInstance;
	}
	
	
	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
               
            	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), "您的网络出错啦！",
                    Toast.LENGTH_LONG).show();
            }
            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                /*
            	Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), "输入正确的检索条件！",
                        Toast.LENGTH_LONG).show();
                        */
            }
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                //授权Key错误：
            	/*
                Toast.makeText(RoadCameraApplication.getInstance().getApplicationContext(), 
                        "请在 DemoApplication.java文件输入正确的授权Key！", Toast.LENGTH_LONG).show();
                */
                RoadCameraApplication.getInstance().m_bKeyRight = false;
            }
        }
    }
}
