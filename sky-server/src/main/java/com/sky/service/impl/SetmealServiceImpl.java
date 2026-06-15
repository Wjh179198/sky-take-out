package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public List<Setmeal> getByCategoryId (Long categoryId) {
        List<Setmeal> setmealList = setmealMapper.selectByCategoryId(categoryId);
        return setmealList;
    }

    @Override
    public List<DishItemVO> getDishListById(Long id) {
        return setmealMapper.selectDishById(id);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        List<SetmealVO> setmealVOList = setmealMapper.pageQuery(pageQueryDTO);
        Page<SetmealVO> setmealVOPage = (Page<SetmealVO>) setmealVOList;
        return new PageResult(setmealVOPage.getTotal(), setmealVOPage.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(1);
        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        if (setmealDishList != null && setmealDishList.size() > 0) {
            for(SetmealDish setmealDish : setmealDishList) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishList);
        }
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        List<SetmealDish> setmealDishList = setmealDishMapper.selectBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(List<Long> ids) {
        for(Long id : ids) {
            Integer status = setmealMapper.getStatusById(id);
            if(status == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException("当前有起售中的套餐,无法删除");
            }
        }
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        List<Long> setmealList = new ArrayList<>();
        setmealList.add(setmeal.getId());
        setmealDishMapper.deleteBySetmealIds(setmealList);
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        if (setmealDishList != null) {
            for(SetmealDish setmealDish : setmealDishList) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishList);
        }
    }
}
