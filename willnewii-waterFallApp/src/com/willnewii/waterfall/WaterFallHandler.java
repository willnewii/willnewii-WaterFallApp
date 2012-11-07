package com.willnewii.waterfall;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.willnewii.waterfallapp.BuildConfig;

/**
 * 触发Scroll  的  滑动到底部刷新事件.
 */
public class WaterFallHandler extends Handler {
	
	public static final int HANDLER_WHAT = 0 ;
	
	private View mColumeView;
	private WaterFallView waterFallView;
	private int mSensitivityY ;
	
	public WaterFallHandler(View ls, WaterFallView waterFallView , int SensitivityY) {
		this.mColumeView = ls;
		this.waterFallView = waterFallView;
		this.mSensitivityY = SensitivityY ;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case HANDLER_WHAT:
			if (mColumeView.getMeasuredHeight() - mSensitivityY <= waterFallView.getScrollY() + waterFallView.getHeight()) {
				if(BuildConfig.DEBUG){
					Log.d(WaterFallView.TAG,"onBottom"+ "childViewHeight->"+mColumeView.getMeasuredHeight() +"waterFallView->" + (waterFallView.getScrollY() + waterFallView.getHeight()));
				}
				if (waterFallView.onScrollListener != null) {
					waterFallView.onScrollListener.onBottom();
				}

			} else if (waterFallView.getScrollY() <= 0) {
				if (waterFallView.onScrollListener != null) {
					if(BuildConfig.DEBUG){Log.d(WaterFallView.TAG, "onTop->" + waterFallView.getScrollY());}
					waterFallView.onScrollListener.onTop();
				}
			} else {
				if (waterFallView.onScrollListener != null) {
					if(BuildConfig.DEBUG){Log.d(WaterFallView.TAG, "onScroll");}
					waterFallView.onScrollListener.onScroll();
				}
			}
			break;

		default:
			break;
		}
	}

}
