package tv.ismar.sakura.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huaijie on 1/8/15.
 */
public class SakuraListView extends ListView {
    private static final String TAG = "SakuraListView";
    private int tempPositioin = -1;

    private int currentSelectedPosition;
    private View currentSelectedView;
//    private HomeActivityHoverBroadCastReceiver hoverBroadCastReceiver;


    public SakuraListView(Context context) {
        super(context);
//        registerBroadCastReceiver(context);
    }

    public SakuraListView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        registerBroadCastReceiver(context);
    }

    public SakuraListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        registerBroadCastReceiver(context);
    }


//    @Override
//    public boolean dispatchHoverEvent(MotionEvent event) {
//        int position = pointToPosition((int) event.getX(), (int) event.getY());
//
//        Log.i(TAG, "node list view: " + position);
////        if (AppConstant.DEBUG) {
////
////            Log.d(TAG, "list position is --->" + position);
////            Log.d(TAG, "hover event is --->" + event.getAction());
////        }
//        if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER && position != -1) || (event.getAction() == MotionEvent.ACTION_HOVER_MOVE && position != -1)) {
////            selectItem();
////            setMySelection(position);
////            selectItem(position);
//            if (currentSelectedPosition != position) {
//                currentSelectedPosition = position;
//            } else {
//                View itemView = getChildAt(currentSelectedPosition).findViewById(R.id.list_item_layout);
//                itemView.setBackgroundDrawable(null);
//                currentSelectedPosition = position;
//            }
//            View itemView = getChildAt(currentSelectedPosition).findViewById(R.id.list_item_layout);
//            itemView.setBackgroundResource(R.drawable.list_focused_holo);
////                currentSelectedPosition = position;
////            } else {
////            if (getChildAt(position) != null) {
////                View myitemView = getChildAt(position).findViewById(R.id.list_item_layout);
////                itemView.setBackgroundDrawable(null);
////            }
//        } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
//            if (getChildAt(position) != null) {
//                View itemView = getChildAt(position).findViewById(R.id.list_item_layout);
//                itemView.setBackgroundDrawable(null);
//            }
//        }
//
//
////                setMySelection(2);
////                getChildAt(position).setSelected(true);
////                setMySelection(position);
////                currentSelectedPosition = position;
//
////    }
//
////            setFocusable(true);
////            requestFocus();
////            setSelection(position);
////
////        } else {
////            clearFocus();
////
////        }
//        return true;
//    }

    private void selectItem(int position) {
        if (!isFocusable()) {
            setFocusableInTouchMode(true);
        }

        if (!isFocused()) {
            requestFocusFromTouch();
        }
        try {
            Method method = ListView.class.getDeclaredMethod("setSelectionInt", int.class);
            method.setAccessible(true);
            method.invoke(this, position);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setMySelection(int position) {
        if (!isFocusable()) {
            setFocusableInTouchMode(true);
        }

        if (!isFocused()) {
            requestFocusFromTouch();
        }
        setSelection(position);
//        setFocusable(true);

//        requestFocus();
//        getChildAt(position).setFocusable(true);
//        getChildAt(position).setSelected(true);
//        getChildAt(position).requestFocusFromTouch();
    }


    public void setSelectionOne() {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocusFromTouch();
        requestFocus();
        setSelection(0);
    }
//
//    private void registerBroadCastReceiver(Context context) {
//        hoverBroadCastReceiver = new HomeActivityHoverBroadCastReceiver();
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(HomeActivity.HOME_ACTIVITY_HOVER_ACTION);
//
//        context.registerReceiver(hoverBroadCastReceiver, intentFilter);
//    }

//    class HomeActivityHoverBroadCastReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(HomeActivity.HOME_ACTIVITY_HOVER_ACTION)) {
//                clearFocus();
//            }
//        }
//    }


}
