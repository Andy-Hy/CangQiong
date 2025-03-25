package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.ArrayList;
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
    @Autowired
    private SetmealMapper setmealMapper;    //注入setmealMapper

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Transactional  //事务注解  保证数据一致性
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
    @Transactional  //事务注解，保证数据的一致性
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
                dishFlavorMapper.deleteByDishId(id);
            }
            //性能优化,改为批量删除
            //根据菜品id集合批量删除菜品数据
            //sql: delete from dish where id in (?,?,?)
//           dishMapper.deleteByIds(ids);

            //根据菜品id集合批量删除关联的口味数据
            //sql: delete from dish_flavor where dish_id in (?,?,?)
//            dishFlavorMapper.deleteByDishIds(ids);


        }
    }

    /**
     * 根据id查询菜品对应口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);//因为口味是多种，所以用集合接收
        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);//属性拷贝
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除原有口味数据                  //修改就是先 删除 再 插入 的操作
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {     //遍历DishFlavor集合
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Transactional //事务注解，保证数据的一致性
    public void startOrStop(Integer status, Long id) {
        Dish  dish = Dish.builder() //构建dish对象
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);//调用update更新dish对应菜品状态

        if (status == StatusConstant.DISABLE) {// 如果是停售状态，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            //查询与菜品关联的所有套餐ID，以List集合方式存入setmealIds中
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {//若存在关联套餐
                for (Long setmealId : setmealIds) {//则遍历每个套餐的ID，
                    Setmeal setmeal = Setmeal.builder() //构建setmeal对象并设置状态为停售
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);//更新套餐状态
                }
            }
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     */
    public List<Dish> list(Long categoryId) {//@Builder:默认为在类中声明的字段生成相应的建造方法
        Dish dish = Dish.builder() //使用Lombok的建造者模式创建Dish对象
                .categoryId(categoryId) //设置菜品分类ID(传入查询参数)
                .status(StatusConstant.ENABLE)//固定设置为启售状态（常量值为1）
                .build();//完成对象构建
        return dishMapper.list(dish);//调用Mapper查询数据库
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);//复制

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

}