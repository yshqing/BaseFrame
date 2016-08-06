package com.ysq.android.utils.baseframe.fragment;

import android.app.DialogFragment;

import com.ysq.android.utils.singleprogressdialog.SingleProgressDialog;
import com.ysq.android.utils.singletoast.YSingleToast;

/**
 * Created by ysq on 16/7/18.
 */
public class BaseDialogFragment extends DialogFragment {
    /**
     * 用于显示Toast，立即取消上一个显示新的。
     */
    private YSingleToast mToast;

    /**
     * 显示模态圆形进度条
     */
    private SingleProgressDialog mProgressDialog;
    @Override
    public void onStop() {
        super.onStop();
        if (mToast != null) {
            mToast.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToast = null;
        hideProgressDialog();
    }

    protected void showProgressDialog() {
        hideProgressDialog();
        if (mProgressDialog == null) {
            mProgressDialog = SingleProgressDialog.getInstance(getActivity());
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
            mToast = YSingleToast.getInstance(getActivity());
        }
        return mToast;
    }
}
