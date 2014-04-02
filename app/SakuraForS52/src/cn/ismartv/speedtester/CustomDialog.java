package cn.ismartv.speedtester;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog extends Dialog {

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	
	public static class Builder {
		private Context mContext;
		private String mMessage;
		private String mPositiveButtonText;
		private String mNegativeButtonText;
		private DialogInterface.OnClickListener mPositiveListener;
		private DialogInterface.OnClickListener mNegativeListener;
		
		public Builder(Context context){
			mContext = context;
		}
		
		public Builder setMessage(String message){
			mMessage = message;
			return this;
		}
		
		public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener){
			mPositiveButtonText = text;
			mPositiveListener = listener;
			return this;
		}
		
		public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener){
			mNegativeButtonText = text;
			mNegativeListener = listener;
			return this;
		}
		
		public CustomDialog create(){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomDialog dialog = new CustomDialog(mContext, R.style.Dialog);
			View layout = inflater.inflate(R.layout.alert_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			if(mPositiveButtonText!=null){
				((Button)layout.findViewById(R.id.positive_button)).setText(mPositiveButtonText);
			}
			((Button)layout.findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					mPositiveListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				}
			});
			((Button)layout.findViewById(R.id.positive_button)).setOnHoverListener(mButtonHoverListener);
			((Button)layout.findViewById(R.id.positive_button)).setOnFocusChangeListener(mOnFocusChangeListener);
			if(mNegativeButtonText!=null){
				((Button)layout.findViewById(R.id.negative_btn)).setText(mNegativeButtonText);
			}
			((Button)layout.findViewById(R.id.negative_btn)).setOnHoverListener(mButtonHoverListener);
			((Button)layout.findViewById(R.id.negative_btn)).setOnFocusChangeListener(mOnFocusChangeListener);
			if(mNegativeListener!=null){
				((Button)layout.findViewById(R.id.negative_btn)).setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						mNegativeListener.onClick(dialog, BUTTON_NEGATIVE);
					}
				});
			}
			if(mMessage!=null){
				((TextView)layout.findViewById(R.id.alert_info_text)).setText(mMessage);
			}
			dialog.setContentView(layout);
			return dialog;
		}
		
		private OnHoverListener mButtonHoverListener = new OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_HOVER_ENTER:
					v.setSelected(true);
					v.requestFocusFromTouch();
					break;
				case MotionEvent.ACTION_HOVER_EXIT:
					v.setSelected(false);
					break;
				case MotionEvent.ACTION_HOVER_MOVE:
					if(!v.isSelected()) {
						v.setSelected(true);
						v.requestFocusFromTouch();
					}
					break;
				}
				return false;
			}
		};
		
		private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				v.setSelected(hasFocus);
			}
		};
	}
	
	
}
