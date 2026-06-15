package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    public Result<List<DishVO>> list (Long categoryId) {
        String key = "dish_" + categoryId;
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(dishVOList != null && dishVOList.size() > 0) {
            return Result.success(dishVOList);
        }
        dishVOList = dishService.getByCategoryId(categoryId);
        redisTemplate.opsForValue().set(key, dishVOList);
        return Result.success(dishVOList);
    }
}
