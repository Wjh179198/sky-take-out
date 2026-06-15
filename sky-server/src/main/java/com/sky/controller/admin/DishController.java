package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DishMapper dishMapper;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save (@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavor(dishDTO);
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page (DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteByIdBatch (@RequestParam List<Long> ids) {
        dishService.deleteByIds(ids);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询")
    public Result<DishVO> getById (@PathVariable Long id) {
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update (@RequestBody DishDTO dishDTO) {
        dishService.update(dishDTO);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售或停售菜品")
    public Result startOrStop (@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        Integer categoryIdByDishId = dishMapper.getCategoryIdByDishId(id);
        String key = "dish_" + categoryIdByDishId;
        redisTemplate.delete(key);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list (Long categoryId) {
        List<DishVO> dishVOList = dishService.getByCategoryId(categoryId);
        return Result.success(dishVOList);
    }
}
