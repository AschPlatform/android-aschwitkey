package com.xw.idld.aschwitkey.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lihang.ShadowLayout;
import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.activity.AccountSettingActivity;
import com.xw.idld.aschwitkey.activity.HelpActivity;
import com.xw.idld.aschwitkey.activity.InviteRewardsActivity;
import com.xw.idld.aschwitkey.activity.WalletManagementActivity;
import com.xw.idld.aschwitkey.entity.EventMessageBean;
import com.xw.idld.aschwitkey.utils.SPUtils;
import com.xw.idld.aschwitkey.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyFragment extends NewLazyFragment {

    @BindView(R.id.mTextView_username)
    TextView mTextView_username;
    @BindView(R.id.mTextView_address)
    TextView mTextView_address;
    @BindView(R.id.mLinearLayout_address)
    LinearLayout mLinearLayout_address;
    @BindView(R.id.mShadowLayout_inviteRewards)
    ShadowLayout mShadowLayout_inviteRewards;

    private Context mContext;
    private SPUtils spUtils;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_my_layout;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        super.initData();
        spUtils = new SPUtils(mContext, "AschWallet");
        String phone = spUtils.getString("phone");
        if (spUtils.getString("nickname").isEmpty()) {
            mTextView_username.setText(phone.substring(0, 3) + "****" + phone.substring(7, phone.length()));
        } else {
            mTextView_username.setText(spUtils.getString("nickname"));
        }
        if (spUtils.getBoolean("isCouncil", false)) {
            mShadowLayout_inviteRewards.setVisibility(View.GONE);
        }
        if (spUtils.getString("address", "").isEmpty()) {
            mLinearLayout_address.setVisibility(View.GONE);
        } else {
            mTextView_address.setText(spUtils.getString("address"));
        }
    }

    @OnClick({R.id.mRelativeLayout_inviteRewards, R.id.mLinearLayout_address, R.id.mRelativeLayout_accountSetting, R.id.mRelativeLayout_walletSetting, R.id.mRelativeLayout_help, R.id.mRelativeLayout_setting})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mRelativeLayout_inviteRewards:
                Intent intent = new Intent(mContext, InviteRewardsActivity.class);
                startActivity(intent);
                break;
            case R.id.mLinearLayout_address:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("address", mTextView_address.getText().toString());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showShort(mContext, "账户地址已复制到剪贴板");
                break;
            case R.id.mRelativeLayout_accountSetting:
                Intent as = new Intent(mContext, AccountSettingActivity.class);
                startActivity(as);
                break;
            case R.id.mRelativeLayout_walletSetting:
                Intent wm = new Intent(mContext, WalletManagementActivity.class);
                startActivity(wm);
                break;
            case R.id.mRelativeLayout_help:
                Intent help = new Intent(mContext, HelpActivity.class);
                startActivity(help);
                break;
            case R.id.mRelativeLayout_setting:
                ToastUtil.showShort(mContext, "开发中，请期待");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventMessageBean messageEvent) {
        switch (messageEvent.getTag()) {
            case 10:
                mTextView_username.setText(spUtils.getString("nickname"));
                break;
        }
    }


}
