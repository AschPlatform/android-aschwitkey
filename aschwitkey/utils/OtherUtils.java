package com.xw.idld.aschwitkey.utils;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.LocaleList;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/8/30.
 */

public class OtherUtils {

    /**
     * 判断是否含其他特殊字符
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean checkzhongx(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }


    /**
     * 判断字符是不是英文包括中线
     *
     * @param eng
     * @return
     */
    public static boolean checkEnglishandxian(String eng) {
        String regex = "[a-zA-Z-]+";
        return eng.matches(regex);   //true：含有英文
    }


    /**
     * 判断字符是不是英文
     *
     * @param eng
     * @return
     */
    public static boolean checkEnglish(String eng) {
        String regex = "[a-zA-Z]+";
        return eng.matches(regex);   //true：含有英文
    }

    /**
     * 判断字符是不是英文或者是数字
     *
     * @param eng
     * @return
     */
    public static boolean checkEnglishornum(String eng) {
        String regex = "[a-zA-Z0-9]+";
        return eng.matches(regex);   //true：含有英文
    }

    //正则表达式
    public static boolean checkEmaile(String emaile) {
        /**
         *   ^匹配输入字符串的开始位置
         *   $结束的位置
         *   \转义字符 eg:\. 匹配一个. 字符  不是任意字符 ，转义之后让他失去原有的功能
         *   \t制表符
         *   \n换行符
         *   \\w匹配字符串  eg:\w不能匹配 因为转义了
         *   \w匹配包括字母数字下划线的任何单词字符
         *   \s包括空格制表符换行符
         *   *匹配前面的子表达式任意次
         *   .小数点可以匹配任意字符
         *   +表达式至少出现一次
         *   ?表达式0次或者1次
         *   {10}重复10次
         *   {1,3}至少1-3次
         *   {0,5}最多5次
         *   {0,}至少0次 不出现或者出现任意次都可以 可以用*号代替
         *   {1,}至少1次  一般用+来代替
         *   []自定义集合     eg:[abcd]  abcd集合里任意字符
         *   [^abc]取非 除abc以外的任意字符
         *   |  将两个匹配条件进行逻辑“或”（Or）运算
         *   [1-9] 1到9 省略123456789
         *    邮箱匹配 eg: ^[a-zA-Z_]{1,}[0-9]{0,}@(([a-zA-z0-9]-*){1,}\.){1,3}[a-zA-z\-]{1,}$
         *
         */
        String RULE_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        //正则表达式的模式 编译正则表达式
        Pattern p = Pattern.compile(RULE_EMAIL);
        //正则表达式的匹配器
        Matcher m = p.matcher(emaile);
        //进行正则匹配\
        return m.matches();
    }


    /**
     * 判断密码格式
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        String zengze = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$";
        return !str.matches(zengze);
    }

    /**
     * 判断是否含有特殊字符
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 显示动画
     *
     * @param view
     */
    public static void SHOWView(View view) {
        TranslateAnimation showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(500);
        view.startAnimation(showAnim);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏动画
     *
     * @param view
     */
    public static void HIDDView(View view) {
        TranslateAnimation hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnim.setDuration(500);
        view.startAnimation(hideAnim);
        view.setVisibility(View.GONE);
    }

    /**
     * 判断奇数（包括正负奇数）
     *
     * @param num
     * @return
     */
    public static boolean isOddNumber(int num) {
        return (num & 1) != 0;
    }


    /**
     * 隐藏电话号码中间4位
     *
     * @param mb
     * @return
     */
    public static String PhoneM(String mb) {
        if (mb.isEmpty()) {
            return "";
        } else {
            String mobile = mb.substring(0, 3) + "****" + mb.substring(7, 11);
            return mobile;
        }
    }

    /**
     * 检查网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        return true;
    }

    /**
     * 判断时候有相机权限
     *
     * @param constent
     * @return
     */
    public static boolean isCAMERA(Context constent) {
        PackageManager pm = constent.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", constent.getPackageName()));
        if (permission) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 判断手机号码格式
     *
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
        String regex = "^(?:(?:\\+|00)86)?1(?:(?:3[\\d])|(?:4[5-7|9])|(?:5[0-3|5-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[1|8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }

    /**
     * 将字符串转化为小写
     *
     * @param string
     * @return
     */
    public static String toLowerCase(String string) {
        return string.toLowerCase();
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static long DateToMil(String date) {
        long ts = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = simpleDateFormat.parse(date);
            ts = date1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;
    }

    /**
     * "yyyy-MM-dd HH:mm:ss"
     * 调用此方法输入所要转换的时间戳输入例如（1402733340）输出（"2014-06-14  16:09:00"）	 * 	 * @param time	 * @return
     */
    public static String timedate(long time,String pattern) {
        if (time == 0) {
            return "";
        }
        String result;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        result = simpleDateFormat.format(new Date(time));
        return result;
    }


    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }
    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    /**
     * 将时间转换为时间戳
     */
    public static long dateToStamp(String s) throws ParseException{
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        return ts;
    }

    /**
     * 沉浸式状态栏设置
     */
    public static void config(Activity activity) {
        //当系统版本为4.4或者4.4以上时可以使用沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


//    /**
//     * 服务器当前时间
//     *
//     * @return
//     */
//    public static long Time() {
//        long time = new Date().getTime();
//        long a = time + Constent.TIME;
//        return a;
//    }


    /**
     * 截取字符串
     *
     * @param str      源字符串
     * @param strStart 起始字符串
     * @param strEnd   结束字符串
     * @return
     */
    public String getInsideString(String str, String strStart, String strEnd) {
        if (str.indexOf(strStart) < 0) {
            return "";
        }
        if (str.indexOf(strEnd) < 0) {
            return "";
        }
        return str.substring(str.indexOf(strStart) + strStart.length(), str.indexOf(strEnd));
    }


    public static boolean isStrContains(String maxstr, String minstr) {
        boolean status = maxstr.contains(minstr);
        if (status) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取本地软件版本号
     */
    public static long getLocalVersion(Context ctx) {
        long localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 删除单个文件
     *
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.e("cxy", "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("cxy", "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }

    /**
     * 获取系统的locale
     */
    public static void getSystemLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        if (!locale.getLanguage().equals("en")) {
            if (!locale.getLanguage().equals("zh")) {
                Locale myLocale = new Locale("en");
                locale = myLocale;
            }
        }
        Resources res = context.getResources();// 获得res资源对象
        DisplayMetrics dm = res.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        Configuration config = res.getConfiguration();// 获得设置对象
        config.locale = locale;
        res.updateConfiguration(config, dm);
    }

    public static void switchLanguage(Context context, int type) {
        // 本地语言设置
        Locale myLocale;
        switch (type) {
            case 0:
                myLocale = new Locale("en");
                break;
            case 1:
            default:
                myLocale = new Locale("zh");
                break;
        }
        Resources res = context.getResources();// 获得res资源对象
        DisplayMetrics dm = res.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        Configuration config = res.getConfiguration();// 获得设置对象
        config.locale = myLocale;
        res.updateConfiguration(config, dm);
    }

    /**
     * 查询手机的 MCC+MNC
     */
    private static String getSimOperator(Context c) {
        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSimOperator();
        } catch (Exception e) {
            Log.e("cxy","sim异常："+e.getMessage());
            return null;
        }
    }


    /**
     * 因为发现像华为Y300，联想双卡的手机，会返回 "null" "null,null" 的字符串
     */
    private static boolean isOperatorEmpty(String operator) {
        if (operator == null) {
            return true;
        }


        if (operator.equals("") || operator.toLowerCase(Locale.ENGLISH).contains("null")) {
            return true;
        }


        return false;
    }


    /**
     * 判断是否是国内的 SIM 卡，优先判断注册时的mcc
     */
    public static boolean isChinaSimCard(Context c) {
        String mcc = getSimOperator(c);
        Log.e("cxy","mcc："+mcc);
        if (isOperatorEmpty(mcc)) {
            return true;
        } else {
            return mcc.startsWith("460");
        }
    }

    public static boolean isChinaLatLng(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.get(0).getCountryName().equals("中国") || addresses.get(0).getCountryName().equals("China")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("cxy", "判断是否国外经纬度报错：" + e.getMessage());
            return false;
        }
    }

    public static String getCountryCity(Context context, double lat, double lng, int type) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        if (!locale.getLanguage().equals("en")) {
            if (!locale.getLanguage().equals("zh")) {
                Locale myLocale = new Locale("en");
                locale = myLocale;
            }
        }
        Geocoder geocoder = new Geocoder(context, locale);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (type == 0) {
                return addresses.get(0).getCountryName();
            } else {
                return addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            Log.e("cxy", "获取国家或城市报错：" + e.getMessage());
            return "";
        }
    }

    //获取随机字符串
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(52);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 判断助记词
     * @param str
     * @return
     */
    public static boolean isPuBlick(String str){
        if (str.trim().isEmpty()){
            return false;
        }

        String a=str.trim().replace(" ","&");
        int count=0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i)=='&'){
                count++;
            }
        }
        if (count==11){
            Pattern p = Pattern.compile(".*\\d+.*");
            Matcher m = p.matcher(str);
            if (!m.matches()) {
                return true;
            }
        }
        return false;
    }

}
