package com.dami.fileexplorer.view;

import java.util.ArrayList;

import com.dami.fileexplorer.R;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;


public class PathGallery
  extends LinearLayout
{
  private String mCurrentPath;
  private LayoutInflater mInflater = LayoutInflater.from(getContext());
  private IPathItemClickListener mPathItemClickListener;
  private ArrayList<Pair<String, String>> mPathSegments = new ArrayList();
  private int mPathStartIndex = 1;
  private View.OnClickListener pathItemClickListener = new View.OnClickListener()
  {
    @Override
	public void onClick(View paramAnonymousView)
    {
      String str = (String)paramAnonymousView.getTag();
      if (mPathItemClickListener != null) {
        mPathItemClickListener.onPathItemClickListener(str);
      }
    }
  };
  
  public PathGallery(Context paramContext)
  {
    super(paramContext, null);
  }
  
  public PathGallery(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
  }
  
  private void addPathSegmentViews()
  {
	  LinearLayout localLinearLayout = (LinearLayout)findViewById(R.id.scroll_container);
	  localLinearLayout.removeAllViews();
	  final HorizontalScrollView localHorizontalScrollView = (HorizontalScrollView)findViewById(R.id.path_scroll_view);
	  if (mPathSegments.size() > mPathStartIndex) {
		  for (int i = mPathStartIndex; i < mPathSegments.size(); i++) {
			  Pair localPair = mPathSegments.get(i);
			  TextView localTextView = (TextView)mInflater.inflate(R.layout.path_gallery_item, null, false);
			  localTextView.setText((CharSequence)localPair.first);
			  localTextView.setTag(localPair.second);
			  localTextView.setOnClickListener(pathItemClickListener);
			  localLinearLayout.addView(localTextView);
			  postDelayed(new Runnable() {
				  @Override
				public void run() {
					  if (localHorizontalScrollView != null) {
						  localHorizontalScrollView.fullScroll(View.FOCUS_RIGHT);
					  }
				  }
			  }, 100L);
		  }
	  }
  }
  
  private void initFirstPathView()
  {
    TextView localTextView = (TextView)findViewById(R.id.first_path);
    if ((mPathSegments.size() == 0) || (localTextView == null)) {
      return;
    }
    Pair localPair = mPathSegments.get(0);
    localTextView.setText((CharSequence)localPair.first);
    localTextView.setTag(localPair.second);
    localTextView.setOnClickListener(pathItemClickListener);
  }
  
  private void parsePathSegments() {
      if (!TextUtils.isEmpty(mCurrentPath)) {
          mPathSegments.clear();
          int i = 0;
          while (i < mCurrentPath.length()) {
              int indexOf = mCurrentPath.indexOf("/", i);
              if (indexOf >= 0) {
				  Pair pair = new Pair(mCurrentPath.substring(i, indexOf), mCurrentPath.substring(0, indexOf));
					mPathSegments.add(pair);  
                  //mPathSegments.add(new Pair(mCurrentPath.substring(i, indexOf), mCurrentPath.substring(0, indexOf)));
                  i = indexOf + 1;
              } else {
                  return;
              }
          }
      }
  }  
  
  public void setPath(String paramString)
  {
    mCurrentPath = (paramString + "/");
    parsePathSegments();
    initFirstPathView();
    addPathSegmentViews();
  }
  
  public void setPathItemClickListener(IPathItemClickListener paramIPathItemClickListener)
  {
    mPathItemClickListener = paramIPathItemClickListener;
  }
  
  public void setPathStartIndex(int paramInt)
  {
    mPathStartIndex = paramInt;
  }
  
  public static abstract interface IPathItemClickListener
  {
    public abstract void onPathItemClickListener(String paramString);
  }
}
