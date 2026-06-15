package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("select count(*) from setmeal_dish where dish_id = #{id}")
    Integer countByDishId (Long id);

    @Select("select id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user from setmeal where category_id = #{categoryId} and status = 1")
    List<Setmeal> selectByCategoryId(Long categoryId);

    @Select("select sd.name, sd.copies, d.image, d.description from setmeal_dish sd left join dish d on sd.dish_id = d.id where setmeal_id = #{id}")
    List<DishItemVO> selectDishById(Long id);

    List<SetmealVO> pageQuery(SetmealPageQueryDTO pageQueryDTO);

    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    @Select("select id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user from setmeal where id = #{id}")
    Setmeal selectById(Long id);

    @Select("select status from setmeal where id = #{id}")
    Integer getStatusById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
