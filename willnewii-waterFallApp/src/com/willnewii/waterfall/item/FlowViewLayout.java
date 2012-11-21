package com.willnewii.waterfall.item;

import java.io.File;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.willnewii.waterfallapp.R;
import com.youxilua.common.fetch.CacheFetch;

/**
 * 增加对Bitmap为空的处理.
 * 刚开始觉得应该添加Position. 实践,瀑布流是个乱序,完全没有必要. id记录就OK~
 */
public class FlowViewLayout extends RelativeLayout implements View.OnClickListener,
		View.OnLongClickListener {

	private Context context;
	/** 列数*/
	private int columnIndex;
	/** 行数*/
	private int rowIndex;
	/** 布局中-宽度*/
	private int ItemWidth;
	/** 当获取图片异常时,默认使用.*/
	private int ErrorImage ;
	
	private ImageView mImageView ;
	private TextView mTextView ;
	
	public ImageView getmImageView() {
		return mImageView;
	}

	public TextView getmTextView() {
		return mTextView;
	}

	public Bitmap bitmap;
	private String filePath;
	private Handler viewHandler;
	private onItemListener mItemListener ;

	public FlowViewLayout(Context c, AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		this.context = c;
		Init(null);
	}

	public FlowViewLayout(Context c, AttributeSet attrs) {
		super(c, attrs);
		this.context = c;
		Init(null);
	}

	/**
	 * 要显示的内容
	 * @param c
	 * @param content
	 */
	public FlowViewLayout(Context c , String content) {
		super(c);
		this.context = c;
		Init(content);
	}

	private void Init(String content) {

		RelativeLayout mRelativeLayout =  (RelativeLayout)LayoutInflater.from(context).inflate(R.layout.flowviewlayout, this, true);  
		mImageView = (ImageView)mRelativeLayout.findViewById(R.id.flowview_image);
		
		if(!TextUtils.isEmpty(content)){
			mTextView = (TextView)mRelativeLayout.findViewById(R.id.flowview_text);
			mTextView.setText(content);
			mTextView.setVisibility(View.VISIBLE);
		}
		
		setOnClickListener(this);
		setOnLongClickListener(this);
		
		//是否保持宽高比。需要与maxWidth、MaxHeight一起使用，否则单独使用没有效果
		//setAdjustViewBounds(true);

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
	 * 加载/重载图片
	 */
	public void LoadImage(boolean isReload) {
		new LoadImageThread(isReload).start();
	}
	
	/**
	 * 加载/重载图片
	 * 试用线程池
	 */
	public void LoadImage(ExecutorService mExecutorService  , final boolean isReload) {
		mExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					//SD卡不可用,显示默认图片.
					File cacheFile = CacheFetch.dowanLoadBitmap(getContext(),getFilePath());
					bitmap = BitmapFactory.decodeFile(cacheFile.getPath());
				} catch (Exception e) {
					bitmap = BitmapFactory.decodeResource(getResources(), ErrorImage);
				}
				if(bitmap == null)
					bitmap = BitmapFactory.decodeResource(getResources(), ErrorImage);
				// if (bitmap != null) {

				// 此处不能直接更新UI，否则会发生异常：CalledFromWrongThreadException: Only the original thread that. created a view hierarchy can touch its views.
				// 也可以使用Handler或者Looper发送Message解决这个问题

				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						if (bitmap != null) {// 此处在线程过多时可能为null
							if(isReload){//重载.
								getmImageView().setImageBitmap(bitmap);
							}else{
								int width = bitmap.getWidth();// 获取真实宽高
								int height = bitmap.getHeight();

								ViewGroup.LayoutParams lp = getLayoutParams();
								int layoutHeight = (height * getItemWidth()) / width;// 调整高度
								if (lp == null) {
									//原来的设置会出现不全宽的问题.  由于已经限定了该列的宽度,那么可以直接使用FILL_PARENT,更好维护.
									//lp = new LayoutParams(getItemWidth(), layoutHeight);
									lp = new LayoutParams(LayoutParams.FILL_PARENT, layoutHeight);
								}
								setLayoutParams(lp);
								
								//getmImageView().setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT));

								getmImageView().setImageBitmap(bitmap);
								Handler h = getViewHandler();
								Message m = h.obtainMessage(FlowViewHandler.HANDLER_FLOWVIEWLAYOUT,width, layoutHeight, FlowViewLayout.this);
								h.sendMessage(m);
							}
						}
					}
				});
				
			}
		});
	}


	/**
	 * 回收内存
	 */
	public void recycle() {
		getmImageView().setImageBitmap(null);
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

	public FlowViewLayout setViewHandler(Handler viewHandler) {
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
			try {
				//SD卡不可用,显示默认图片.
				File cacheFile = CacheFetch.dowanLoadBitmap(getContext(),getFilePath());
				bitmap = BitmapFactory.decodeFile(cacheFile.getPath());
			} catch (Exception e) {
				bitmap = BitmapFactory.decodeResource(getResources(), ErrorImage);
			}
			if(bitmap == null)
				bitmap = BitmapFactory.decodeResource(getResources(), ErrorImage);
			// if (bitmap != null) {

			// 此处不能直接更新UI，否则会发生异常：CalledFromWrongThreadException: Only the original thread that. created a view hierarchy can touch its views.
			// 也可以使用Handler或者Looper发送Message解决这个问题

			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					if (bitmap != null) {// 此处在线程过多时可能为null
						if(isReload){//重载.
							getmImageView().setImageBitmap(bitmap);
						}else{
							int width = bitmap.getWidth();// 获取真实宽高
							int height = bitmap.getHeight();

							ViewGroup.LayoutParams lp = getLayoutParams();
							int layoutHeight = (height * getItemWidth()) / width;// 调整高度
							if (lp == null) {
								//原来的设置会出现不全宽的问题.  由于已经限定了该列的宽度,那么可以直接使用FILL_PARENT,更好维护.
								//lp = new LayoutParams(getItemWidth(), layoutHeight);
								lp = new LayoutParams(LayoutParams.FILL_PARENT, layoutHeight);
							}
							setLayoutParams(lp);
							
							//getmImageView().setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT));

							getmImageView().setImageBitmap(bitmap);
							Handler h = getViewHandler();
							Message m = h.obtainMessage(FlowViewHandler.HANDLER_FLOWVIEWLAYOUT,width, layoutHeight, FlowViewLayout.this);
							h.sendMessage(m);
						}
					}
				}
			});
		}
	}
}
