package com.willnewii.waterfall.item;

import android.os.Handler;
import android.os.Message;

import com.willnewii.waterfall.WaterFallView;

/**
 * 图片更新.
 */
public class FlowViewHandler extends Handler {
	
	public static final int HANDLER_FLOWVIEW = 0 ;
	public static final int HANDLER_FLOWVIEWLAYOUT = 1 ;
	
	private WaterFallView mWaterFallView;
	
	public FlowViewHandler(WaterFallView sv){
		this.mWaterFallView = sv;
	}
	
	@Override
	public void handleMessage(Message msg) {

		// super.handleMessage(msg);

		switch (msg.what) {
		case HANDLER_FLOWVIEW:
			
			FlowView mFlowView= (FlowView) msg.obj;
			WaterFallView.Debug("width->"+msg.arg1);
			int height = msg.arg2;

			mWaterFallView.addItem(mFlowView, height);
			/**
			//将图片添加到最短列.
			int columnIndex = WaterFallView.GetMinValue(mWaterFallView.mColumn_Heights);
			mFlowView.setColumnIndex(columnIndex);

			//更新高度.
			mWaterFallView.mColumn_Heights[columnIndex] += high;
			//显示图片.并将图片放入已加载的列表中..
			mWaterFallView.mColumnLayouts.get(columnIndex).addView(mFlowView);
			mWaterFallView.mFlowList.put(mFlowView.getId(), mFlowView);
			//当前列的Size+1
			mWaterFallView.lineIndex[columnIndex]++;
			//所在列的行数
			mFlowView.setRowIndex(mWaterFallView.lineIndex[columnIndex]);
			//当前列,Size为N时的总高度.
			mWaterFallView.pin_mark[columnIndex].put(mWaterFallView.lineIndex[columnIndex],mWaterFallView.mColumn_Heights[columnIndex]);
			//当期列的最后一个元素.
			mWaterFallView.bottomIndex[columnIndex] = mWaterFallView.lineIndex[columnIndex];
			*/
			break;
		case HANDLER_FLOWVIEWLAYOUT:
			FlowViewLayout mFlowViewLayout = (FlowViewLayout) msg.obj;
			WaterFallView.Debug("width->"+msg.arg1);
//			int height = msg.arg2;
			mWaterFallView.addItem(mFlowViewLayout, (int)msg.arg2);
			break;
			
		}

	}

	@Override
	public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
		return super.sendMessageAtTime(msg, uptimeMillis);
	}
};


