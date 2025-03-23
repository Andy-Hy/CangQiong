package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author admin
 * @date 2025/3/23 16:15
 */
@RestController("userShopCOntroller")//区分开不同包的
@RequestMapping("/user/shop")
@Api(tags ="店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";//定义常量，更加规范

    @Autowired
    private RedisTemplate redisTemplate;//注入Redis对象，使用Redis


    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("KEY");
        log.info("获取到店铺营业状态为：{}",status == 1 ? "营业中" : "打烊了");
        return Result.success(status);
    }
}
