package com.xw.idld.aschwitkey.http;

public class Http {
	
    //阿希地址
    public static final String aschUrl = "https://exnode.asch.io";

    /**
     * 账户信息(通过登录根地址拼接)
     * post
     * json
     */
    public static final String Accounts = aschUrl + "/api/accounts/open2/";

    //创建新账户
    public static final String newAccount = aschUrl+"/api/accounts/new";

    //事务同步
    public static final String transactions = aschUrl+"/peer/transactions";

}
