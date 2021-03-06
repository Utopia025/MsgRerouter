package com.utopia025.msgrerouter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

public class PreRece extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action 	= intent.getAction();
		res 			= context.getResources();
		mContext		= context;
		number			= DEFAULT_NUMBER;
		delim 			= DEFAULT_DELIM;
		if (action.equals(FILTER_ACTION_RECEIVE_SMS)) {
		    Log.d(TAG, "In onRece of PreServiceReceiver; SMS has been received and is being screened");
		    // if message starts with start serv delim, go through message and pull number/delim if present
		    //  if not present will use default
		    String fromMsgArray[] = new ReRouterService().getMessageData(intent);
		    if (fromMsgArray[1].substring(0, 13).equals(START_SERVICE_DELIM)){
			   try {
		    	Log.d(TAG, "In on StartService of onRece; start service initiated");
			    String messageArray[] = fromMsgArray[1].split("\n");
			    // Search message for potential delims/numbers to set, if not found will use defaults
			    for (int i = 0; i < messageArray.length; i++) {
			    	if (messageArray[i].equals(SET_BASE_DELIM)) {
			    		number 	= messageArray[i + 1];
				    	Log.d(TAG, "Number pulled from message: " + number);
			    	} else if (messageArray[i].equals(SET_DELIM_DELIM)) {
			    		delim 	= messageArray[i + 1];
				    	Log.d(TAG, "Delim pulled from message: " + delim);
			    	}
			    }
			    startServing(number, delim);
			   } catch (Exception e) {
					System.out.println(e.toString());
			   }
		    }
		}
	}
	
	// Starts the service with specified delim and BA$E, also toggles button
	protected void startServing(String number2, String delim2) {
		Intent mIntent = new Intent(mContext, ReRouterService.class);
	    mIntent.putExtra(EXTRA_NUMBER, number2);
	    mIntent.putExtra(EXTRA_DELIM, delim2);
	    new ReRouterPreferences().toggleButtonTitle();
	    mContext.startService(mIntent);
		
	}

	private String number;
	private String delim;
	protected Resources res;
	protected Context mContext;
	public String DEFAULT_DELIM									= "qaw";//res.getString(R.string.delim).substring(5);
	public final static String EXTRA_NUMBER						= "com.utopia025.msgrerouter.NUM_STRING";
	public final static String EXTRA_DELIM						= "com.utopia025.msgrerouter.DELIM_STRING";
	public final static String TAG								= "$$PreRece$$";
	public final static String START_SERVICE_DELIM				= "::start_serv::";
	public final static String SET_BASE_DELIM					= "::set_base::";
	public final static String SET_DELIM_DELIM					= "::set_delim::";
	public final static String DEFAULT_NUMBER					= "6194174583";
	public final static String FILTER_ACTION_RECEIVE_SMS		= "android.provider.Telephony.SMS_RECEIVED";

}
