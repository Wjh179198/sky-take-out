package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for(LocalDate curr = begin; !curr.isAfter(end); curr = curr.plusDays(1L)) {
            dateList.add(curr);
        }
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            if(turnover == null) turnover = 0.0;
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for(LocalDate curr = begin; !curr.isAfter(end); curr = curr.plusDays(1L)) {
            dateList.add(curr);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> sumUserList = new ArrayList<>();
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer newUserCount = userMapper.getNewUserCount(beginTime, endTime);
            Integer sumUserCount = userMapper.getSumUser(endTime);
            if(newUserCount == null) newUserCount = 0;
            if(sumUserCount == null) sumUserCount = 0;
            newUserList.add(newUserCount);
            sumUserList.add(sumUserCount);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(sumUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for(LocalDate curr = begin; !curr.isAfter(end); curr = curr.plusDays(1L)) {
            dateList.add(curr);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer orderCount = 0;
        Integer validOrderCount = 0;
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer sumCount = orderMapper.getCountByDay(beginTime, endTime);
            Integer validSumCount = orderMapper.getValidCountByDay(beginTime, endTime);
            if(sumCount == null) sumCount = 0;
            if(validSumCount == null) validSumCount = 0;
            orderCount += sumCount;
            validOrderCount += validSumCount;
            orderCountList.add(sumCount);
            validOrderCountList.add(validSumCount);
        }
        Double orderCompleteRate = 0.0;
        if(orderCount != 0) {
            orderCompleteRate = validOrderCount.doubleValue() / orderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCompletionRate(orderCompleteRate)
                .totalOrderCount(orderCount)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .validOrderCount(validOrderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        LocalDate end = LocalDate.now().minusDays(1L);
        LocalDate begin = end.minusDays(29L);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模版创建一个excel文件
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //填充数据
            XSSFSheet sheet = excel.getSheet("sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间" + begin + "至" + end);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(2).getCell(6).setCellValue(businessData.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());
            int row = 7;
            for(LocalDate curr = end; !curr.isBefore(begin); curr = curr.minusDays(1L)) {
                businessData = workspaceService.getBusinessData(LocalDateTime.of(curr, LocalTime.MIN), LocalDateTime.of(curr, LocalTime.MAX));
                sheet.getRow(row).getCell(1).setCellValue(curr.toString());
                sheet.getRow(row).getCell(2).setCellValue(businessData.getTurnover());
                sheet.getRow(row).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet.getRow(row).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet.getRow(row).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet.getRow(row).getCell(6).setCellValue(businessData.getNewUsers());
                row++;
            }
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
