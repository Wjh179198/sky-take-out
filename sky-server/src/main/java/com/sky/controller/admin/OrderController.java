package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> page (OrdersPageQueryDTO pageQueryDTO) {
        PageResult pageResult = orderService.page(pageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatus () {
        OrderStatisticsVO orderStatus = orderService.getOrderStatus();
        return Result.success(orderStatus);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> detailList(@PathVariable Long id) {
        OrderVO details = orderService.getDetails(id);
        return Result.success(details);
    }

    @PutMapping("/confirm")
    public Result confirm (@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result delivery (@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejection (@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    public Result cancel (@RequestBody OrdersCancelDTO ordersCancelDTO) {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result complete (@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
