package com.xw.idld.aschwitkey.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.utils.OtherUtils;
import com.xw.idld.aschwitkey.utils.ToastUtil;
import com.xw.idld.aschwitkey.utils.ViewUtils;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackupAccountActivity2 extends AppCompatActivity {

    @BindView(R.id.mTextView_bar)
    TextView mTextView_bar;
    @BindView(R.id.mTextView_secret)
    TextView mTextView_secret;

    private Context mContext;
    private String secret;
    private String accountName;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_account2);
        ButterKnife.bind(this);
        OtherUtils.config(this);
        init();
    }

    private void init() {
        mContext = BackupAccountActivity2.this;
        //设置沉浸式状态栏并且字体为黑色
        ViewUtils.setImmersionStateMode(BackupAccountActivity2.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mTextView_bar.setHeight(ViewUtils.getStatusBarHeight(mContext));

        Intent intent = getIntent();
        secret = intent.getStringExtra("secret");
        accountName = intent.getStringExtra("accountName");
        mTextView_secret.setSelected(true);
        mTextView_secret.setText(secret);
    }

    @OnClick({R.id.mImageView_back, R.id.mTextView_secret, R.id.mTextView_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mImageView_back:
                finish();
                break;
            case R.id.mTextView_secret:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("secret", secret);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showShort(mContext, "助记词已复制到剪贴板");
                break;
            case R.id.mTextView_confirm:
                dialogOk();
//                Intent intent = new Intent(mContext, BackupAccountActivity3.class);
//                intent.putExtra("secret", secret);
//                intent.putExtra("accountName", accountName);
//                startActivity(intent);
                break;
        }
    }

    private void dialogOk() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_secret_layout, null);
        TextView mTextView_ok = view.findViewById(R.id.mTextView_ok);
        mTextView_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
        dialog = new Dialog(mContext, R.style.inputDialog);
        dialog.setContentView(view);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow == null) {
            return;
        }
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.1f;
        //将属性设置给窗体
        lp.width = getResources().getDisplayMetrics().widthPixels - OtherUtils.dp2px(mContext, 60);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

}
