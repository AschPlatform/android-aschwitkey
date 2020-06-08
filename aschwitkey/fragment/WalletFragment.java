package com.xw.idld.aschwitkey.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lihang.ShadowLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.activity.CustomActivity;
import com.xw.idld.aschwitkey.activity.ImportAccountActivity;
import com.xw.idld.aschwitkey.adapter.HistoryAdapter;
import com.xw.idld.aschwitkey.db.SQLUtils;
import com.xw.idld.aschwitkey.entity.DBHelperBean;
import com.xw.idld.aschwitkey.entity.EventMessageBean;
import com.xw.idld.aschwitkey.entity.HistoryBean;
import com.xw.idld.aschwitkey.http.Http;
import com.xw.idld.aschwitkey.http.OkHttpClient;
import com.xw.idld.aschwitkey.utils.AESUtil;
import com.xw.idld.aschwitkey.utils.DialogUtils;
import com.xw.idld.aschwitkey.utils.MyDialog;
import com.xw.idld.aschwitkey.utils.SPUtils;
import com.xw.idld.aschwitkey.utils.ToastUtil;
import com.xw.idld.aschwitkey.utils.ZXingUtils;
import com.xw.idld.aschwitkey.view.RecyclerViewNoBugLinearLayoutManager;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.widget.WheelView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WalletFragment extends NewLazyFragment implements OnRefreshListener, OnLoadMoreListener, HistoryAdapter.OnClickListenerFace {

    @BindView(R.id.mTextView_account)
    TextView mTextView_account;
    @BindView(R.id.mTextView_address)
    TextView mTextView_address;
    @BindView(R.id.mTextView_allMoney)
    TextView mTextView_allMoney;
    @BindView(R.id.mTextView_available)
    TextView mTextView_available;
    @BindView(R.id.mTextView_freeze)
    TextView mTextView_freeze;
    @BindView(R.id.mRefreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.mRecyclerView_history)
    RecyclerView mRecyclerView_history;
    @BindView(R.id.mRelativeLayout_noData)
    RelativeLayout mRelativeLayout_noData;
    @BindView(R.id.mShadowLayout_history)
    ShadowLayout mShadowLayout_history;
    @BindView(R.id.mRelativeLayout_copy)
    RelativeLayout mRelativeLayout_copy;

    private Activity mContext;
    private HistoryAdapter adapter;
    private RecyclerViewNoBugLinearLayoutManager layoutManager = null;
    private List<HistoryBean.History> list;
    private Gson gson;
    private SPUtils spUtils;
    private SPUtils spUtils1;
    private int offset = 1;
    private MyDialog myDialog;
    private int RESULT_PTYE_CAM;
    private Dialog dialog;
    private Dialog dialog1;
    private String address = "";
    private EditText mEditText_address;
    private boolean isClick = false, isSecondSecret = false;
    private double lockedBalance = 0d, totalBalance = 0d, usableBalance = 0d;
    private String secret;
    private WebView mWebView;
    private EditText mEditText_tradePassword, mEditText_secondSecret;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_wallet_layout;
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
        gson = new Gson();
        spUtils = new SPUtils(mContext, "AschWallet");
        spUtils1 = new SPUtils(mContext, "AschImport");
        list = new ArrayList<>();
        adapter = new HistoryAdapter(mContext, list);
        layoutManager = new RecyclerViewNoBugLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView_history.setLayoutManager(layoutManager);
        mRecyclerView_history.setAdapter(adapter);
        mRecyclerView_history.setNestedScrollingEnabled(false);
        adapter.setOnClickListenerFace(this);

        setPullRefresher();
        if (!spUtils1.getString("childAddress", "").isEmpty()) {
            getUserInfo();
        } else {
            if (spUtils.getBoolean("isDeal", false)) {
                ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
            } else {
                ToastUtil.showLong(mContext, "请先到账户中心设置交易密码");
            }
        }
    }

    private void getUserInfo() {
        List<DBHelperBean> beanList = SQLUtils.QuerySQLAll(mContext, "");
        for (int i = 0; i < beanList.size(); i++) {
            if (spUtils1.getString("childAddress", "").equals(beanList.get(i).getAddress())) {
                mRelativeLayout_copy.setVisibility(View.VISIBLE);
                mTextView_account.setText(beanList.get(i).getAccount());
                mTextView_address.setText(beanList.get(i).getAddress());
                secret = beanList.get(i).getSecret();
            }
        }
        myDialog = DialogUtils.createLoadingDialog(mContext, "加载中...");
        getBalance();
        getHistory();
    }

    @OnClick({R.id.mRelativeLayout_scan, R.id.mRelativeLayout_code, R.id.mRelativeLayout_import, R.id.mTextView_btn_switch, R.id.mRelativeLayout_copy, R.id.mTextView_btn_transfer})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mRelativeLayout_scan:
                if (!spUtils.getBoolean("isDeal", false)) {
                    ToastUtil.showLong(mContext, "请先到账户中心设置交易密码");
                    return;
                }
                if (spUtils1.getString("childAddress", "").isEmpty()) {
                    ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
                    return;
                }
                RESULT_PTYE_CAM = 1;
                cameraApply();
                break;
            case R.id.mRelativeLayout_code:
                if (!spUtils.getBoolean("isDeal", false)) {
                    ToastUtil.showLong(mContext, "请先到账户中心设置交易密码");
                    return;
                }
                if (spUtils1.getString("childAddress", "").isEmpty()) {
                    ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
                    return;
                }
                myQRCode();
                break;
            case R.id.mRelativeLayout_import:
                if (isClick) {
                    return;
                } else {
                    isClick = true;
                    if (!spUtils.getBoolean("isDeal", false)) {
                        isClick = false;
                        ToastUtil.showLong(mContext, "请先到账户中心设置交易密码");
                        return;
                    }
                    dialogAuthentication();
                }
                break;
            case R.id.mTextView_btn_switch:
                List<DBHelperBean> beanList = SQLUtils.QuerySQLAll(mContext, "");
                if (beanList.isEmpty()) {
                    ToastUtil.showLong(mContext, "当前没有账户可切换，请先到账户管理中导入或新建账户");
                } else {
                    List<String> items = new ArrayList<>();
                    for (int i = 0; i < beanList.size(); i++) {
                        items.add("[" + beanList.get(i).getAccount() + "]" + beanList.get(i).getAddress());
                    }
                    OptionPicker picker = new OptionPicker(mContext, items);
                    picker.setDividerRatio(WheelView.DividerConfig.WRAP);
                    picker.setShadowColor(getResources().getColor(R.color.bg_color3), 40);
                    picker.setCancelTextColor(getResources().getColor(R.color.text_bg));
                    picker.setSubmitTextColor(getResources().getColor(R.color.text_bg));
                    picker.setTopLineColor(getResources().getColor(R.color.white1));
                    picker.setLineSpaceMultiplier(4);
                    picker.setCycleDisable(true);
                    picker.setTextSize(11);
                    picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                        @Override
                        public void onOptionPicked(int index, String item) {
                            spUtils1.putString("childAddress", item.substring(item.indexOf("]") + 1, item.length()));
                            mTextView_address.setText(item.substring(item.indexOf("]") + 1, item.length()));
                            mTextView_account.setText(item.substring(1, item.indexOf("]")));
                            offset = 1;
                            mRefreshLayout.resetNoMoreData();
                            getUserInfo();
                        }
                    });
                    picker.show();
                }
                break;
            case R.id.mRelativeLayout_copy:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("address", mTextView_address.getText().toString());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showShort(mContext, "账户地址已复制到剪贴板");
                break;
            case R.id.mTextView_btn_transfer:
                if (!spUtils.getBoolean("isDeal", false)) {
                    ToastUtil.showLong(mContext, "请先到账户中心设置交易密码");
                    return;
                }
                if (spUtils1.getString("childAddress", "").isEmpty()) {
                    ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
                    return;
                }
                if (isClick) {
                    return;
                } else {
                    isClick = true;
                    transferDialog();
                }
                break;
        }
    }

    private void dialogAuthentication() {
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
                verifyAccount(mEditText_verification.getText().toString());
            }
        });

        dialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        dialog.setCancelable(false);
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

    private void verifyAccount(String password) {
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
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("code") == 1) {
                                dialog.dismiss();
                                Intent ia = new Intent(mContext, ImportAccountActivity.class);
                                ia.putExtra("isImport", true);
                                ia.putExtra("tradePassword", password);
                                startActivity(ia);
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

    private void setPullRefresher() {
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(mContext));
        mRefreshLayout.setRefreshFooter(new ClassicsFooter(mContext));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventMessageBean messageEvent) {
        switch (messageEvent.getTag()) {
            case 9:
                if (spUtils1.getString("childAddress", "").isEmpty()) {
                    mRelativeLayout_copy.setVisibility(View.GONE);
                    mTextView_account.setText("");
                    mTextView_address.setText("");
                    secret = "";
                    mTextView_allMoney.setText("0.000 xas");
                    mTextView_freeze.setText("0.000");
                    mTextView_available.setText("0.000");
                    list.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    getUserInfo();
                }
                break;
        }
    }

    private void transferDialog() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_transfer_layout, null);
        LinearLayout mLinearLayout_scan = v.findViewById(R.id.mLinearLayout_scan);
        mEditText_address = v.findViewById(R.id.mEditText_address);
        EditText mEditText_amount = v.findViewById(R.id.mEditText_amount);
        EditText mEditText_message = v.findViewById(R.id.mEditText_message);
        TextView mTextView_btn_cancel = v.findViewById(R.id.mTextView_btn_cancel);
        LinearLayout mLinearLayout_btn_subscribe = v.findViewById(R.id.mLinearLayout_btn_subscribe);
        TextView mTextView_balance = v.findViewById(R.id.mTextView_balance);

        mTextView_balance.setText(new DecimalFormat("0.000").format(usableBalance));
        mEditText_address.setText(address);
        mLinearLayout_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RESULT_PTYE_CAM = 2;
                cameraApply();
            }
        });

        mTextView_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick = false;
                dialog.dismiss();
            }
        });

        mLinearLayout_btn_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick = false;
                if (mEditText_address.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "收款地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mEditText_amount.getText().toString().trim().isEmpty() || Float.valueOf(mEditText_amount.getText().toString()) == 0f) {
                    Toast.makeText(mContext, "转账金额不能为空或0", Toast.LENGTH_SHORT).show();
                    return;
                }
                payDialog(mEditText_address.getText().toString(), Double.parseDouble(mEditText_amount.getText().toString()), mEditText_message.getText().toString());
            }
        });

        dialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        dialog.setContentView(v);
        dialog.setCancelable(false);
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

    private void payDialog(String address, double amount, String message) {
        View v1 = LayoutInflater.from(mContext).inflate(R.layout.dialog_transfer_trade_password_layout, null);
        TextView mTextView_currency = v1.findViewById(R.id.mTextView_currency);
        mEditText_tradePassword = v1.findViewById(R.id.mEditText_tradePassword);
        mEditText_secondSecret = v1.findViewById(R.id.mEditText_secondSecret);
        RelativeLayout mRelativeLayout_secondSecret = v1.findViewById(R.id.mRelativeLayout_secondSecret);
        if (isSecondSecret) {
            mRelativeLayout_secondSecret.setVisibility(View.VISIBLE);
        } else {
            mRelativeLayout_secondSecret.setVisibility(View.GONE);
        }
        mTextView_currency.setText(new DecimalFormat("0.000").format(amount) + " xas");
        v1.findViewById(R.id.mTextView_btn_transfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mEditText_tradePassword.getText().toString();
                String secondSecret = mEditText_secondSecret.getText().toString();
                transferWeb(address, amount, message, password, secondSecret);
            }
        });
        v1.findViewById(R.id.mImageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
            }
        });
        dialog1 = new Dialog(mContext, R.style.LeftDialogStyle);
        dialog1.setContentView(v1);
        dialog1.setCancelable(false);
        //获取当前Activity所在的窗体
        Window dialogWindow1 = dialog1.getWindow();
        if (dialogWindow1 == null) {
            return;
        }
        //设置Dialog从窗体底部弹出
        dialogWindow1.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp1 = dialogWindow1.getAttributes();
        lp1.dimAmount = 0.1f;
        //将属性设置给窗体
        lp1.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp1.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow1.setAttributes(lp1);
        dialog1.show();
    }

    private void transferWeb(String address, double amount, String message, String password, String secondSecret) {
        myDialog = DialogUtils.createLoadingDialog(mContext, "转账中...");
        mWebView = new WebView(getActivity());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setBlockNetworkLoads(false);
        mWebView.clearCache(true);
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        mWebView.addJavascriptInterface(javaScriptInterface, "stub");
        mWebView.loadUrl("file:///android_asset/transfer.html");


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                JsParseMainAssetTra(address, amount, message, password, secondSecret);
            }
        });
    }

    private void JsParseMainAssetTra(String address, double amount, String message, String password, String secondSecret) {
        String url = null;
        try {
            url = "javascript:stub.startFunction(dappTrans('" + AESUtil.decrypt(secret, password) + "'," + amount + ",'" + address + "','" + secondSecret + "','" + message + "'" + "));";
        } catch (Exception e) {
            mEditText_tradePassword.setText("");
            mEditText_secondSecret.setText("");
            DialogUtils.closeDialog(myDialog);
            ToastUtil.showShort(mContext, "交易密码错误");
            Log.e("cxy", "转账报错：" + e.getMessage(), e);
        }
        mWebView.loadUrl(url);
    }

    @Override
    public void OnClickTemp(int p, View view) {
        switch (view.getId()) {
            case R.id.mImageView_copy:
            case R.id.mTextView_address:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData;
                // 创建普通字符型ClipData
                if (list.get(p).getInOrout() == 1) {
                    mClipData = ClipData.newPlainText("address", list.get(p).getSenderId());
                } else {
                    mClipData = ClipData.newPlainText("address", list.get(p).getRecipientId());
                }
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showShort(mContext, "账户地址已复制到剪贴板");
                break;
        }
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void startFunction(String str) {
            transfer(str);//转账
        }
    }

    private void transfer(String str) {
        RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), str);
        OkHttpClient.initPost(Http.transactions, requestBodyPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditText_tradePassword.setText("");
                        mEditText_secondSecret.setText("");
                        DialogUtils.closeDialog(myDialog);
                        ToastUtil.showShort(mContext, "网络错误，转账失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", "转账返回：" + result);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mEditText_tradePassword.setText("");
                            mEditText_secondSecret.setText("");
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getBoolean("success")) {
                                DialogUtils.closeDialog(myDialog);
                                ToastUtil.showShort(mContext, "转账成功");
                                dialog1.dismiss();
                                dialog.dismiss();
                            } else {
                                DialogUtils.closeDialog(myDialog);
                                if (jsonObject.getString("error").contains("Invalid second signature")) {
                                    ToastUtil.showShort(mContext, "二级密码错误");
                                } else {
                                    ToastUtil.showShort(mContext, "转账失败，请稍候再试");
                                }
                            }
                        } catch (JSONException e) {
                            DialogUtils.closeDialog(myDialog);
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void getBalance() {
        OkHttpClient.initGet(Http.getAddressInfo + "?address=" + spUtils1.getString("childAddress")).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.closeDialog(myDialog);
                        ToastUtil.showShort(mContext, "网络错误，查询可用资产失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", result);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("code") == 1) {
                                isSecondSecret = jsonObject.getJSONObject("data").getBoolean("isHaveSecond");
                                totalBalance = jsonObject.getJSONObject("data").getDouble("totalBalance");
                                lockedBalance = jsonObject.getJSONObject("data").getDouble("lockedBalance");
                                usableBalance = jsonObject.getJSONObject("data").getDouble("usableBalance");
                                mTextView_allMoney.setText(new DecimalFormat("0.000").format(totalBalance) + " xas");
                                mTextView_freeze.setText(new DecimalFormat("0.000").format(lockedBalance));
                                mTextView_available.setText(new DecimalFormat("0.000").format(usableBalance));
                            } else {
                                DialogUtils.closeDialog(myDialog);
                                ToastUtil.showShort(mContext, jsonObject.getString("msg"));
                            }
                        } catch (JSONException e) {
                            DialogUtils.closeDialog(myDialog);
                            Log.e("cxy", "查询余额报错" + e.getMessage(), e);
                        }
                    }
                });
            }
        });
    }

    /**
     * 相机权限申请
     */
    private void cameraApply() {
        if (AndPermission.hasPermissions(mContext, Permission.CAMERA)) {
            startActivityForResult(new Intent(mContext, CustomActivity.class), RESULT_PTYE_CAM);
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.CAMERA)
                    .rationale(new Rationale<List<String>>() {
                        @Override
                        public void showRationale(Context context, List<String> data, final RequestExecutor executor) {
                            // 这里使用一个Dialog询问用户是否继续授权。
                            //初始化Builder给builder设置参数
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            //设置标题
                            builder.setTitle("权限已被拒绝");
                            //设置提示信息
                            builder.setMessage("您已经拒绝过我们申请授权，请您同意授权，否则功能无法正常使用");
                            //设置消极按钮监听事件
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 如果用户中断：
                                    executor.cancel();
                                }
                            });
                            //设置积极按钮监听事件
                            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 如果用户继续：
                                    executor.execute();
                                }
                            });
                            //将builder设置到AlertDialog中
                            AlertDialog dialog = builder.create();
                            //显示
                            dialog.show();
                        }
                    })
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> permissions) {
                            startActivityForResult(new Intent(mContext, CustomActivity.class), RESULT_PTYE_CAM);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> permissions) {
                            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
                            if (AndPermission.hasAlwaysDeniedPermission(mContext, permissions)) {
                                // 第一种：用默认的提示语。
                                ToastUtil.showLong(mContext, "您没有允许部分权限，请到设置界面开启权限");
                            }
                        }
                    })
                    .start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt("resultType") == 1) {
                    address = bundle.getString("result");
                    transferDialog();
                } else if (bundle.getInt("resultType") == 0) {
                    Toast.makeText(mContext, "解析二维码失败,请尽量保持二维码在正中心", Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == 2) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt("resultType") == 1) {
                    mEditText_address.setText(bundle.getString("result"));
//                    EventBus.getDefault().postSticky(new EventMessageBean(6, map));
                } else if (bundle.getInt("resultType") == 0) {
                    Toast.makeText(mContext, "解析二维码失败,请尽量保持二维码在正中心", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void myQRCode() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_qr_layout, null);
        ImageView mImageView_QRCode = inflate.findViewById(R.id.mImageView_QRCode);
        Bitmap bitmap = ZXingUtils.createQRImage(spUtils1.getString("childAddress"), getResources().getDimensionPixelSize(R.dimen.dp_160), getResources().getDimensionPixelSize(R.dimen.dp_160));
        mImageView_QRCode.setImageBitmap(bitmap);

        Dialog dialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        dialog.setContentView(inflate);
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

    private void getHistory() {
        Map map = new HashMap();
        map.put("address", spUtils1.getString("childAddress"));
        Map postmap = new HashMap();
        postmap.put("limit", 0);
        postmap.put("offset", 1);
        postmap.put("params", map);

        Log.e("cxy", "转账记录请求：" + gson.toJson(postmap));
        RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), gson.toJson(postmap));
        OkHttpClient.initPost(Http.getTransfers, requestBodyPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DialogUtils.closeDialog(myDialog);
                ToastUtil.showShort(mContext, "网络错误，转账记录查询失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", "转账记录：" + result);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DialogUtils.closeDialog(myDialog);
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("code") == 1) {
                                HistoryBean bean = gson.fromJson(jsonObject.getString("data"), HistoryBean.class);
                                if (!list.isEmpty()) {
                                    list.clear();
                                }
                                list.addAll(bean.getData());
                                if (!list.isEmpty()) {
                                    mShadowLayout_history.setVisibility(View.VISIBLE);
                                    mRelativeLayout_noData.setVisibility(View.GONE);
                                } else {
                                    mShadowLayout_history.setVisibility(View.GONE);
                                    mRelativeLayout_noData.setVisibility(View.VISIBLE);
                                }
                                adapter.notifyDataSetChanged();
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
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        if (spUtils1.getString("childAddress", "").isEmpty()) {
            refreshLayout.finishRefresh();
            ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getBalance();
                    Thread.sleep(1000 * 5);
                    Map map = new HashMap();
                    map.put("address", spUtils1.getString("childAddress"));
                    Map postmap = new HashMap();
                    postmap.put("limit", 0);
                    postmap.put("offset", 1);
                    postmap.put("params", map);

                    Log.e("cxy", "转账记录请求：" + gson.toJson(postmap));
                    RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), gson.toJson(postmap));
                    OkHttpClient.initPost(Http.getTransfers, requestBodyPost).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            refreshLayout.finishRefresh(1000);
                            ToastUtil.showShort(mContext, "网络错误，转账记录刷新失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            refreshLayout.finishRefresh(1000);
                            String result = response.body().string();
                            Log.e("cxy", "转账记录：" + result);
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        if (jsonObject.getInt("code") == 1) {
                                            offset = 1;
                                            HistoryBean bean = gson.fromJson(jsonObject.getString("data"), HistoryBean.class);
                                            list.clear();
                                            list.addAll(bean.getData());
                                            if (!list.isEmpty()) {
                                                mShadowLayout_history.setVisibility(View.VISIBLE);
                                                mRelativeLayout_noData.setVisibility(View.GONE);
                                            } else {
                                                mShadowLayout_history.setVisibility(View.GONE);
                                                mRelativeLayout_noData.setVisibility(View.VISIBLE);
                                            }
                                            adapter.notifyDataSetChanged();
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (spUtils1.getString("childAddress", "").isEmpty()) {
            refreshLayout.finishLoadMore();
            ToastUtil.showLong(mContext, "您还没有添加账户，请先到账户管理中导入或创建账户");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 5);
                    Map map = new HashMap();
                    map.put("address", spUtils1.getString("childAddress"));
                    Map postmap = new HashMap();
                    postmap.put("limit", 0);
                    postmap.put("offset", ++offset);
                    postmap.put("params", map);

                    Log.e("cxy", "转账记录请求：" + gson.toJson(postmap));
                    RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), gson.toJson(postmap));
                    OkHttpClient.initPost(Http.getTransfers, requestBodyPost).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            --offset;
                            refreshLayout.finishLoadMore();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            refreshLayout.finishLoadMore();
                            String result = response.body().string();
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        if (jsonObject.getInt("code") == 1) {
                                            HistoryBean bean = gson.fromJson(jsonObject.getString("data"), HistoryBean.class);
                                            if (bean.getData().isEmpty()) {
                                                refreshLayout.finishLoadMoreWithNoMoreData();
                                                return;
                                            }
                                            list.addAll(bean.getData());
                                            adapter.notifyDataSetChanged();
                                        } else if (jsonObject.getInt("code") == 3) {
                                            EventBus.getDefault().postSticky(new EventMessageBean(-2, null));
                                        } else {
                                            --offset;
                                            ToastUtil.showShort(mContext, jsonObject.getString("msg"));
                                        }
                                    } catch (JSONException e) {
                                        --offset;
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
