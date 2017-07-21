package com.dami.fileexplorer.xdja.utils;

import com.dami.fileexplorer.R;

import android.content.Context;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by xwy on 2016/8/12.
 */
public class ToastUtils extends Toast {

    public ToastUtils(Context context){
        super(context);
    }

    public static void makeText(Context context, String string) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast, null);
        TextView tv = (TextView) view.findViewById(R.id.toast_text);

        final float scale = context.getResources().getDisplayMetrics().density;

        Toast toast = new Toast(context);
        tv.setText(string);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, (int) (68 * scale + 0.5f));
        toast.show();
    }


}
