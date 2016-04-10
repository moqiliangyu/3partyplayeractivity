package com.ANT.MiddleWare.Integrity;

import android.util.Log;
import android.util.SparseArray;

import com.ANT.MiddleWare.Celluar.CellularDown;
import com.ANT.MiddleWare.Celluar.CellularDown.CellType;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.Segment;
import com.ANT.MiddleWare.WiFi.WiFiFactory;

public class IntegrityCheck {
	private static final String TAG = IntegrityCheck.class.getSimpleName();
	// single instance mode
	private static IntegrityCheck instance;
	private SparseArray<Segment> urlMap;
	public static final String URL_TAG = "http://buptant.cn/autoChart/du/video/ljw2016/zxyqwe/download.php";
	public static final String URI_TAG = "http://127.1.1.1:9999/";

	private IntegrityCheck() {
		urlMap = new SparseArray<Segment>();
	}

	public static synchronized IntegrityCheck getInstance() {
		if (instance == null) {
			instance = new IntegrityCheck();
		}
		return instance;
	}

	public byte[] getSegments(int uri) {
		return getSegments(uri, CellType.Single);
	}

	public byte[] getSegments(int uri, CellType ct) {
		int id = uri;
		synchronized (this) {
			if (urlMap.indexOfKey(id) < 0) {
				urlMap.put(id, new Segment(id, -1));
			}
			Segment segment = urlMap.get(id);
			if (segment.checkIntegrity()) {
				return segment.getData();
			} else {
				CellularDown.queryFragment(ct, id);
				Log.v(TAG, "url" + id);
			}
		}
		while (true) {
			synchronized (this) {
				Segment segment = urlMap.get(id);
				if (segment.checkIntegrity()) {
					return segment.getData();
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return null;// Empty Data
			}
		}

	}

	public void setSegLength(int id, int totalLength) {
		Segment s = urlMap.get(id);
		s.setSegLength(totalLength);
	}

	public void insert(int id, FileFragment fm) {
		Segment s = urlMap.get(id);
		WiFiFactory.insertF(fm);
		s.insert(fm);
	}

	public Segment getSeg(int id) {
		return urlMap.get(id);
	}
}