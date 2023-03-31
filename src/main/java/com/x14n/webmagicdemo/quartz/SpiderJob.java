package com.x14n.webmagicdemo.quartz;


import com.x14n.webmagicdemo.config.RedisConfig;
import com.x14n.webmagicdemo.process.DiscussProcess;
import com.x14n.webmagicdemo.process.HotListProcess;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SpiderJob implements Job {
    public static String HotListUrl = "https://s.weibo.com/top/summary?cate=realtimehot";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public SpiderJob(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public SpiderJob(){}

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //热榜爬取任务
        // 先把先前爬取的list备份
        if (redisTemplate != null) {
            redisTemplate.delete("HotListBak");
            redisTemplate.delete("HotListUrlBak");
        }

        if (redisTemplate.opsForList().size("HotList") > 0) {
            redisTemplate.rename("HotList", "HotListBak");
            redisTemplate.rename("HotListUrl", "HotListUrlBak");
        }

        Spider.create(new HotListProcess(redisTemplate))
                .addUrl(HotListUrl)
                // .addPipeline(new ExcelPipeline())
                //.thread(5)  //表示开启5个线程来完成任务
                //设置布隆过滤器，最多对100w数据进行去重
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(1000 * 1000)))
                .run();

        List<String> hotListUrl = redisTemplate.opsForList().range("HotListUrl", 1, 50);

        log.info("开始进行详细内容爬取！");
        redisTemplate.delete("HotDetailBak");
        if (redisTemplate.opsForList().size("HotDetail") > 0) {
            redisTemplate.rename("HotDetail", "HotDetailBak");
        }
        long startTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);

        int maxTry = 3;
        for (int i = 0; i < hotListUrl.size(); i++) {
            Long hotDetailLen = redisTemplate.opsForList().size("HotDetail");
            // 因为有爬取失败的情况，所以当redis中详情的list的长度小于计数长度COUNT时候，说明上一条失败了
            if (hotDetailLen < count.get()) {
                if (maxTry > 0) {
                    log.info("第 " + count.get() + "失败，开始重新爬取该条！" + hotListUrl.get(count.get()));
                    i--;
                    count.getAndDecrement();
                    maxTry--;

                } else {
                    log.info("多次尝试失败，填充错误提示数据！");
                    redisTemplate.opsForList().rightPush("HotDetail", "[{'id': '99','content':'Error!'}]");
                }
            }
            count.getAndIncrement();
            Spider.create(new DiscussProcess(redisTemplate))
                    .addUrl(hotListUrl.get(i))
                    .thread(5)
                    .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(1000 * 1000)))
                    .run();
            log.info("第 " + count + " 条完成！");
        }

        log.info("详细内容爬取完成，耗时：" + (System.currentTimeMillis() - startTime) + " ms");
    }
}
