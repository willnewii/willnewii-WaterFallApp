package com.willnewii.waterfall.item;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.willnewii.waterfallapp.R;
import com.youxilua.common.fetch.CacheFetch;

/**
 * 增加对Bitmap为空的处理.
 * 刚开始觉得应该添加Position. 实践,瀑布流是个乱序,完全没有必要. id记录就OK~
 */
public class FlowView extends ImageView implements View.OnClickListener,
		View.OnLongClickListener {

	private Context context;
	/** 图片列数*/
	private int columnIndex;
	/** 图片行数*/
	private int rowIndex;
	/** 布局中-图片宽度*/
	private int ItemWidth;
	/** 当获取图片异常时,默认使用.*/
	private int ErrorImage ;
	
	public Bitmap bitmap;
	private String filePath;
	private Handler viewHandler;
	private onItemListener mItemListener ;

	public FlowView(Context c, AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		this.context = c;
		Init();
	}

	public FlowView(Context c, AttributeSet attrs) {
		super(c, attrs);
		this.context = c;
		Init();
	}

	public FlowView(Context c) {
		super(c);
		this.context = c;
		Init();
	}

	private void Init() {

		setOnClickListener(this);
		setOnLongClickListener(this);
		setAdjustViewBounds(true);

	}
	
	@Override
	public void onClick(View v) {
		mItemListener.onItemClick(getId());
	}
	
	@Override
	public boolean onLongClick(View v) {
		mItemListener.onItemLongClick(getId());
		return true;
	}

	/**
	 * 点击事件调用
	 * @author Will
	 */
    public interface onItemListener {
        public void onItemClick(int position);
        public void onItemLongClick(int position);
    }

	/**
	 * 加载图片
	 * 重载图片
	 */
	public void LoadImage(boolean isReload) {
		new LoadImageThread(isReload).start();
	}


	/**
	 * 回收内存
	 */
	public void recycle() {
		setImageBitmap(null);
		if ((this.bitmap == null) || (this.bitmap.isRecycled()))
			return;
		this.bitmap.recycle();
		this.bitmap = null;
	}
	
	public void setItemListener(onItemListener mItemListener) {
		this.mItemListener = mItemListener;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public int getErrorImage() {
		return ErrorImage;
	}

	public void setErrorImage(int errorImage) {
		ErrorImage = errorImage;
	}

	public int getItemWidth() {
		return ItemWidth;
	}

	public void setItemWidth(int itemWidth) {
		ItemWidth = itemWidth;
	}

	public Handler getViewHandler() {
		return viewHandler;
	}

	public FlowView setViewHandler(Handler viewHandler) {
		this.viewHandler = viewHandler;
		return this;
	}

	/**
	 * isReload 重载/加载
	 * 整合了下载线程和重载线程.
	 * 添加了一个标记.
	 * 动作的区别就是新图片需要现在计算宽高/通知UI修改.
	 * 旧图片直接加载即可.
	 * @author Will
	 */
	class LoadImageThread extends Thread {
		boolean isReload = false ;
		
		LoadImageThread(boolean reload) {
			isReload = reload ;
		}

		public void run() {
			File cacheFile = CacheFetch.dowanLoadBitmap(getContext(),getFilePath());
			try {
				bitmap = BitmapFactory.decodeFile(cacheFile.getPath());
			} catch (Exception e) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			}
			if(bitmap == null)
				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			// if (bitmap != null) {

			// 此处不能直接更新UI，否则会发生异常：
			// CalledFromWrongThreadException: Only the original thread that
			// created a view hierarchy can touch its views.
			// 也可以使用Handler或者Looper发送Message解决这个问题

			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					if (bitmap != null) {// 此处在线程过多时可能为null
						if(isReload){//重载.
							setImageBitmap(bitmap);
						}else{
							int width = bitmap.getWidth();// 获取真实宽高
							int height = bitmap.getHeight();

							LayoutParams lp = getLayoutParams();
							int layoutHeight = (height * getItemWidth()) / width;// 调整高度
							if (lp == null) {
								lp = new LayoutParams(getItemWidth(), layoutHeight);
							}
							setLayoutParams(lp);

							//setImageBitmap(bitmap);
							Handler h = getViewHandler();
							Message m = h.obtainMessage(FlowViewHandler.HANDLER_FLOWVIEW,width, layoutHeight, FlowView.this);
							h.sendMessage(m);
						}
					}
				}
			});
		}
	}
}
