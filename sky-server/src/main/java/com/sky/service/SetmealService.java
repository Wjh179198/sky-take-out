package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {


    List<Setmeal> getByCategoryId(Long categoryId);

    List<DishItemVO> getDishListById(Long id);

    PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO);

    void insert(SetmealDTO setmealDTO);

    SetmealVO getById(Long id);

    void deleteById(List<Long> ids);

    void startOrStop(Integer status, Long id);

    void update(SetmealDTO setmealDTO);
}
