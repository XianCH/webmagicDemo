package com.x14n.webmagicdemo.process;

import org.springframework.beans.factory.annotation.Value;
import us.codecraft.webmagic.Site;

public class CommonWebSite {

    private static String cookie;

    @Value("${spider.cookie}")
    public static void setCookie(String cookies) {
        cookie = cookies;
    }

    public static Site getCommonWebSite()
    {
        return Site.me()
                //设置编码
                .setCharset("utf8")
                //设置超时时间
                .setTimeOut(10 * 1000)
                //设置重试的间隔时间
                .setRetrySleepTime(3000)
                //设置重试的次数
                .setRetryTimes(3)
                //添加抓包获取的cookie信息
                .addCookie("s.weibo.com", cookie)
                //添加请求头，伪装浏览器请求
                .addHeader("User-Agent",
                        "ozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80" +
                                " Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8188.400")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate, sdch")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8")
                .addHeader("Connection", "keep-alive")
                .addHeader("Referer", "https://s.weibo.com");
    }
}