package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    void deleteByDishIds(List<Long> dishIds);

    @Select("select id, dish_id, name, value from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);
}
