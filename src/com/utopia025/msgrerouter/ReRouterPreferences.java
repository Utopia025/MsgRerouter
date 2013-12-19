package com.utopia025.msgrerouter;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReRouterPreferences extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_re_router_preferences);
		
		setBroadcastListeners(); 
		
		// Find start button, set clickListener, and save number + send stats to service
		Button btnScheduleForwarding 	= (Button) findViewById(R.id.start_forwarding_service);
		btnScheduleForwarding.setOnClickListener(new View.OnClickListener() {
			// Perform your action here
			public void onClick(View v) {
				EditText number = (EditText) findViewById(R.id.forwarding_number);
				forwardingNumber = stripNumbers(number.getText().toString());
				if (validateNumber(forwardingNumber)) {
					// start service
					EditText delim = (EditText) findViewById(R.id.service_stopper);
					delimStr = delim.getText().toString();
					if (delimStr.equals("")) {
						String temp = getResources().getString(R.string.delim);
						delimStr = temp.substring(5);
					}
					Log.d(TAG, "In onClick, delimStr: '" + delimStr + "'");
					Log.d(TAG, "In onClick, number: " + forwardingNumber);
				    Intent mIntent = new Intent(getBaseContext(), ReRouterService.class);
				    mIntent.putExtra(EXTRA_NUMBER, forwardingNumber);
				    mIntent.putExtra(EXTRA_DELIM, delimStr);
				    startService(mIntent);
			    } else {
					// throw error message stating number is not correct
		            Toast.makeText(getBaseContext(), INVALID_NUMBER_MESSAGE, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	protected boolean validateNumber(String forwardingNumber2) {
		if (forwardingNumber2.length() == 10) {
			return true;
		}
		return false;
	}

	// Registers local listener to communicate with service (to toggle button text when
	// service is remotely killed) and SMS receiver to listen for remote service init
	private void setBroadcastListeners() {
		// Register listener for SMS as well as local listener
		LocalBroadcastManager.getInstance(this).registerReceiver(lbr, new IntentFilter(TOGGLE_BUTTON_NAME));
//		PreServiceReceiver pr = new PreServiceReceiver();
//		registerReceiver(pr, new IntentFilter(FILTER_ACTION_RECEIVE_SMS));
//	    Log.d(TAG, "BroadcastReceiver registered");
	}
	
	private final BroadcastReceiver lbr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TOGGLE_BUTTON_NAME)) {
			    Log.d(TAG, "In onRece of Local--BR; local toggle br has received local trigger");
				toggleButtonTitle();
			}	
		}
	};
	
	// Strips all non-digits from a number field
	protected String stripNumbers (String inputNumber) {
        String[] rex = inputNumber.split("\\D");
        String stripNum = "";
        for (int i = 0; i < rex.length; i++) {
                stripNum += rex[i];
        }
		Log.d(TAG, "In stripNum, stripNum: '" + stripNum + "'");
		return stripNum;
	}
	
	// change the button title to off or on
	public void toggleButtonTitle() {
		Button b = (Button) findViewById(R.id.start_forwarding_service);
		if (b.getText().toString().equals(getResources().getString(R.string.start_forward))) {
	        b.setText(getResources().getString(R.string.stop_forward));
            Toast.makeText(this, SERVICE_START_MESSAGE, Toast.LENGTH_SHORT).show();
		} else {
	        b.setText(getResources().getString(R.string.start_forward));
            Toast.makeText(this, SERVICE_STOP_MESSAGE, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.re_router_preferences, menu);
		return true;
	}
		
	
	
	public String forwardingNumber;
	public String delimStr;
	public final static String TAG								= "$$RRPrefs$$";
	public final static String EXTRA_NUMBER						= "com.utopia025.msgrerouter.NUM_STRING";
	public final static String EXTRA_DELIM						= "com.utopia025.msgrerouter.DELIM_STRING";
	public final static String TOGGLE_BUTTON_NAME				= "com.utopia025.msgrerouter.CHANGE_BUTTON_STRING";
	public final static String TOGGLE_CHANGE_INTENT_MESSAGE		= "changeButtonToggle";
	public final static String FILTER_ACTION_RECEIVE_SMS		= "android.provider.Telephony.SMS_RECEIVED";
	public final static String SERVICE_START_MESSAGE			= "Forwarding Service Initiated";
	public final static String SERVICE_STOP_MESSAGE				= "Forwarding Service Stopped";
	public final static String INVALID_NUMBER_MESSAGE			= "Uh oh! The entered number is not valid! please enter valid 10 digit number.";
}
