package com.xw.idld.aschwitkey.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.db.SQLUtils;
import com.xw.idld.aschwitkey.entity.DBHelperBean;
import com.xw.idld.aschwitkey.utils.AESUtil;
import com.xw.idld.aschwitkey.utils.OtherUtils;
import com.xw.idld.aschwitkey.utils.SPUtils;
import com.xw.idld.aschwitkey.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class BackupAccountActivity extends AppCompatActivity {

    @BindView(R.id.mTextView_bar)
    TextView mTextView_bar;
    @BindView(R.id.mRelativeLayout_account)
    RelativeLayout mRelativeLayout_account;
    @BindView(R.id.mTextView_name)
    TextView mTextView_name;
    @BindView(R.id.mTextView_address)
    TextView mTextView_address;
    @BindView(R.id.mTextView_confirm)
    TextView mTextView_confirm;

    private Context mContext;
    private SPUtils spUtils1;
    private List<DBHelperBean> list;
    private String secret, accountName, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_account);
        ButterKnife.bind(this);
        OtherUtils.config(this);
        init();
    }

    private void init() {
        mContext = BackupAccountActivity.this;
        //设置沉浸式状态栏并且字体为黑色
        ViewUtils.setImmersionStateMode(BackupAccountActivity.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mTextView_bar.setHeight(ViewUtils.getStatusBarHeight(mContext));

        spUtils1 = new SPUtils(mContext, "AschImport");
        list = new ArrayList<>();
        mRelativeLayout_account.setSelected(true);
        Intent intent = getIntent();
        list.addAll(SQLUtils.QuerySQLAll(mContext, ""));
        for (int i = 0; i < list.size(); i++) {
            DBHelperBean bean = list.get(i);
            if (!spUtils1.getString("childAddress", "").isEmpty()) {
                if (spUtils1.getString("childAddress", "").equals(list.get(i).getAddress())) {
                    try {
                        secret = AESUtil.decrypt(list.get(i).getSecret(),intent.getStringExtra("tradePassword"));
                    } catch (Exception e) {
                    }
                    accountName = list.get(i).getAccount();
                    address = list.get(i).getAddress();
                    mTextView_name.setText(list.get(i).getAccount());
                    mTextView_address.setText(list.get(i).getAddress());
                }
            } else {
                if (i == 0) {
                    bean.setSelect(true);
                } else {
                    bean.setSelect(false);
                }
            }
            list.set(i, bean);
        }
    }

    @OnClick({R.id.mImageView_back, R.id.mTextView_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mImageView_back:
                finish();
                break;
            case R.id.mTextView_confirm:
                Intent intent = new Intent(mContext,BackupAccountActivity2.class);
                intent.putExtra("secret",secret);
                intent.putExtra("accountName",accountName);
                startActivityForResult(intent,1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }
}
