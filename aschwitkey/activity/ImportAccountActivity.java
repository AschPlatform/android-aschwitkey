package com.xw.idld.aschwitkey.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.db.SQLUtils;
import com.xw.idld.aschwitkey.entity.CreateUserBean;
import com.xw.idld.aschwitkey.entity.DBHelperBean;
import com.xw.idld.aschwitkey.entity.EventMessageBean;
import com.xw.idld.aschwitkey.entity.UserBean;
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
import java.util.List;

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

public class ImportAccountActivity extends AppCompatActivity {

    @BindView(R.id.mTextView_bar)
    TextView mTextView_bar;
    @BindView(R.id.mTextView_title)
    TextView mTextView_title;
    @BindView(R.id.mEditText_note)
    EditText mEditText_note;
    @BindView(R.id.mEditText_mnemonicWord)
    EditText mEditText_mnemonicWord;
    @BindView(R.id.mTextView_btn_import)
    TextView mTextView_btn_import;

    private Context mContext;
    private WebView mWebView;
    private boolean isImport, isClick = false, isOk = false;
    private Gson gson;
    private String secret;
    private MyDialog myDialog;
    private String tradePassword;
    private SPUtils spUtils1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_account);
        ButterKnife.bind(this);
        OtherUtils.config(this);
        init();
    }

    private void init() {
        mContext = ImportAccountActivity.this;
        //设置沉浸式状态栏并且字体为黑色
        ViewUtils.setImmersionStateMode(ImportAccountActivity.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mTextView_bar.setHeight(ViewUtils.getStatusBarHeight(mContext));

        spUtils1 = new SPUtils(mContext, "AschImport");
        gson = new Gson();
        Intent intent = getIntent();
        isImport = intent.getBooleanExtra("isImport", true);
        tradePassword = intent.getStringExtra("tradePassword");
        if (isImport) {
            mTextView_title.setText("导入账户");
            mTextView_btn_import.setText("确认导入");
            mEditText_mnemonicWord.setVisibility(View.VISIBLE);
        } else {
            mTextView_title.setText("新建账户");
            mTextView_btn_import.setText("确认新建");
            mEditText_mnemonicWord.setVisibility(View.GONE);
        }

        mEditText_note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String note = mEditText_note.getText().toString();
                String reg = "^[0-9a-zA-Z_]{6,12}$";
                if (note.matches(reg)) {
                    isOk = true;
                } else {
                    isOk = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.mImageView_back, R.id.mTextView_btn_import})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mImageView_back:
                finish();
                break;
            case R.id.mTextView_btn_import:
                if(isClick){
                   return;
                }
                isClick = true;
                if (mEditText_note.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "账户备注不能为空", Toast.LENGTH_SHORT).show();
                    isClick = false;
                    return;
                }
                if (isImport) {
                    if (!isOk) {
                        Toast.makeText(mContext, "账户备注格式错误，请检查您的输入", Toast.LENGTH_SHORT).show();
                        isClick = false;
                        return;
                    }
                    if (mEditText_mnemonicWord.getText().toString().isEmpty()) {
                        Toast.makeText(mContext, "助记词不能为空", Toast.LENGTH_SHORT).show();
                        isClick = false;
                        return;
                    }
                    if (!OtherUtils.isPuBlick(mEditText_mnemonicWord.getText().toString())) {
                        Toast.makeText(mContext, "助记词格式不正确", Toast.LENGTH_SHORT).show();
                        isClick = false;
                        return;
                    }
                    secret = mEditText_mnemonicWord.getText().toString();
                    myDialog = DialogUtils.createLoadingDialog(mContext, "导入中...");
                    login();
                } else {
                    myDialog = DialogUtils.createLoadingDialog(mContext, "创建中...");
                    createAccount();
                }
                break;
        }
    }

    private void login() {
        mWebView = new WebView(ImportAccountActivity.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setBlockNetworkLoads(false);
        mWebView.clearCache(true);
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        mWebView.addJavascriptInterface(javaScriptInterface, "stub");
        mWebView.loadUrl("file:///android_asset/about.html");


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                JsParse(secret);
            }
        });
    }

    private void JsParse(String data) {
        String url = "javascript:stub.startFunction(encrypt('" + data + "'));";
        mWebView.loadUrl(url);
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void startFunction(String str) {
            doLoginQ(str);
        }
    }

    private void doLoginQ(String str) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("publicKey", str);
        } catch (JSONException e) {
            isClick = false;
            e.printStackTrace();
        }
        RequestBody requestBodyPost = FormBody.create(MediaType.parse("application/json"), jsonObject.toString());
        OkHttpClient.initPost(Http.Accounts, requestBodyPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isClick = false;
                        DialogUtils.closeDialog(myDialog);
                        ToastUtil.showShort(mContext, "网络错误，导入账户失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", "登录" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isClick = false;
                            DialogUtils.closeDialog(myDialog);
                            UserBean userBean = gson.fromJson(result, UserBean.class);
                            if (userBean.isSuccess()) {
                                boolean isThere = false;
                                List<DBHelperBean> bean = SQLUtils.QuerySQLAll(mContext, "");
                                for (int i = 0; i < bean.size(); i++) {
                                    if (bean.get(i).getAddress().equals(userBean.getAccount().getAddress())) {
                                        isThere = true;
                                    }
                                }
                                if (!isThere) {
                                    SQLUtils.AddSql(mContext, mEditText_note.getText().toString(), userBean.getAccount().getAddress(), AESUtil.encrypt(secret, tradePassword), "");
                                    ToastUtil.showShort(mContext, "导入成功");
                                    setResult(RESULT_OK);
                                    spUtils1.putString("childAddress", userBean.getAccount().getAddress());
                                    EventBus.getDefault().postSticky(new EventMessageBean(9, null));
                                    finish();
                                } else {
                                    ToastUtil.showShort(mContext, "该账户已导入，请勿重复导入");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("cxy", "登录报错：" + e.getMessage(), e);
                        }
                    }
                });
            }
        });
    }

    private void createAccount() {
        OkHttpClient.initGet(Http.newAccount).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isClick = false;
                        DialogUtils.closeDialog(myDialog);
                        ToastUtil.showShort(mContext, "网络错误，创建账户失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("cxy", "创建账户" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isClick = false;
                            DialogUtils.closeDialog(myDialog);
                            CreateUserBean createUserBean = gson.fromJson(result, CreateUserBean.class);
                            if (createUserBean.isSuccess()) {
                                SQLUtils.AddSql(mContext, mEditText_note.getText().toString(), createUserBean.getAddress(), AESUtil.encrypt(createUserBean.getSecret(), tradePassword), "");
                                ToastUtil.showShort(mContext, "创建成功");
                                setResult(RESULT_OK);
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e("cxy", "新建报错：" + e.getMessage(), e);
                        }
                    }
                });
            }
        });
    }

}
