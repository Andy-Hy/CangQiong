package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 * @author admin
 * @date 2025/3/19 16:11
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;    //注入dishService

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping    //申明这是post请求
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){   //使用@RequestBody才能封装JSON格式数据
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult= dishService.pageQuery(dishPageQueryDTO); //调用dishService的pageQuery方法传入PagerQueryDTO内容，返回到PageResult
        return Result.success(pageResult);  // 返回值pageResult
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping  //申明删除请求方式
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){ //@RequestParam：使MVC动态解析字符串，并将List集合中的id提取出来
        log.info("菜品批量删除：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){//@PathVariable:固定参数
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop (@PathVariable Integer status,Long id){//@PathVariable:固定参数
        dishService.startOrStop(status,id); //使用startOrStop方法将接收的状态和ID传入Service层
        return Result.success();
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){//泛型List<Dish>返回菜品列表，categoryId参数用于接收分类ID
        List<Dish> list = dishService.list(categoryId);//调用dishService的业务逻辑层方法，传入分类ID参数，获取对应下的菜品数据集合
        return Result.success(list);
    }
}
