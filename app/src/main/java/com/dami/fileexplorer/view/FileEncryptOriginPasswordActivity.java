package com.dami.fileexplorer.view;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class FileEncryptOriginPasswordActivity extends Activity implements OnClickListener {
	
	private EditText safebox_origin_password;
	private Button safebox_origin_password_btn;
	private String origin;
	private ImageButton display_origin_password_login_btn;
	private boolean isHiddenPassword=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_encrypt_origin_password);
		
		safebox_origin_password = (EditText) findViewById(R.id.safebox_origin_password);
		safebox_origin_password_btn = (Button) findViewById(R.id.safebox_origin_password_btn);
		display_origin_password_login_btn = (ImageButton) findViewById(R.id.display_origin_password_login_btn);
		safebox_origin_password_btn.setOnClickListener(this);
		display_origin_password_login_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.safebox_origin_password_btn:
			origin = safebox_origin_password.getText().toString();
			String password_origin = FileCategoryActivity.mDBBusiness.queryPassword(CommonUtils.currentAccount);
			if(password_origin.equals(origin)){
				Intent intent = new Intent(FileEncryptOriginPasswordActivity.this, FileEncryptPasswordSettingActivity.class);
				startActivity(intent);
			}else{
				Toast.makeText(FileEncryptOriginPasswordActivity.this, R.string.password_modify_toast, 0).show();
			}
			break;
		case R.id.display_origin_password_login_btn:
			if(isHiddenPassword){
				safebox_origin_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				display_origin_password_login_btn.setImageResource(R.drawable.ic_password_visible);
			}else{
				safebox_origin_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
				display_origin_password_login_btn.setImageResource(R.drawable.ic_password_invisible);
			}
			isHiddenPassword = !isHiddenPassword;
			safebox_origin_password.postInvalidate();
            CharSequence charSequence = safebox_origin_password.getText();
            if (charSequence instanceof Spannable) {
                Spannable spanText = (Spannable) charSequence;
                Selection.setSelection(spanText, charSequence.length());
            }
			break;			

		default:
			break;
		}
	}

}
