package com.dami.fileexplorer.view;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.xdja.business.DBBusiness;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class FileEncryptPasswordSettingActivity extends Activity implements OnClickListener {
	
	private EditText safebox_password_first,safebox_password_secound;
	private Button safebox_password_and_question_setting_btn;
	private String password_first,password_secound;
	private ImageButton display_password_setting_login_btn1, display_password_setting_login_btn2;
	private boolean isHiddenPassword1=true;
	private boolean isHiddenPassword2=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_encrypt_password_setting);
		
		safebox_password_first = (EditText) findViewById(R.id.safebox_password_first);
		safebox_password_secound = (EditText) findViewById(R.id.safebox_password_secound);
		safebox_password_and_question_setting_btn = (Button) findViewById(R.id.safebox_password_and_question_setting_btn);
		display_password_setting_login_btn1 = (ImageButton) findViewById(R.id.display_password_setting_login_btn1);
		display_password_setting_login_btn2 = (ImageButton) findViewById(R.id.display_password_setting_login_btn2);
		safebox_password_and_question_setting_btn.setOnClickListener(this);
		display_password_setting_login_btn1.setOnClickListener(this);
		display_password_setting_login_btn2.setOnClickListener(this);
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.safebox_password_and_question_setting_btn:
			password_first = safebox_password_first.getText().toString();
			password_secound = safebox_password_secound.getText().toString();
			Log.d("william","william FileEncryptPasswordSettingActivity password_first is:"+password_first);
			Log.d("william","william FileEncryptPasswordSettingActivity password_secound is:"+password_secound);
			if(password_first.equals(password_secound)){
				String pwd = FileCategoryActivity.mDBBusiness.queryPassword(CommonUtils.currentAccount);
	        	if(pwd == null){
	        		ContentValues account = new ContentValues();
			    	account.put("accountname", CommonUtils.currentAccount);
			        account.put("password", password_first);
			        FileCategoryActivity.mDBBusiness.insertPassword(account);
	        	}else{
	        		FileCategoryActivity.mDBBusiness.delete(CommonUtils.currentAccount);
	        		ContentValues account = new ContentValues();
			    	account.put("accountname", CommonUtils.currentAccount);
			        account.put("password", password_first);
			        FileCategoryActivity.mDBBusiness.insertPassword(account);
	        	}
		    	Intent intent = new Intent(FileEncryptPasswordSettingActivity.this, FileEncryptCategoryActivity.class);
		    	startActivity(intent);
				FileEncryptPasswordSettingActivity.this.finish();
				
			}else{
				Toast.makeText(FileEncryptPasswordSettingActivity.this, R.string.password_setting_toast, 0).show();
			}
			break;
		case R.id.display_password_setting_login_btn1:
			if(isHiddenPassword1){
				safebox_password_first.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				display_password_setting_login_btn1.setImageResource(R.drawable.ic_password_visible);
			}else{
				safebox_password_first.setTransformationMethod(PasswordTransformationMethod.getInstance());
				display_password_setting_login_btn1.setImageResource(R.drawable.ic_password_invisible);
			}
			isHiddenPassword1 = !isHiddenPassword1;
			safebox_password_first.postInvalidate();
            CharSequence charSequence1 = safebox_password_first.getText();
            if (charSequence1 instanceof Spannable) {
                Spannable spanText = (Spannable) charSequence1;
                Selection.setSelection(spanText, charSequence1.length());
            }
			break;
		case R.id.display_password_setting_login_btn2:
			if(isHiddenPassword2){
				safebox_password_secound.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				display_password_setting_login_btn2.setImageResource(R.drawable.ic_password_visible);
			}else{
				safebox_password_secound.setTransformationMethod(PasswordTransformationMethod.getInstance());
				display_password_setting_login_btn2.setImageResource(R.drawable.ic_password_invisible);
			}
			isHiddenPassword2 = !isHiddenPassword2;
			safebox_password_secound.postInvalidate();
            CharSequence charSequence2 = safebox_password_secound.getText();
            if (charSequence2 instanceof Spannable) {
                Spannable spanText = (Spannable) charSequence2;
                Selection.setSelection(spanText, charSequence2.length());
            }
			break;			
		default:
			break;
		}
	}

}
