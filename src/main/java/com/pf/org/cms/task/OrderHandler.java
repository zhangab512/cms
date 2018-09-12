package com.pf.org.cms.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.SimpleFormatter;

/**
 * Created by zl on 2018-09-12.
 */
@Component
public class OrderHandler {
    private Jedis jedis = new Jedis("127.0.0.1",6379);
    private AtomicInteger currentOrderId = new AtomicInteger(0);
    private SimpleDateFormat sdForMatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String key ="orders";

    @Scheduled(cron="0/1 * * * * *")
    public void initData() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND,60);//取当前日期的后60s
        Map<String,Double> scores =new HashMap<>();
        for(int i=0;i<170;i++){
            currentOrderId.getAndIncrement();
            scores.put(currentOrderId.toString(),(double)removeMillis(cal));
            jedis.zadd(key,scores);
            System.out.print("当前订单ID:-----"+currentOrderId);
        }
    }

    private long removeMillis(Calendar cal) throws ParseException {
        String date =sdForMatter.format(cal.getTime());
        Date nowTime = sdForMatter.parse(date);
        System.out.println("------当前时间："+nowTime+"-----毫秒数"+nowTime.getTime());
        return nowTime.getTime();
    }

    @Scheduled(cron="0/1 * * * * *")
    public void consumer() throws ParseException {
        Calendar cal = Calendar.getInstance();
        long scores = removeMillis(cal);
        Set<String>orders =jedis.zrangeByScore(key,0,scores);
        if(orders.isEmpty()||orders.size()==0){
            System.out.println(String.format("==========暂时没有订单,时间：%s",cal.getTime()));
        }
        for(String order:orders){
            System.out.println("-----处理订单----"+order);
            long result = jedis.zrem(key,order);
            System.out.println("----处理完订单---"+order);
        }
    }

}
