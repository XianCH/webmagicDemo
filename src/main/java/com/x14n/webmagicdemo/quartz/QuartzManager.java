package com.x14n.webmagicdemo.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
public class QuartzManager {
    public void runJob()
    {
        JobDetail jobDetail = JobBuilder.newJob(SpiderJob.class).withIdentity("WeiboSpider", "Spider").build();

        //10分钟执行一次 一直执行
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group").startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(600).repeatForever()).build();

        // 定义一个 Schedule，用于绑定任务和触发器
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try
        {
            scheduler = sf.getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        }
        catch (SchedulerException e)
        {
            e.printStackTrace();
        }

    }
}