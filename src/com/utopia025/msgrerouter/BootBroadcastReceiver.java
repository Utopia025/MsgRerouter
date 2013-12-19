package com.utopia025.msgrerouter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		PreServiceReceiver pr = new PreServiceReceiver();
//		context.registerReceiver(pr, new IntentFilter(FILTER_ACTION_RECEIVE_SMS));
		Log.d(TAG, "In BootBR OnRece, started the PreRece receiver");
	}

	public final static String FILTER_ACTION_RECEIVE_SMS		= "android.provider.Telephony.SMS_RECEIVED";
	public final static String TAG								= "$$BootBR$$";

}
