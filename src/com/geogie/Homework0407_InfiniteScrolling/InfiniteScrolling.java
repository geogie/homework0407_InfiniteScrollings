package com.geogie.Homework0407_InfiniteScrolling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Inte[i] IDEA.
 * User: renchaojun[FR]
 * Date:@date ${date}
 * Email:1063658094@qq.com
 */
public class InfiniteScrolling extends ScrollView{

    public InfiniteScrolling(Context context) {
        this(context, null);
    }

    public InfiniteScrolling(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfiniteScrolling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    /**
     * 瀑布流的列数
     */
    private int columnNumber ;

    /**
     * 每一列的布局对象
     */
    private List<LinearLayout> columnList ;

    /**
     * 每一列的长度
     */
    private List<Integer> columnLength ;
    /**
     * 回调函数
     */
    private Callback callback ;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    private void init(Context context, AttributeSet attrs) {

        // 瀑布流默认是三列
        columnNumber = 3 ;

        columnList = new ArrayList<LinearLayout>() ;
        columnLength = new ArrayList<Integer>() ;

        // 构造一个线性布局，用于存放瀑布流布局
        LinearLayout layoutContainer = new LinearLayout(context) ;

        // 设置layoutContainer的宽高
        layoutContainer.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        // 设置layoutContainer的排列方式
        layoutContainer.setOrientation(LinearLayout.HORIZONTAL);

        addView(layoutContainer);
        // 构造出columnNumber个瀑布流
        for (int i = 0; i < columnNumber; i++) {
            LinearLayout layoutContent = new LinearLayout(context) ;

            // 设置layoutContent的排列方式
            layoutContent.setOrientation(LinearLayout.VERTICAL);
            // 设置layoutContent的长宽和比重
            layoutContent.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            300,
                            1));

            layoutContainer.addView(layoutContent);
            columnList.add(layoutContent);
            columnLength.add(0);

            switch(i) {
                case 0:
                    layoutContent.setBackgroundColor(Color.GREEN);
                    break ;
                case 1:
                    layoutContent.setBackgroundColor(Color.YELLOW);
                    break ;
                case 2:
                    layoutContent.setBackgroundColor(Color.BLUE);
                    break ;
            }
        }
    }
    public void addImage(Bitmap bitmap) {
        if (bitmap != null) {
            int index = getShortestLayoutIndex() ;
            LinearLayout layout = columnList.get(index) ;

            // 计算在imageView中显示的图片的大小
            int imageWidth = bitmap.getWidth() ;
            int imageHeight = bitmap.getHeight() ;
            int standredWidth = getContext().getResources().getDisplayMetrics().widthPixels / columnNumber ;
            int standredHeight = imageHeight * standredWidth / imageWidth ;

            ImageView imageView = new ImageView(getContext()) ;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(standredWidth, standredHeight));
            imageView.setImageBitmap(bitmap);

            // 将imageView添加到流布局里面
            layout.addView(imageView);

            // 增加改列的长度
            columnLength.set(index, columnLength.get(index) + standredHeight);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean success = super.onTouchEvent(ev);

        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                success = true ;
                break;
            case MotionEvent.ACTION_MOVE:
                if(callback != null && isBottom()) {
                    callback.isBottom();
                }

                break;
            default:
                return success ;
        }
        return success ;
    }

    private boolean isBottom() {
        return getHeight() + getScrollY() == getMeasuredHeight() ;
    }

    private int getShortestLayoutIndex() {
        int index = 0 ;
        int min_length = columnLength.get(0);
        for(int i = columnNumber - 1; i > 0; i --) {
            if(columnLength.get(i) < min_length) {
                index = i ;
                min_length = columnLength.get(i) ;
            }
        }
        return index ;
    }

    public interface Callback {
        void isBottom() ;
    }
}
