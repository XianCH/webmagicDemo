package com.x14n.webmagicdemo.process;

import com.x14n.webmagicdemo.process.CommonWebSite;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

@Slf4j
public class HotListProcess implements PageProcessor {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private Site site = CommonWebSite.getCommonWebSite();

    public HotListProcess(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void process(Page page)
    {
        //当前获取了多少数据
        int count = 0;
        List<Selectable> list = page.getHtml().css("#pl_top_realtimehot td.td-02").nodes();

        //获取当前页面所有帖子
        for (Selectable crad : list)
        {
            Document doc = Jsoup.parse(crad.toString());
            //获取发帖人微博
            String contentUrl = crad.links().get();
            //获取帖子内容
            String text = doc.text();
            String content = text.equals("") ? "" : text;

            //写入redis
            redisTemplate.opsForList().rightPush("HotList", content);
            redisTemplate.opsForList().rightPush("HotListUrl", contentUrl);

            log.info(count + "  " + content + "  ");
            count++;
        }
    }

    @Override
    public Site getSite()
    {
        return site;
    }

}
