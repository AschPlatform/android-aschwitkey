package com.xw.idld.aschwitkey.http;



import com.xw.idld.aschwitkey.BuildConfig;
import com.xw.idld.aschwitkey.MyApplication;
import com.xw.idld.aschwitkey.activity.Content;
import com.xw.idld.aschwitkey.utils.DESUtil;
import com.xw.idld.aschwitkey.utils.OtherUtils;
import com.xw.idld.aschwitkey.utils.SPUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClient {

    public static okhttp3.OkHttpClient Call() {
        final SPUtils spUtils = new SPUtils(MyApplication.getContext(), "AschWallet");
        okhttp3.OkHttpClient.Builder okHttpClient = new okhttp3.OkHttpClient.Builder()
                .hostnameVerifier(new HttpsTrustManager.TrustAllHostnameVerifier())
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(20, TimeUnit.SECONDS)    //连接超时时间
                .readTimeout(200, TimeUnit.SECONDS)      //读取超时时间
                .addInterceptor(new Interceptor() { //自定义拦截器
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("secret", "1")
                                .addHeader("timestamp", "1")
                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0")
//                                .addHeader("Accept", "application/json")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("version","")
                                .addHeader("loginToken",spUtils.getString("loginToken",""))
                                .addHeader("sign",DESUtil.encryptResult(System.currentTimeMillis() + Content.timePoor))
                                .addHeader("versionCode",OtherUtils.getLocalVersionName(MyApplication.getContext()))
                                .addHeader("magic","5f5b3cf5")
                                .build();
                        return chain.proceed(request);
                    }
                });
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptornew = new HttpLoggingInterceptor();   //消息拦截器
            interceptornew.setLevel(HttpLoggingInterceptor.Level.BASIC);            //设置日志打印级别
            okHttpClient.addInterceptor(interceptornew);
        }
        return okHttpClient.build();
    }

    /**
     * 异步post请求
     *
     * @param POST_URL
     * @param requestBodyPost
     * @return
     */
    public static Call initPost(String POST_URL, RequestBody requestBodyPost) {
        Request request = new Request.Builder()
                .url(POST_URL)
                .post(requestBodyPost)
                .build();

        Call call = Call().newCall(request);
        return call;
    }

    /**
     * 异步get请求
     * @param url
     * @return
     */
    public static Call initGet(String url) {

        Request mRequest = new Request.Builder()
                .url(url)
                .build();
        Call mCall = Call().newCall(mRequest);
        return mCall;
    }

    public static Call sendMultipart(String urlAddress, RequestBody requestBody) {
        //这里根据需求传，不需要可以注释掉
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("title", "wangshu")
//                .addFormDataPart("image", "wangshu.jpg",
//                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))
//                .build();
//        private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        Request request = new Request.Builder()
                .url(urlAddress)
                .post(requestBody)
                .build();
        Call mCall = Call().newCall(request);
        return mCall;
    }
}
