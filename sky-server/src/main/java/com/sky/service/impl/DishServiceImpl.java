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
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(1);
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            for(DishFlavor dishFlavor : flavors) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<DishVO> dishVOList = dishMapper.pageQuery(dishPageQueryDTO);
        Page<DishVO> dishVOPage = (Page<DishVO>) dishVOList;
        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        for(Long id : ids) {
            Integer status = dishMapper.getStatusById(id);
            if(status == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        for(Long id : ids) {
            Integer counts = setmealMapper.countByDishId(id);
            if(counts > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        List<Long> dishIds = new ArrayList<>();
        dishIds.add(dish.getId());
        dishFlavorMapper.deleteByDishIds(dishIds);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            for(DishFlavor dishFlavor : flavors) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }

    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        List<Dish> dishList = dishMapper.getByCateGoryId(categoryId);
        List<DishVO> dishVOList = new ArrayList<>();
        for(Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(dish.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }


}
