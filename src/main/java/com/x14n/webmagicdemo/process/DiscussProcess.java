package com.x14n.webmagicdemo.process;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DiscussProcess implements PageProcessor {

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;
    private final Site site = CommonWebSite.getCommonWebSite();

    public DiscussProcess(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void process(Page page)
    {
        //css定位微博内容
        List<Selectable> list = page.getHtml().css("#pl_feedlist_index div.card-wrap").nodes();
        List<Map<String, String>> detailList = new ArrayList<>();

        //获取当前页面所有帖子
        for (Selectable crad : list)
        {
            int count = 1;
            Document doc = Jsoup.parse(crad.toString());
            Map<String, String> detailsMap = new HashMap<>(7);

            // 作者
            if (doc.select("a.name").first() != null)
            {
                detailsMap.put("author", doc.select("a.name").first().attr("nick-name"));

            }
            else
            {
                continue;
            }
            //获取时间
            if (doc.select("p[class=from]").first() != null)
            {
                detailsMap.put("time", doc.select("p[class=from]").first().text());
            }
            else
            {
                detailsMap.put("time", "");
            }

            //获取帖子图片
            if (doc.select("div[node-type=feed_list_media_prev]") == null || doc.select("div[node-type=feed_list_media_prev]").select("img[src~=(?i)\\.(png|jpe?g|gif)]") == null)
            {
                detailsMap.put("img", "0");
            }
            else
            {
                Elements imageUrlElements = doc.select("div[node-type=feed_list_media_prev]").select("img[src~=(?i)\\.(png|jpe?g|gif)]");
                List<String> imgUrls = new ArrayList<>();
                imageUrlElements.forEach( element -> {
                    imgUrls.add(element.attr("src"));
                });
                detailsMap.put("img", JSON.toJSONString(imgUrls));
            }
            String text = doc.select("p[node-type=feed_list_content]").text();
            String textFull = doc.select("p[node-type=feed_list_content_full]").text();
            String content = textFull.equals("") ? text : textFull;

            detailsMap.put("id", String.valueOf(count));
            detailsMap.put("contentUrl", crad.css("div[class=avator]").links().get());
            detailsMap.put("content", content);
            detailsMap.put("discussNum", doc.select("div[class=card-act]").text());
            detailList.add(detailsMap);
            count++;
        }
        redisTemplate.opsForList().rightPush("HotDetail", JSON.toJSONString(detailList));
    }

    @Override
    public Site getSite()
    {
        return site;
    }
}
