package com.utopia025.msgrerouter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;


public class ReRouterService extends Service {

	
	@Override
	public void onCreate() {
	    Log.d(TAG, "In onCreate of Adventure Service" );
	    isRunning = false;
//		PreServiceReceiver psr = new PreServiceReceiver();
//		registerReceiver(psr, new IntentFilter(FILTER_ACTION_RECEIVE_SMS));
//		unregisterReceiver(psr);
//		Log.d(TAG, "Stopped running PreServRece");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    baseNumber = intent.getStringExtra(EXTRA_NUMBER);
	    baseDelim = intent.getStringExtra(EXTRA_DELIM);
	    Log.d(TAG, "In onStartCommand, number: " + baseNumber + " delim: " + baseDelim);
	    
	    if (isRunning == true) {
	    	stopServing();
		    Log.d(TAG, "In onStartCommand if statement, killing Service");
	    } else if (isRunning == false) {
		    IntentFilter iFilter = getIntentFilter();
		    registerReceiver(br, iFilter);
	    	fireToggleChange();
		    isRunning = true;
		    Log.d(TAG, "In onStartCommand if statement, service started");
	    }
	    
	    // We want this service to continue running until it is explicitly stopped, so return sticky.
	    return START_STICKY;
	}
	
	private IntentFilter getIntentFilter() {
		IntentFilter iFilter = new IntentFilter();
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		iFilter.addAction(FILTER_ACTION_QUIT_SERVICE);
		iFilter.addAction(FILTER_ACTION_RECEIVE_SMS);
		return iFilter;
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void stopServing() {
		stopSelf();
		unregisterReceiver(br);
		isRunning = false;
    	fireToggleChange();
//		registerReceiver(new PreServiceReceiver(), new IntentFilter(FILTER_ACTION_RECEIVE_SMS));
	    Log.d(TAG, "Stopped service");
	}
	
	private final BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(FILTER_ACTION_RECEIVE_SMS)) {
	            //action for receiving SMS
			    Log.d(TAG, "In onRece of SMS Rece--BR; SMS has been received and is being screened");
			    boolean isBaseNumber = checkMessageParameters(intent);
			    Log.d(TAG, "isBaseNumber = " + isBaseNumber);
			    if (isBaseNumber) {
			    	sendForwardedBaseMessage(intent);
			    } else {
			    	forwardMessageToBase(intent);
			    }
			}  		
		}
		
		// Push all incoming messages (coming from Ba$e) to specified message number
		private void sendForwardedBaseMessage(Intent intent) {
			String[] incomingMessageDataArr = getMessageData(intent);
			String check = incomingMessageDataArr[1];
		    Log.d(TAG, "In sendForwardedBaseMessage, baseDelim= '" + baseDelim + "' check = '" + check + "'");
			if (check.equals(baseDelim)) {
		    	Log.d(TAG, "In sendForwardedBaseMessage, message matched baseDelim, stopping service remotely");
		    	stopServing();
		    	fireToggleChange();
			} else {
				String[] sentMessage = incomingMessageDataArr[1].split("::");
				if (sentMessage.length > 1 && !sentMessage[1].equals(baseNumber)) {
					// Send message to specified number
					sendMessage(sentMessage[1], sentMessage[2]);
				} else {
					// No number entered send back to BA$E
					sendMessage(baseNumber, INVALID_NUMBER_MESSAGE + "\n" + incomingMessageDataArr[1]);
				}
			}
		}
		
		// Push all incoming messages (not coming from Ba$e) from RUNNER to Ba$e
		private void forwardMessageToBase(Intent intent) {
			String[] incomingMessageDataArr = getMessageData(intent);
			String fowardMessage = "*PUSH UPDATE*\n" + incomingMessageDataArr[0] + "\n" + incomingMessageDataArr[1];
			sendMessage(baseNumber, fowardMessage);
		}
		
		// Send the specified message to the specified number
		private void sendMessage(String number, String message) {
			try {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(number, null, message, null, null);
			    Log.d(TAG, "In sendMessage, message sent successfully");
			} catch (Exception e) {	
				Log.d(TAG, "In sendMessage, SMS faild");
				e.printStackTrace();
			}			
		}

		// Check if the received message is from the Ba$e
		private boolean checkMessageParameters(Intent intent) {
			// Parse the SMS.
			Bundle extras = intent.getExtras();
	        SmsMessage[] msgs = null;
	        String fromNumber = "";
	        if (extras != null) {
	        	//	Get originating address from message and return true if matches BA$E
	            Object[] pdus = (Object[]) extras.get("pdus");
	            msgs = new SmsMessage[pdus.length];
                msgs[0] = SmsMessage.createFromPdu((byte[])pdus[0]);
                fromNumber = getFormattedNumber(msgs[0].getOriginatingAddress());
			    Log.d(TAG, "In checkMessageParameters, pulled num: " + fromNumber);
                if (fromNumber.equals(baseNumber)) {
                	return true; 
                } else {
                	return false;
                }
	        }
			return false;
		}
	};
	
	// parses incoming message, builds message, and returns an array with [incoming number, incoming message]
	public String[] getMessageData(Intent intent) {
		// Parse the SMS.
		Bundle extras = intent.getExtras();
        SmsMessage[] msgs = null;
        String messageString = "";
        if (extras != null) {
            // Retrieve the SMS; protocol description unit (PDU- the industry format for an SMS message);
        	//	A large message might be broken into many, which is why it is an array of objects
            Object[] pdus = (Object[]) extras.get("pdus");
            msgs = new SmsMessage[pdus.length];
		    Log.d(TAG, "In getMessageData of SMS Rece Workflow (BR); pdu/msgs #: " + pdus.length);
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                // In case of a particular App / Service.
                //if(msgs[i].getOriginatingAddress().equals("+91XXX"))
                //{
                messageString += msgs[i].getMessageBody().toString();
                //}
            }
        }
        String origAddress = getFormattedNumber(msgs[0].getOriginatingAddress());
        String[] messageData = {origAddress, messageString};
		return messageData;
	}
			
	// Get correctly formatted number of message
	public String getFormattedNumber (String phoneNumber) {
		if (Character.toString(phoneNumber.charAt(0)).equals("+")) {
			return phoneNumber.substring(2);
        } else {
        	return phoneNumber;
        }
	}
	
	protected void fireToggleChange() {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(TOGGLE_BUTTON_NAME));
	    Log.d(TAG, "In fireToggleChange, sent toggle local broadcast");
	}
	
	
	private String baseNumber;
	private String baseDelim;
	private boolean isRunning;
	public final static String TAG								= "$$Serv$$";
	public final static String EXTRA_NUMBER						= "com.utopia025.msgrerouter.NUM_STRING";
	public final static String EXTRA_DELIM						= "com.utopia025.msgrerouter.DELIM_STRING";
	public final static String FILTER_ACTION_QUIT_SERVICE 		= "com.utopia025.msgrerouter.quitService";
	public final static String FILTER_ACTION_RECEIVE_SMS		= "android.provider.Telephony.SMS_RECEIVED";
	public final static String TOGGLE_BUTTON_NAME				= "com.utopia025.msgrerouter.CHANGE_BUTTON_STRING";
	public final static String TOGGLE_BUTTON_VALUE				= "changeButtonToggle";
	public final static String INVALID_NUMBER_MESSAGE				= "~~Number not provided or number == BA$E~~";
	
}
