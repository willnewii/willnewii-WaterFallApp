package com.willnewii.waterfall;

import com.willnewii.waterfallapp.R;

/**
 * WaterFall的设置参数
 * 显示列数
 * 图片上限
 * 列宽度
 * ....
 * @author Will
 */
public class WaterFallOption {
	
	/** 列布局之间的间隙*/
	public int Column_Padding = 4 ;
	
	/** 显示列数  */
	public int Column_Count = 3;
	
	/** 图片加载上限  */
	public int MaxPictureCount = 1000;
	
	/** 列宽度  */
	public int Column_Width;
	
	/** Y轴灵敏度 
	 * 当距离底部还有多少的时候,触发onBootm事件.
	 * 之前为20.
	 * */
	public int mSensitivityY = 20;
	
	/** Y轴移动速率的分母.
	 * 默认不变.  
	 * 该参数意思,是想减慢滑动速度,可以减少加载压力？
	 * */
	public int VelocityY = 1;
	
	/**
	 * 当出现获取图片为空时使用该参数.
	 */
	public int ErrorImage = R.drawable.ic_launcher ;
	
	//用于handle 通讯的常量
	//消息发送的延迟时间
	public int message_delay = 200;

	public WaterFallOption(int itemWidth,int columnCount){
		this.Column_Width = itemWidth;
		this.Column_Count = columnCount;
	}
	
}
