package com.willnewii.waterfall;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.willnewii.waterfall.item.FlowView;
import com.willnewii.waterfall.item.FlowViewLayout;
import com.willnewii.waterfallapp.BuildConfig;

public class WaterFallView extends ScrollView {

	public static final String TAG = "WaterFallView";

	public WaterFallOption mOption ;
	
	/** 子元素/列View的Parent */
	public ViewGroup mColumnGroup;
	
	/** 列View容器 */
	public ArrayList<LinearLayout> mColumnLayouts;

	/** 列View高度 */
	public int[] mColumn_Heights;// 每列的高度
	
	/** 
	 * 存放View
	 * 索引是view的id.
	 * 由于实现机制问题.这是图片已经下载好,显示出来的数量.
	 * 和数据源数量有出入.
	 * public HashMap<Integer, FlowView> iviews;
	 */
	public SparseArray<View> mFlowList;
	
	/** 触发onTop/onBottom/onScroll  */
	public Handler mWaterFallHandler;
	
	/** 通过滑动方向和距离 来实现图片的加载和释放.  */
	public WaterFallUtils waterFallUtils;
	
	//可以加载
	public static int State_OK = 0;
	//正在下载
	public static int State_Running  = 1; 
	/** 状态.防止在底部多次刷新*/
	public int State = State_OK ;
	
	public int[] topIndex;
	public int[] bottomIndex;
	public int[] lineIndex;
	//记录m列 第N元素添加时的总高度. 为了加载/释放 .
	// public SparseArray<Integer> [] pin_mark;
	public SparseIntArray[] pin_mark;


	public WaterFallView(Context context) {
		super(context);
	}

	public WaterFallView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaterFallView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		onScrollListener.onAutoScroll(l, t, oldl, oldt);
		waterFallUtils.autoReload(l, t, oldl, oldt);
	}

	private void init() {
		this.setOnTouchListener(onTouchListener);
		mColumn_Heights = new int[mOption.Column_Count];
		mFlowList = new SparseArray<View>();
		pin_mark = new SparseIntArray[mOption.Column_Count];
		this.lineIndex = new int[mOption.Column_Count];
		this.bottomIndex = new int[mOption.Column_Count];
		this.topIndex = new int[mOption.Column_Count];
		for (int i = 0; i < mOption.Column_Count; i++) {
			lineIndex[i] = -1;
			bottomIndex[i] = -1;
		}
		// 初始化话waterfall_items 用于加载图片
		mColumnLayouts = new ArrayList<LinearLayout>();
		for (int i = 0; i < mOption.Column_Count; i++) {
			LinearLayout itemLayout = new LinearLayout(getContext());
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(mOption.Column_Width, LayoutParams.WRAP_CONTENT);
			
			if(i != 0 && i != (mOption.Column_Count - 1)){
				itemLayout.setPadding(mOption.Column_Padding, mOption.Column_Padding, mOption.Column_Padding, mOption.Column_Padding);
			}
			//第一列右边不需要设置
			else if (i == 0){
				itemLayout.setPadding(mOption.Column_Padding, mOption.Column_Padding, 0, mOption.Column_Padding);
			}
			//最后一列左边不需要设置
			else if (i == (mOption.Column_Count - 1)){
				itemLayout.setPadding(0, mOption.Column_Padding, mOption.Column_Padding, mOption.Column_Padding);
			}
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setLayoutParams(itemParam);
			mColumnLayouts.add(itemLayout);
			mColumnGroup.addView(itemLayout);
			pin_mark[i] = new SparseIntArray();
		}
	}

	/**
	 * 初始化操作.
	 * 获得参考的View，主要是为了获得它的MeasuredHeight，然后和滚动条的ScrollY+getHeight作比较。
	 */
	public void initWaterFall(WaterFallOption options , ViewGroup viewGroup) {

		mOption = options;
		this.mColumnGroup = viewGroup;
		
		waterFallUtils = new WaterFallUtils(this);
		if (mColumnGroup != null) {
			mWaterFallHandler = new WaterFallHandler(mColumnGroup, this , options.mSensitivityY);
			init();
		}
	}
	
	OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(BuildConfig.DEBUG)
					Log.d(TAG,"ACTION_DOWN" + "Y->" + event.getY() + "X->"+ event.getX());
				break;
			case MotionEvent.ACTION_UP:
				if (mColumnGroup != null && onScrollListener != null) {
					mWaterFallHandler.sendMessageDelayed(mWaterFallHandler.obtainMessage(WaterFallHandler.HANDLER_WHAT),200);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				// Log.d(TAG,"ACTION_MOVE"+"Y->"+
				// event.getY()+"X->"+event.getX());
				break;
			default:
				break;
			}
			return false;
		}

	};

	/**
	 * 定义接口 底部刷新
	 * @author admin
	 */
	public interface OnScrollListener {
		void onBottom();

		void onTop();

		void onScroll();

		void onAutoScroll(int l, int t, int oldl, int oldt);
	}

	protected OnScrollListener onScrollListener;

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	/**
	 * 通过图片的真实高度,提前算出布局,进行预加载.
	 * @param mFlowView
	 * @param imageHeight
	 * @param imageWidth
	 */
	public void addItem(FlowViewLayout mFlowView , int imageHeight , int imageWidth){
		ViewGroup.LayoutParams mLayoutParam = mFlowView.getLayoutParams() ;
		
		int layoutHeight = (imageHeight * mFlowView.getItemWidth()) / imageWidth;// 调整高度
		if (mLayoutParam == null) {
			mLayoutParam = new LayoutParams(mFlowView.getItemWidth(), layoutHeight);
		}
		/**
		mLayoutParam = new LayoutParams(mFlowView.getItemWidth(), layoutHeight);
		mLayoutParam.setMargins(0, 2, 0, 2);
		item.setLayoutParams(mLp);
		item.setBackgroundResource(R.drawable.background_corners);
		item.setPadding(1, 1, 1, 1);
		*/
		
		setLayoutParams(mLayoutParam);
		
		addItem(mFlowView, layoutHeight);
	}
	
	/**
	 * 宽高未知,加载模式.
	 * @param mFlowView
	 * @param height
	 */
	public void addItem(FlowViewLayout mFlowView , int height){
		//将图片添加到最短列.
		int columnIndex = GetMinValue(this.mColumn_Heights);

		//添加布局.
		this.mColumnLayouts.get(columnIndex).addView(mFlowView);
		//更新该列的高度.
		this.mColumn_Heights[columnIndex] += height;
		//更新该列计数.
		this.lineIndex[columnIndex]++;
		//设置列和行
		mFlowView.setColumnIndex(columnIndex);
		mFlowView.setRowIndex(lineIndex[columnIndex]);
		
		this.mFlowList.put(mFlowView.getId(), mFlowView);
		this.pin_mark[columnIndex].put(this.lineIndex[columnIndex],this.mColumn_Heights[columnIndex]);
		this.bottomIndex[columnIndex] = this.lineIndex[columnIndex];
	}
	
	public void deleteItem(FlowView mFlowView) {
		//行数
		int rowIndex = mFlowView.getRowIndex();
		//列数
		int columnIndex = mFlowView.getColumnIndex();

		int height = mFlowView.getHeight();
		
		//去除在该列的显示/去除在pin_mark里的索引/去除在mFlowList的索引.
		mColumnLayouts.get(columnIndex).removeView(mFlowView);
		this.pin_mark[columnIndex].removeAt(rowIndex);
		this.mFlowList.remove(mFlowView.getId());
		
		//更新布局.让该列布局位置向上移.
		for (int i = rowIndex; i < pin_mark[columnIndex].size(); i++) {
			this.pin_mark[columnIndex].put(i,this.pin_mark[columnIndex].get(i + 1) - height);
			this.pin_mark[columnIndex].removeAt(i + 1);
			((FlowView) this.mColumnLayouts.get(columnIndex).getChildAt(i)).setRowIndex(i);
		}

		lineIndex[columnIndex]--;
		mColumn_Heights[columnIndex] -= height;
		if (this.bottomIndex[columnIndex] > this.lineIndex[columnIndex]) {
			bottomIndex[columnIndex]--;
		}
	}
	
	public void deleteItem(FlowViewLayout mFlowView) {
		//行数
		int rowIndex = mFlowView.getRowIndex();
		//列数
		int columnIndex = mFlowView.getColumnIndex();

		int height = mFlowView.getHeight();
		
		//去除在该列的显示/去除在pin_mark里的索引/去除在mFlowList的索引.
		mColumnLayouts.get(columnIndex).removeView(mFlowView);
		this.pin_mark[columnIndex].removeAt(rowIndex);
		this.mFlowList.remove(mFlowView.getId());
		
		//更新布局.让该列布局位置向上移.
		for (int i = rowIndex; i < pin_mark[columnIndex].size(); i++) {
			this.pin_mark[columnIndex].put(i,this.pin_mark[columnIndex].get(i + 1) - height);
			this.pin_mark[columnIndex].removeAt(i + 1);
			((FlowView) this.mColumnLayouts.get(columnIndex).getChildAt(i)).setRowIndex(i);
		}

		lineIndex[columnIndex]--;
		mColumn_Heights[columnIndex] -= height;
		if (this.bottomIndex[columnIndex] > this.lineIndex[columnIndex]) {
			bottomIndex[columnIndex]--;
		}
	}
	
	/**
	 * 获取当前已经加载的图片总数.
	 */
	public int getSize(){
		int size = 0;
		for (int i = 0; i < lineIndex.length; ++i) {
			size += lineIndex[i] + 1;
		}
		return size ;
	}

	/**
	 * 设置滑动速率
	 */
	@Override
	public void fling(int velocityY) {
		super.fling(velocityY / mOption.VelocityY);
		Debug("velocity-->" + velocityY);
	}
	
	/**
	 * 获取最小值
	 * @param array
	 * @return
	 */
	public static int GetMinValue(int[] array) {
		int m = 0;
		int length = array.length;
		for (int i = 0; i < length; ++i) {
			if (array[i] < array[m]) {
				m = i;
			}
		}
		return m;
	}

	public static void Debug(String message) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, message);
		}
	}
}
