package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HttpRequestExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController("adminReportController")
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> getTurnOverStatistics (@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        TurnoverReportVO turnover = reportService.getTurnover(begin, end);
        return Result.success(turnover);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> getUserStatistics (@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
        return Result.success(userReportVO);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> getOrderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        OrderReportVO orderReportVO = reportService.getOrderStatistics(begin, end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("销量排名top10")
    public Result<SalesTop10ReportVO> getSalesTop10 (@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        SalesTop10ReportVO salesTop10ReportVO = reportService.getSalesTop10(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export (HttpServletResponse httpServletResponse) {
        reportService.exportBusinessData(httpServletResponse);
    }
}
