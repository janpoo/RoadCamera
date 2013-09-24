package zhangjing.police.roadcamera;

import java.security.Timestamp;

public class RoadCameraEntity {

	public String ID;
	public String CarmeraType;
	public String AutoAddress;
	public String Address;
	public String CameraName;
	public int latitudeE6;
	public int longitudeE6;
	public int Sector;
	public int Radius;
	public String Recorder;
	public Timestamp RecordTime;
	public String Memo;
	
	public RoadCameraEntity()
	{
		
	}
	
	@Override
	public String toString()
	{
		return String.format("ID:%s;Address:%s;CareraName:%s;latitudeE6:%d;longitudeE6:%d", ID,Address,CameraName,latitudeE6,longitudeE6);
	}

}
