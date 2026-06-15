package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    List<Orders> pageQueryByParam(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(*) from orders where status = 2")
    Integer getToBeConfirmedCounts();

    @Select("select count(*) from orders where status = 3")
    Integer getConfirmedCounts();

    @Select("select count(*) from orders where status = 4")
    Integer getDeliveryInProgressCounts();

    @Select("select * from orders where status = #{pendingPayment} and order_time <= #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    Double sumByMap(Map map);

    @Select("select count(*) from orders where order_time between #{beginTime} and #{endTime}")
    Integer getCountByDay(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(*) from orders where order_time between #{beginTime} and #{endTime} and status = 5")
    Integer getValidCountByDay(LocalDateTime beginTime, LocalDateTime endTime);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    @Select("select count(*) from orders where status = 5")
    Integer getCompleted();

    @Select("select count(*) from orders where status = 6")
    Integer getCancel();

    @Select("select count(*) from orders")
    Integer getCounts();
}
