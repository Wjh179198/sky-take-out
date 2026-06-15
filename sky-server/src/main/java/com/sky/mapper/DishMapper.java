package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select status from dish where id = #{id}")
    Integer getStatusById (Long id);

    void deleteByIds(List<Long> ids);

    @Select("select id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user from dish where id = #{id}")
    public Dish selectById (Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> getByCateGoryId(Long categoryId);

    @Select("select category_id from dish where id = #{dishId}")
    Integer getCategoryIdByDishId(Long dishId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
