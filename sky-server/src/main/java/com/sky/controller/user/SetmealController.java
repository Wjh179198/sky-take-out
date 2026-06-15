package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @Cacheable(cacheNames = "SetmealCache", key = "#categoryId")
    public Result<List<Setmeal>> getByCategoryId (Long categoryId) {
        List<Setmeal> setmealList = setmealService.getByCategoryId(categoryId);
        return Result.success(setmealList);
    }

    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> dishList (@PathVariable Long id) {
        List<DishItemVO> dishItemVOList = setmealService.getDishListById(id);
        return Result.success(dishItemVOList);
    }


}
