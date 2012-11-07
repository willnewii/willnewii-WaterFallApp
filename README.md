WaterFall4android

dodola:https://github.com/dodola/android_waterfall
youxilua:https://github.com/youxilua/waterfall4android

修改自youxilua的项目.是不是一人一个看法...youxilua说dodola封装的不太好,他就
重新设计了一个,结构清晰了很多.读起来也非常顺.但又觉得youxilua的设计有点过渡封装...自己又做了些修改.添加了些可能用到的功能.

总的来说,dodola做了核心实现.youxilua给了一个更良好的框架.
我跟在后面,做了一点点修改,个人感觉用着会舒服一些.

谢谢两位大神提供的代码~

##修改##
1.所有可设置参数都统一放入WaterFallOption进行管理.
2.优化了边距的padding计算.
3.增加了每个Item的回调函数.之前那个不方便响应事件.
4.防止重复触发bottom事件.
5.添加了一个FlowViewLayout.就是ImageView+TextView.我想可能会有在图片上显示文字的需求.

##小问题##
项目的实现流程:获取数据-->WaterFall内部做加载.-->图片下载完成后通知更新UI和更新计数.

获取数据后不能及时显示,需要等待图片加载完成后才能进行操作.

在做公司项目时,获取的数据中已经提供了加载图片的宽/高.这样我就可以提前算出图片在WaterFall中的宽高,进行预加载.

##性能问题##
原理:主要依靠WaterFallUtils类 通过区分向上滑动/向下滑动,对bitmap及时的加载/释放 来实现批量图片的加载.所以理论上数据不停在增加,但是图片占用的内存是恒定的.

dodola说:
> 试过在1万张可以流畅的滑动，不出现内存溢出情况

##Bug##
测试结果,感觉不太理想.图片多了滑动还是会卡,而且最要命的是,加载大图还是会OOM.
所以,还是需要服务器端配合,打给优化过的图片.
