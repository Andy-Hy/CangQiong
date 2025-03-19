package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * @author admin
 * @date 2025/3/19 16:19
 */
public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
