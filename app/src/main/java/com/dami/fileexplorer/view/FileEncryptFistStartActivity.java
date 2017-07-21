package com.dami.fileexplorer.view;

import com.dami.fileexplorer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FileEncryptFistStartActivity extends Activity {
	
	private Button safebox_first_start_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.file_encrypt_fist_start);
		
		safebox_first_start_btn = (Button) findViewById(R.id.safebox_first_start_btn);
		safebox_first_start_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(FileEncryptFistStartActivity.this, FileEncryptPasswordSettingActivity.class);
				startActivity(intent);
				FileEncryptFistStartActivity.this.finish();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
