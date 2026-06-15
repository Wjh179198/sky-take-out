package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingService shoppingService;

    @PostMapping("/add")
    @ApiOperation("新增商品")
    public Result add (@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list () {
        List<ShoppingCart> shoppingCartList = shoppingService.showShoppingCart();
        return Result.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean () {
        shoppingService.cleanShoppingCart();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除购物车中的商品")
    public Result sub (@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
