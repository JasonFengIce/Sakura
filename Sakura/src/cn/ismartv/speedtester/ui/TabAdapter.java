package cn.ismartv.speedtester.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.R;

import java.util.List;

/**
 * Created by huaijie on 14-10-29.
 */
public class TabAdapter extends BaseAdapter implements View.OnFocusChangeListener {
    private Context context;
    private List<Integer> list;

    private AnimationSet mAnimationSet;

    public TabAdapter(Context context, List<Integer> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_tab, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.tabIcon.setImageResource(list.get(position));
        view.setOnFocusChangeListener(this);
        return view;
    }

    @Override
    public void onFocusChange(View view, boolean focused) {
        Toast.makeText(context, "test", Toast.LENGTH_LONG).show();
        if (focused) {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2f, 1, 2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(500);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            view.startAnimation(animationSet);
//            mAnimationSet = animationSet;
        }
    }


    static class ViewHolder {
        @InjectView(R.id.tab_icon)
        ImageView tabIcon;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
