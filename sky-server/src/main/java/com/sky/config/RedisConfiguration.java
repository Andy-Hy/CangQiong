package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author admin
 * @date 2025/3/23 0:22
 */
@Configuration  //配置类注解
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){ //redis工厂对象
        log.info("开始创建Redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();//new一个redisTemplate对象
        //设置Redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);//传入连接工厂对象
        //设置解耦Redis Key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());//字符串类型的序列化器
        return redisTemplate;
    }
}
