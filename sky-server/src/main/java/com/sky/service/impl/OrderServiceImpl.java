package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;

import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;

import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) {      //处理地址为空
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0) {  //处理购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(currentId);
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // 跳过微信支付接口，直接标记订单为已支付（无商户测试用）
        paySuccess(ordersPaymentDTO.getOrderNumber());

        // 返回模拟的支付参数，前端调用 wx.requestPayment 会失败，但订单已支付
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .timeStamp(timeStamp)
                .signType("RSA")
                .packageStr("prepay_id=mock_prepay_id")
                .build();
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号: " + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult pageQuery(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        List<Orders> ordersList = orderMapper.pageQueryByParam(ordersPageQueryDTO);
        Page<Orders> ordersPage = (Page<Orders>) ordersList;
        List<OrderVO> orderVOList = new ArrayList<>();
        if(ordersPage != null && ordersPage.size() > 0) {
            for(Orders orders : ordersPage) {
                List<OrderDetail> details = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(details);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderVO getDetails(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) throws ObjectNotFoundException {
        Orders orders = orderMapper.getById(id);
        if(orders == null) {
            throw new ObjectNotFoundException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = new Orders();
        order.setId(id);
        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消订单");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        for(OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public PageResult page(OrdersPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        List<Orders> ordersList = orderMapper.pageQueryByParam(pageQueryDTO);
        Page<Orders> ordersPage = (Page<Orders>) ordersList;
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders order : ordersList) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
            StringBuilder dishName = new StringBuilder();
            for(OrderDetail orderDetail : orderDetailList) {
                dishName.append(orderDetail.getName());
            }
            orderVO.setOrderDishes(dishName.toString());
            orderVOList.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO getOrderStatus() {
        Integer toBeConfirmedCounts = orderMapper.getToBeConfirmedCounts();
        Integer confirmedCounts = orderMapper.getConfirmedCounts();
        Integer deliveryInProgressCounts = orderMapper.getDeliveryInProgressCounts();
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedCounts);
        orderStatisticsVO.setConfirmed(confirmedCounts);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressCounts);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        Integer status = orders.getStatus();
        Integer payStatus = orders.getPayStatus();
        if(!payStatus.equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.UNPAID);
        }
        if(!status.equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = new Orders();
        order.setStatus(Orders.CONFIRMED);
        order.setId(ordersConfirmDTO.getId());
        orderMapper.update(order);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if(!orders.getStatus().equals(Orders.CONFIRMED) || orders.getPayStatus().equals(Orders.UN_PAID)) {
            throw new OrderBusinessException(MessageConstant.UNPAID);
        }
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = new Orders();
        order.setStatus(Orders.CANCELLED);
        order.setId(orders.getId());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(order);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders.getPayStatus().equals(Orders.PAID)) {
            log.info("用户退款");
        }
        Orders order = new Orders();
        order.setId(orders.getId());
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        orderMapper.update(order);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if(!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号: " + orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }


}