package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    public Result<PageResult> page (SetmealPageQueryDTO pageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @CacheEvict(cacheNames = "SetmealCache", key = "#setmealDTO.categoryId")
    public Result insert (@RequestBody SetmealDTO setmealDTO) {
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getInfo (@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @DeleteMapping
    @CacheEvict(cacheNames = "SetmealCache", allEntries = true)
    public Result deleteById (@RequestParam List<Long> ids) {
        setmealService.deleteById(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "SetmealCache", allEntries = true)
    public Result startOrStop (@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @CacheEvict(cacheNames = "SetmealCache", allEntries = true)
    public Result update (@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
