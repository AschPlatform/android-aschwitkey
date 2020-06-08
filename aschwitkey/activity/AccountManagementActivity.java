package com.xw.idld.aschwitkey.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.adapter.AccountAdapter;
import com.xw.idld.aschwitkey.db.SQLUtils;
import com.xw.idld.aschwitkey.entity.DBHelperBean;
import com.xw.idld.aschwitkey.entity.EventMessageBean;
import com.xw.idld.aschwitkey.http.Http;
import com.xw.idld.aschwitkey.http.OkHttpClient;
import com.xw.idld.aschwitkey.utils.AESUtil;
import com.xw.idld.aschwitkey.utils.OtherUtils;
import com.xw.idld.aschwitkey.utils.SPUtils;
import com.xw.idld.aschwitkey.utils.ToastUtil;
import com.xw.idld.aschwitkey.utils.ViewUtils;
import com.xw.idld.aschwitkey.view.RecyclerViewNoBugLinearLayoutManager;
import com.xw.idld.aschwitkey.view.SlideRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountManagementActivity extends AppCompatActivity implements AccountAdapter.OnClickListenerFace {


    @BindView(R.id.mTextView_bar)
    TextView mTextView_bar;
    @BindView(R.id.mRecyclerView_account)
    SlideRecyclerView mRecyclerView_account;

    private Context mContext;
    private RecyclerViewNoBugLinearLayoutManager layoutManager = null;
    private List<DBHelperBean> list;
    private AccountAdapter adapter;
    private SPUtils spUtils;
    private SPUtils spUtils1;
    private String inAddress = "";
    private Dialog dialog;
    private boolean isClick = false;
    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        ButterKnife.bind(this);
        OtherUtils.config(this);
        init();
    }

    private void init() {
        mContext = AccountManagementActivity.this;
        //设置沉浸式状态栏并且字体为黑色
        ViewUtils.setImmersionStateMode(AccountManagementActivity.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mTextView_bar.setHeight(ViewUtils.getStatusBarHeight(mContext));

        gson = new Gson();
        spUtils = new SPUtils(mContext, "AschWallet");
        spUtils1 = new SPUtils(mContext, "AschImport");
        list = new ArrayList<>();
        adapter = new AccountAdapter(mContext, list);
        layoutManager = new RecyclerViewNoBugLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView_account.setLayoutManager(layoutManager);
        mRecyclerView_account.setAdapter(adapter);
        adapter.setOnClickListenerFace(this);

        list.addAll(SQLUtils.QuerySQLAll(mContext, ""));
        for (int i = 0; i < list.size(); i++) {
            DBHelperBean bean = list.get(i);
            if (!spUtils1.getString("childAddress", "").isEmpty()) {
                inAddress = spUtils1.getString("childAddress", "");
                if (spUtils1.getString("childAddress", "").equals(list.get(i).getAddress())) {
                    bean.setSelect(true);
                } else {
                    bean.setSelect(false);
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
        if (!list.isEmpty()) {
            adapter.notifyDataSetChanged();
        }
    }

    @OnClick({R.id.mImageView_back, R.id.mTextView_btn_importAccount, R.id.mTextView_btn_createAccount})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mImageView_back:
                if (!inAddress.equals(spUtils1.getString("childAddress", ""))) {
                    EventBus.getDefault().postSticky(new EventMessageBean(9, null));
                }
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.mTextView_btn_importAccount:
                dialogAuthentication(true);
                break;
            case R.id.mTextView_btn_createAccount:
                dialogAuthentication(false);
                break;
        }
    }

    private void dialogAuthentication(boolean isImport) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_authentication_layout, null);
        EditText mEditText_verification = v.findViewById(R.id.mEditText_verification);
        TextView mTextView_btn_cancel = v.findViewById(R.id.mTextView_btn_cancel);
        TextView mTextView_confirm = v.findViewById(R.id.mTextView_confirm);

        mTextView_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick = false;
                dialog.dismiss();
            }
        });

        mTextView_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick = false;
                if (mEditText_verification.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "交易密码不能为空", Toast.LENGTH_SHORT).show();
                }
                hintKeyBoard(v);
                verifyAccount(mEditText_verification.getText().toString(), isImport);
            }
        });

        dialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        dialog.setContentView(v);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow == null) {
            return;
        }
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.1f;
        //将属性设置给窗体
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

    private void verifyAccount(String password, boolean isImport) {
        Map postmap = new HashMap();
        try {
            postmap.put("password", AESUtil.encrypt(spUtils.getString("phone"), password));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), gson.toJson(postmap));
        OkHttpClient.initPost(Http.verifyAccount, requestBodyPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtil.showShort(mContext, "网络错误，身份验证失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("code") == 1) {
                                dialog.dismiss();
                                if (isImport) {
                                    Intent ia = new Intent(mContext, ImportAccountActivity.class);
                                    ia.putExtra("isImport", true);
                                    ia.putExtra("tradePassword", password);
                                    startActivityForResult(ia, 1);
                                } else {
                                    Intent ia1 = new Intent(mContext, ImportAccountActivity.class);
                                    ia1.putExtra("isImport", false);
                                    ia1.putExtra("tradePassword", password);
                                    startActivityForResult(ia1, 1);
                                }
                            } else {
                                ToastUtil.showShort(mContext, jsonObject.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void OnClickTemp(int p, View view) {
        switch (view.getId()) {
            case R.id.mRelativeLayout_account:
                spUtils1.putString("childAddress", list.get(p).getAddress());
                for (int i = 0; i < list.size(); i++) {
                    DBHelperBean bean = list.get(i);
                    if (i == p) {
                        bean.setSelect(true);
                    } else {
                        bean.setSelect(false);
                    }
                    list.set(i, bean);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.mTextView_delete:
                dialogDelete(p);
                break;
        }
    }

    private void dialogDelete(int p) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_delete_layout, null);
        TextView mTextView_false = view.findViewById(R.id.mTextView_false);
        TextView mTextView_true = view.findViewById(R.id.mTextView_true);
        mTextView_false.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        mTextView_true.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLUtils.DaleteSql(mContext, list.get(p).getAddress());
                dialog.dismiss();
                ToastUtil.showShort(mContext, "删除成功");
                list.clear();
                list.addAll(SQLUtils.QuerySQLAll(mContext, ""));
                if (!list.isEmpty()) {
                    spUtils1.putString("childAddress", list.get(0).getAddress());
                } else {
                    spUtils1.putString("childAddress", "");
                }
                for (int i = 0; i < list.size(); i++) {
                    DBHelperBean bean = list.get(i);
                    if (!spUtils1.getString("childAddress", "").isEmpty()) {
                        if (spUtils1.getString("childAddress", "").equals(list.get(i).getAddress())) {
                            bean.setSelect(true);
                        } else {
                            bean.setSelect(false);
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
                adapter.notifyDataSetChanged();
                mRecyclerView_account.closeMenu();
                EventBus.getDefault().postSticky(new EventMessageBean(9, null));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (!list.isEmpty()) {
                list.clear();
            }
            list.addAll(SQLUtils.QuerySQLAll(mContext, ""));
            for (int i = 0; i < list.size(); i++) {
                DBHelperBean bean = list.get(i);
                if (i == 0) {
                    bean.setSelect(true);
                } else {
                    bean.setSelect(false);
                }
                list.set(i, bean);
            }
            spUtils1.putString("childAddress", list.get(0).getAddress());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (!inAddress.equals(spUtils1.getString("childAddress", ""))) {
            EventBus.getDefault().postSticky(new EventMessageBean(9, null));
        }
        setResult(RESULT_OK);
        finish();
    }

    /**
     * 隐藏软键盘
     */
    private void hintKeyBoard(View view) {
        InputMethodManager inputMgr = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
