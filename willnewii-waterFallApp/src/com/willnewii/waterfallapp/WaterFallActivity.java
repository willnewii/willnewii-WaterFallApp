package com.willnewii.waterfallapp;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.willnewii.waterfall.WaterFallOption;
import com.willnewii.waterfall.WaterFallView;
import com.willnewii.waterfall.WaterFallView.OnScrollListener;
import com.willnewii.waterfall.item.FlowViewHandler;
import com.willnewii.waterfall.item.FlowViewLayout;

public class WaterFallActivity extends Activity implements OnScrollListener {

	private WaterFallView mWaterFallView;
	
	private Handler mFlowViewHandler;
	public final int Column_Count = 3;// 显示列数

	private int PageSize = 10 ;
	
	private String TAG = getClass().getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waterfall_display);
		
		initLayout();
	}

	private void initLayout() {
		Display display = getWindowManager().getDefaultDisplay();
		
		//所有布局在设置间隙的时候都用了padding ,所以在计算宽度的时候,不需要考虑 间隙.
		int item_width = display.getWidth() / Column_Count;
		
		//1 初始化waterfall 
		mWaterFallView = (WaterFallView) findViewById(R.id.waterfall_scroll);
		//2 初始化显示容器
		LinearLayout waterfall_container = (LinearLayout) findViewById(R.id.waterfall_container);
		//3,设置滚动监听
		mWaterFallView.setOnScrollListener(this);
		//4,实例一个设置
		WaterFallOption mOption = new WaterFallOption(item_width, Column_Count);
		
		//5,初始化android瀑布流
		mWaterFallView.initWaterFall(mOption , waterfall_container);

		mFlowViewHandler = new FlowViewHandler(mWaterFallView);
		onRefresh();
	}
	
	//模拟加载.
	private void onRefresh(){
		mWaterFallView.State = WaterFallView.State_Running;
		
		new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(3000);
						} catch (Exception e) {
							// TODO: handle exception
						}
						//模拟生成数据.
						ArrayList<SimpleObject> mSimples = new ArrayList<SimpleObject>();
						for (int i = 0; i < PageSize ; i++) {
							Random rand = new Random();
							int r = rand.nextInt(ImageMock.imageThumbUrls.length);
							
							mSimples.add(new SimpleObject(ImageMock.imageThumbUrls[r], "第" + i));
						}
						AddItemToContainer(mSimples);
						mWaterFallView.State = WaterFallView.State_OK;
					}
				}).start();
	}

	/**
	 * 添加了点击删除事件.
	 * 但是需要考虑数据同步问题.
	 * @param pageindex
	 * @param mPageSize
	 */
	private void AddItemToContainer(ArrayList<SimpleObject> mSimples) {
		int currentIndex = mWaterFallView.getSize();
		//限制最大加载值.
		int MaxPictureCount = mWaterFallView.mOption.MaxPictureCount;
		
		for (int i = currentIndex ,j = 0; j < mSimples.size() && i < MaxPictureCount; i++ , j++) {
			AddImageLayout(mSimples.get(j) , i);
		}
	}
	
	/**
	 * @param filename
	 * @param position
	 */
	private void AddImageLayout(SimpleObject mSimple, int id) {
		mSimple.setContent("ID 为:" + id);
		FlowViewLayout item = new FlowViewLayout(this , mSimple.getContent());
		//唯一标识.
		item.setId(id);
		item.setPadding(0, 2, 0, 2);
		item.setFilePath(mSimple.getFilePath());
		item.setItemWidth(mWaterFallView.mOption.Column_Width);
		
		item.setErrorImage(mWaterFallView.mOption.ErrorImage);
		item.setViewHandler(mFlowViewHandler);
		item.setItemListener(mItemListener);
	
		item.LoadImage(false);
	}

	@Override
	public void onBottom() {
		Log.i(TAG, "onBottom");
		if(mWaterFallView.State == WaterFallView.State_OK){
			ShowToast("正在加载请稍后...");
			onRefresh();
		}else{
			ShowToast("已经在加载中...");
		}
		
	}
	@Override
	public void onTop() {
		if(BuildConfig.DEBUG)
			Log.i(TAG, "onTop");
	}

	@Override
	public void onScroll() {
		if(BuildConfig.DEBUG)
			Log.i(TAG, "onScroll");
	}

	@Override
	public void onAutoScroll(int l, int t, int oldl, int oldt) {
	}
	
	FlowViewLayout.onItemListener mItemListener = new FlowViewLayout.onItemListener() {
		@Override
		public void onItemLongClick(int id) {
			ShowToast("id:" + id);
//			mWaterFallView.mFlowList.get(id);
		}
		@Override
		public void onItemClick(int id) {
//			ShowToast("id:" + id);
			mWaterFallView.deleteItem((FlowViewLayout)mWaterFallView.mFlowList.get(id));
		}
	};
	
	public void ShowToast(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

}
