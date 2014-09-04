package cn.ismartv.speedtester.others;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.ismartv.speedtester.R;

import java.util.ArrayList;

public class DetailAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<NetworkSpeedInfo> mNetworkSpeedInfos;
	private Resources mResources;
	
	public DetailAdapter(Context context, ArrayList<NetworkSpeedInfo> networkSpeedInfos){
		mContext = context;
		mNetworkSpeedInfos = networkSpeedInfos;
		mResources = context.getResources();
	}
	public int getCount() {
		// TODO Auto-generated method stub
		return mNetworkSpeedInfos.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView!=null){
			holder = (ViewHolder) convertView.getTag();
		} else {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
			holder.engTitleText = (TextView) convertView.findViewById(R.id.eng_title_text);
			holder.engSpeedText = (TextView) convertView.findViewById(R.id.eng_speed_text);
			holder.engProgress = (ProgressBar) convertView.findViewById(R.id.eng_current_progress);
			holder.engProgress.setMax((int)mNetworkSpeedInfos.get(position).length);
			convertView.setTag(holder);
		}
		holder.engTitleText.setText(mNetworkSpeedInfos.get(position).title);
		if(mNetworkSpeedInfos.get(position).timeStarted>0){
		float currentSpeed = mNetworkSpeedInfos.get(position).speed;
			if(currentSpeed>0){
				String currentUnit = mResources.getString(R.string.speed_unit_kb);
				if(currentSpeed > 1024){
					currentSpeed = currentSpeed / 1024.0F;
					currentUnit = mResources.getString(R.string.speed_unit_mb);
				} else if( currentSpeed < 1) {
					currentSpeed = currentSpeed * 1024.0F;
					currentUnit = mResources.getString(R.string.speed_unit_byte);
				}
				currentSpeed = (float)((int)(currentSpeed * 100F))/100F;
				holder.engSpeedText.setText(currentSpeed+currentUnit);
			} else if(currentSpeed == -1) {
				holder.engSpeedText.setText(R.string.exception_time_out);
			} else if(currentSpeed == -2) {
				holder.engSpeedText.setText(R.string.exception_unknown);
			}
			if(holder.engProgress.getVisibility()!=View.VISIBLE){
				holder.engProgress.setVisibility(View.VISIBLE);
			}
			holder.engProgress.setProgress((int)mNetworkSpeedInfos.get(position).timeEscalpsed);
		}
		return convertView;
	}
	
	static class ViewHolder {
		TextView engTitleText;
		TextView engSpeedText;
		ProgressBar engProgress;
	}
}
