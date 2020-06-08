package com.xw.idld.aschwitkey.activity;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.db.SQLUtils;
import com.xw.idld.aschwitkey.entity.DBHelperBean;
import com.xw.idld.aschwitkey.entity.EventMessageBean;
import com.xw.idld.aschwitkey.http.Http;
import com.xw.idld.aschwitkey.http.OkHttpClient;
import com.xw.idld.aschwitkey.utils.AESUtil;
import com.xw.idld.aschwitkey.utils.DialogUtils;
import com.xw.idld.aschwitkey.utils.MyDialog;
import com.xw.idld.aschwitkey.utils.OtherUtils;
import com.xw.idld.aschwitkey.utils.SPUtils;
import com.xw.idld.aschwitkey.utils.ToastUtil;
import com.xw.idld.aschwitkey.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecondSecretActivity extends AppCompatActivity {

    @BindView(R.id.mTextView_bar)
    TextView mTextView_bar;
    @BindView(R.id.mRelativeLayout_account)
    RelativeLayout mRelativeLayout_account;
    @BindView(R.id.mTextView_name)
    TextView mTextView_name;
    @BindView(R.id.mTextView_address)
    TextView mTextView_address;
    @BindView(R.id.mEditText_secondSecret)
    EditText mEditText_secondSecret;
    @BindView(R.id.mTextView_password)
    TextView mTextView_password;
    @BindView(R.id.mEditText_confirmPassword)
    EditText mEditText_confirmPassword;

    private Activity mContext;
    private SPUtils spUtils1;
    private List<DBHelperBean> list;
    private String secret;
    private WebView mWebView;
    private MyDialog myDialog;
    private Dialog dialog;
    private boolean isOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_secret);
        ButterKnife.bind(this);
        OtherUtils.config(this);
        init();
    }

    private void init() {
        mContext = SecondSecretActivity.this;
        //设置沉浸式状态栏并且字体为黑色
        ViewUtils.setImmersionStateMode(SecondSecretActivity.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mTextView_bar.setHeight(ViewUtils.getStatusBarHeight(mContext));

        spUtils1 = new SPUtils(mContext, "AschImport");
        list = new ArrayList<>();
        mRelativeLayout_account.setSelected(true);
        Intent intent = getIntent();
        list.addAll(SQLUtils.QuerySQLAll(mContext, ""));
        for (int i = 0; i < list.size(); i++) {
            if (!spUtils1.getString("childAddress", "").isEmpty()) {
                if (spUtils1.getString("childAddress", "").equals(list.get(i).getAddress())) {
                    try {
                        Log.e("cxy", "解密:" + list.get(i).getSecret() + intent.getStringExtra("tradePassword"));
                        secret = AESUtil.decrypt(list.get(i).getSecret(), intent.getStringExtra("tradePassword"));
                    } catch (Exception e) {
                        Log.e("cxy", "解密助记词报错：" + e.getMessage(), e);
                    }
                    mTextView_name.setText(list.get(i).getAccount());
                    mTextView_address.setText(list.get(i).getAddress());
                }
            }
        }

        mEditText_secondSecret.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String secondSecret = mEditText_secondSecret.getText().toString();
                String reg = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$";
                if (secondSecret.matches(reg)) {
                    isOk = true;
                    mTextView_password.setVisibility(View.GONE);
                } else {
                    isOk = false;
                    mTextView_password.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.mImageView_back, R.id.mTextView_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mImageView_back:
                finish();
                break;
            case R.id.mTextView_confirm:
                hintKeyBoard(view);
                secondSecretSetting();
                break;
        }
    }

    private void secondSecretSetting() {
        String secondSecret = mEditText_secondSecret.getText().toString();
        String confirmPassword = mEditText_confirmPassword.getText().toString();
        if (secondSecret.isEmpty()) {
            Toast.makeText(mContext, "二级密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (confirmPassword.isEmpty()) {
            Toast.makeText(mContext, "确认密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!secondSecret.equals(confirmPassword)) {
            Toast.makeText(mContext, "确认密码和二级密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isOk) {
            return;
        }
        myDialog = DialogUtils.createLoadingDialog(mContext, "请求中...");
        mWebView = new WebView(SecondSecretActivity.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setBlockNetworkLoads(false);
        mWebView.clearCache(true);
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        mWebView.addJavascriptInterface(javaScriptInterface, "stub");
        mWebView.loadUrl("file:///android_asset/secondSecret.html");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String url1 = "javascript:stub.startFunction(dappTrans('" + secret + "','" + secondSecret + "'));";
                mWebView.loadUrl(url1);
            }
        });
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void startFunction(String str) {
            broadcastingBlock(str);
        }
    }


    private void broadcastingBlock(String str) {
        RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), str);
        OkHttpClient.initPost(Http.transactions, requestBodyPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditText_confirmPassword.setText("");
                        mEditText_secondSecret.setText("");
                        DialogUtils.closeDialog(myDialog);
                        ToastUtil.showShort(mContext, "网络错误，设置二级密码失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", "二级密码返回：" + result);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getBoolean("success")) {
                                mEditText_confirmPassword.setText("");
                                mEditText_secondSecret.setText("");
                                EventBus.getDefault().postSticky(new EventMessageBean(9, null));
                                DialogUtils.closeDialog(myDialog);
                                dialogOk();
                            } else {
                                if(jsonObject.getString("error").contains("Error: Insufficient sender balance")){
                                    DialogUtils.closeDialog(myDialog);
                                    ToastUtil.showShort(mContext, "余额不足");
                                }else{
                                    DialogUtils.closeDialog(myDialog);
                                    ToastUtil.showShort(mContext, "设置二级密码失败，请稍候再试");
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

    private void dialogOk() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_second_secret_layout, null);
        TextView mTextView_ok = view.findViewById(R.id.mTextView_ok);
        mTextView_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

    /**
     * 隐藏软键盘
     */
    private void hintKeyBoard(View view) {
        InputMethodManager inputMgr = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
