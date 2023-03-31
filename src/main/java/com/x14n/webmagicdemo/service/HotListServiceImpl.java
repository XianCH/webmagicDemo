package com.x14n.webmagicdemo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Service
public class HotListServiceImpl implements HotListService {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public String getHotListJson() {
        List<String> hotList = redisTemplate.opsForList().range("HotList", 1, -1);
        return JSON.toJSONString(hotList);
    }

    @Override
    public String getHotDetailJson(int index) {
        List<String> hotDetail = redisTemplate.opsForList().range("HotDetail", 0, -1);
        List<Map<String, String>> list = JSON.parseObject(hotDetail.get(index - 1), new TypeReference<List<Map<String, String>>>() {});
        return JSON.toJSONString(list);
    }
}
