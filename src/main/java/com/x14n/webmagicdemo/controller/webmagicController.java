package com.x14n.webmagicdemo.controller;


import com.x14n.webmagicdemo.quartz.QuartzManager;
import com.x14n.webmagicdemo.service.HotListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wemagic")
public class webmagicController {

    @Autowired
    HotListService hotListService;

    @Autowired
    QuartzManager quartzManager;

    @GetMapping("/getHotListJson")
    public String getHotListJson (){
        quartzManager.runJob();
        return hotListService.getHotListJson();
    }

    @GetMapping("/getHotDetailJson")
    public String getHotDetailJson(){
        return hotListService.getHotDetailJson(1);
    }

}
