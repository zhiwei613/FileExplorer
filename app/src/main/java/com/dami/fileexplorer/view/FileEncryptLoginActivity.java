package com.dami.fileexplorer.view;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.content.Context;
import android.app.ActivityManager;
import android.os.CancellationSignal;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;

public class FileEncryptLoginActivity extends Activity implements OnClickListener {
	
	private EditText safebox_password_login;
	private Button safebox_password_login_btn;
	private Button safebox_password_cancel_btn;
	private String login;
	private String password_login;
	private ImageButton display_password_login_btn;
	private boolean isHiddenPassword=true;
	
	/** Fingerprint state: Not listening to fingerprint. */
	private static final int FINGERPRINT_STATE_STOPPED = 0;
	private static final int FINGERPRINT_STATE_RUNNING = 1;
	private static final int FINGERPRINT_STATE_CANCELLING = 2;
	private static final int FINGERPRINT_STATE_CANCELLING_RESTARTING = 3;
	private int mFingerprintRunningState = FINGERPRINT_STATE_STOPPED;
	private CancellationSignal mFingerprintCancelSignal;
	private FingerprintManager mFpm;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_encrypt_login);
		
		safebox_password_login = (EditText) findViewById(R.id.safebox_password_login);
		safebox_password_login_btn = (Button) findViewById(R.id.safebox_password_login_btn);
		display_password_login_btn = (ImageButton) findViewById(R.id.display_password_login_btn);
		safebox_password_cancel_btn = (Button) findViewById(R.id.safebox_password_cancel_btn);
		safebox_password_login_btn.setOnClickListener(this);
		display_password_login_btn.setOnClickListener(this);
		safebox_password_cancel_btn.setOnClickListener(this);
		
		mFpm = (FingerprintManager) this.getApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);
		updateFingerprintListeningState();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.safebox_password_login_btn:
				login = safebox_password_login.getText().toString();
				password_login = FileCategoryActivity.mDBBusiness.queryPassword(CommonUtils.currentAccount);
				if(password_login.equals(login)){
					Intent intent = new Intent(FileEncryptLoginActivity.this, FileEncryptCategoryActivity.class);
			    	startActivity(intent);
					updateFingerprintListeningState();
					FileEncryptLoginActivity.this.finish();
				}else{
					Toast.makeText(FileEncryptLoginActivity.this, R.string.password_login_toast, 0).show();
				}

            break;
		case R.id.display_password_login_btn:
			if(isHiddenPassword){
				safebox_password_login.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				display_password_login_btn.setImageResource(R.drawable.ic_password_visible);
			}else{
				safebox_password_login.setTransformationMethod(PasswordTransformationMethod.getInstance());
				display_password_login_btn.setImageResource(R.drawable.ic_password_invisible);
			}
			isHiddenPassword = !isHiddenPassword;
			safebox_password_login.postInvalidate();
            CharSequence charSequence = safebox_password_login.getText();
            if (charSequence instanceof Spannable) {
                Spannable spanText = (Spannable) charSequence;
                Selection.setSelection(spanText, charSequence.length());
            }
			break;
        case R.id.safebox_password_cancel_btn:
		    FileEncryptLoginActivity.this.finish();
		default:
			break;
		}
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		//if (!isChangingConfigurations()) {
			finish();
		//}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private FingerprintManager.AuthenticationCallback mAuthenticationCallback
			= new AuthenticationCallback() {

		@Override
		public void onAuthenticationFailed() {
			//handleFingerprintAuthFailed();
			Log.e("william", "onAuthenticationFailed " );
		};

		@Override
		public void onAuthenticationSucceeded(AuthenticationResult result) {
			//handleFingerprintAuthenticated();
			Log.e("william", "onAuthenticationSucceeded " + result);
			Intent intent = new Intent(FileEncryptLoginActivity.this, FileEncryptCategoryActivity.class);
			startActivity(intent);
		}

		@Override
		public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
			Log.e("william", "onAuthenticationHelp " + helpString);
			//handleFingerprintHelp(helpMsgId, helpString.toString());
			Toast.makeText(FileEncryptLoginActivity.this, helpString, 0).show();
		}

		@Override
		public void onAuthenticationError(int errMsgId, CharSequence errString) {
			//handleFingerprintError(errMsgId, errString.toString());
			Log.e("william", "onAuthenticationError " + errString);
		}

		@Override
		public void onAuthenticationAcquired(int acquireInfo) {
			//handleFingerprintAcquired(acquireInfo);
			Log.e("william", "onAuthenticationAcquired " + acquireInfo);
		}
	};

	private void updateFingerprintListeningState() {
		boolean shouldListenForFingerprint = true;//shouldListenForFingerprint();
		Log.e("william","updateFingerprintListeningState");
		if (mFingerprintRunningState == FINGERPRINT_STATE_RUNNING && shouldListenForFingerprint) {
			stopListeningForFingerprint();
		} else if (mFingerprintRunningState != FINGERPRINT_STATE_RUNNING
				&& shouldListenForFingerprint) {
			startListeningForFingerprint();
		}
	}

	public boolean isUnlockWithFingerprintPossible(int userId) {
		return mFpm != null && mFpm.isHardwareDetected() //&& !isFingerprintDisabled(userId)
				&& mFpm.getEnrolledFingerprints(userId).size() > 0;
	}

	private void startListeningForFingerprint() {
		if (mFingerprintRunningState == FINGERPRINT_STATE_CANCELLING) {
			//setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING_RESTARTING);
			return;
		}
		int userId = ActivityManager.getCurrentUser();
		if (isUnlockWithFingerprintPossible(userId)) {
			if (mFingerprintCancelSignal != null) {
				mFingerprintCancelSignal.cancel();
			}
			mFingerprintCancelSignal = new CancellationSignal();
			mFpm.authenticate(null, mFingerprintCancelSignal, 0, mAuthenticationCallback, null, userId);
			setFingerprintRunningState(FINGERPRINT_STATE_RUNNING);
		}
	}

	private void stopListeningForFingerprint() {
		if (mFingerprintRunningState == FINGERPRINT_STATE_RUNNING) {
			mFingerprintCancelSignal.cancel();
			mFingerprintCancelSignal = null;
			setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING);
		}
		if (mFingerprintRunningState == FINGERPRINT_STATE_CANCELLING_RESTARTING) {
			setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING);
		}
	}

	private void setFingerprintRunningState(int fingerprintRunningState) {
		boolean wasRunning = mFingerprintRunningState == FINGERPRINT_STATE_RUNNING;
		boolean isRunning = fingerprintRunningState == FINGERPRINT_STATE_RUNNING;
		mFingerprintRunningState = fingerprintRunningState;

		// Clients of KeyguardUpdateMonitor don't care about the internal state about the
		// asynchronousness of the cancel cycle. So only notify them if the actualy running state
		// has changed.
		if (wasRunning != isRunning) {
			//notifyFingerprintRunningStateChanged();
		}
	}

}
