package com.ctv.settings.base;

import android.app.Activity;

/**
 * 基础ViewHolder
 * @author wanghang
 * @date 2019/09/17
 */
public abstract class BaseViewHolder implements IBaseViewHolder {
    protected Activity mActivity;

    public BaseViewHolder(Activity activity) {
        this.mActivity = activity;
        initUI(activity);
        initData(activity);
        initListener();
    }

    @Override
    public void initData(Activity activity) {

    }


}
