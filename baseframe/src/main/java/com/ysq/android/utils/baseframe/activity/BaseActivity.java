package com.ysq.android.utils.baseframe.activity;

import android.app.Activity;

import com.ysq.android.utils.singleprogressdialog.SingleProgressDialog;
import com.ysq.android.utils.singletoast.YSingleToast;

public class BaseActivity extends Activity {

    /**
     * 用于显示Toast，立即取消上一个显示新的。
     */
    private YSingleToast mToast;

    /**
     * 显示模态圆形进度条
     */
	private SingleProgressDialog mProgressDialog;
    @Override
    protected void onStop() {
        super.onStop();
        if (mToast != null) {
            mToast.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToast = null;
        hideProgressDialog();
    }

    protected void showProgressDialog() {
        hideProgressDialog();
        if (mProgressDialog == null) {
            mProgressDialog = SingleProgressDialog.getInstance(this);
        }
        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    protected YSingleToast getSingleToast() {
        if (mToast == null) {
            mToast = YSingleToast.getInstance(this);
        }
        return mToast;
    }
}
