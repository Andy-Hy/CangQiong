package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.List;

/**
 * @author admin
 * @date 2025/3/19 16:24
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;  //注入dishMapper
    @Autowired
    private DishFlavorMapper dishFlavorMapper;  //注入dishFlavorMapper
    @Autowired
    private SetmealDishMapper setmealDishMapper;  //注入setmealDishMapper

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transient  //事务注解  保证数据一致性
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);   //对象拷贝,将dishDTO 拷贝至dish

        //向菜品表插入1条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();    //获取口味List集合中的数据
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {     //遍历DishFlavor集合
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {//使用PageHelper插件实现分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());//开始分页
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品？
        for (Long id : ids) {   //循环获取id
            Dish dish = dishMapper.getById(id); //得到id传入dish
            if (dish.getStatus() == StatusConstant.ENABLE) {  //若当前dish值为1
                //当前菜品处于起售中，不可删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);//抛出不可删除的异常，提示不可删除信息交给前端显
            }
            //判断当前菜品是否能够删除---是否被套餐关联了？
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
            if(setmealIds != null && setmealIds.size() > 0){
                //当前套餐被关联了，不可删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);//同样使用抛异常方式，抛出不可删信息
            }
            //删除菜品表中的菜品数据
            for (Long id1 : ids) {
                dishMapper.deleteById(id1);
                //删除菜品关联的口味数据
                dishFlavorMapper.deleByDishId(id);
            }


        }
    }
}