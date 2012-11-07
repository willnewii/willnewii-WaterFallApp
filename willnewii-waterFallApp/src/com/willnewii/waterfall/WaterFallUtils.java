package com.willnewii.waterfall;

import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.willnewii.waterfall.item.FlowViewLayout;

/**
 * 通过滑动方向
 * 进行重载/释放 动作.
 */
public class WaterFallUtils {
	private WaterFallView mWaterFallView;
	/** 容器高度.*/
	private int scrollHeight;

	private final String TAG = getClass().getSimpleName();
	
	public int getScrollHeight(ScrollView scroll) {
		return scroll.getMeasuredHeight();
	}

	public WaterFallUtils(WaterFallView waterFallView) {
		this.mWaterFallView = waterFallView;

	}

	public void autoReload(int l, int t, int oldl, int oldt) {

		scrollHeight = mWaterFallView.getMeasuredHeight();
		WaterFallView.Debug("scroll_height:" + scrollHeight);

		if (t > oldt) {// 向下滚动
			scrollDown(l, t, oldl, oldt);
		} else {// 向上滚动
			scrolldUp(l, t, oldl, oldt);
		}
	}

	private void scrolldUp(int l, int t, int oldl, int oldt) {
		if (t > 2 * scrollHeight) {// 超过两屏幕后
			for (int k = 0; k < mWaterFallView.mOption.Column_Count; k++) {
				LinearLayout localLinearLayout = mWaterFallView.mColumnLayouts.get(k);
				// 最底部的图片位置小于当前t+3*屏幕高度
				if (mWaterFallView.pin_mark[k].get(mWaterFallView.bottomIndex[k]) > t + 3 * scrollHeight) {
					WaterFallView.Debug("recycle,k:" + mWaterFallView.bottomIndex[k]);
					
					((FlowViewLayout) localLinearLayout.getChildAt(mWaterFallView.bottomIndex[k])).recycle();
					mWaterFallView.bottomIndex[k]--;
				}

				if (mWaterFallView.pin_mark[k].get(Math.max(mWaterFallView.topIndex[k] - 1,0)) >= t - 2 * scrollHeight) {
					((FlowViewLayout) localLinearLayout.getChildAt(Math.max(-1+ mWaterFallView.topIndex[k], 0))).LoadImage(true);
					mWaterFallView.topIndex[k] = Math.max(mWaterFallView.topIndex[k] - 1, 0);
				}
			}
		}
	}

	//
	private void scrollDown(int l, int t, int oldl, int oldt) {
		if (t > 2 * scrollHeight) {// 超过两屏幕后
			for (int k = 0; k < mWaterFallView.mOption.Column_Count; k++) {
				LinearLayout localLinearLayout = mWaterFallView.mColumnLayouts.get(k);
				if (mWaterFallView.pin_mark[k].get(Math.min(mWaterFallView.bottomIndex[k] + 1, mWaterFallView.lineIndex[k])) <= t+ 3 * scrollHeight) {
					
					((FlowViewLayout) mWaterFallView.mColumnLayouts.get(k).getChildAt(
							Math.min(1 + mWaterFallView.bottomIndex[k],
									mWaterFallView.lineIndex[k]))).LoadImage(true);

					mWaterFallView.bottomIndex[k] = Math.min(
							1 + mWaterFallView.bottomIndex[k], mWaterFallView.lineIndex[k]);
					WaterFallView.Debug("relaod,k:"+mWaterFallView.bottomIndex[k]);

				}
				WaterFallView.Debug("headIndex:" + mWaterFallView.topIndex[k]
						+ "  footIndex:" + mWaterFallView.bottomIndex[k]
						+ "  headHeight:"
						+ mWaterFallView.pin_mark[k].get(mWaterFallView.topIndex[k]));
				// 未回收图片的最高位置<t-两倍屏幕高度
				if (mWaterFallView.pin_mark[k].get(mWaterFallView.topIndex[k]) < t - 2 * scrollHeight) {
					WaterFallView.Debug("recycle,k:" + k + " headindex:"
							+ mWaterFallView.topIndex[k]);
					int i1 = mWaterFallView.topIndex[k];
					mWaterFallView.topIndex[k]++;
					((FlowViewLayout) localLinearLayout.getChildAt(i1)).recycle();
				}
			}

		}
	}
}