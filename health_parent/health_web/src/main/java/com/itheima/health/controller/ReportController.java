package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.service.MemberService;
import com.itheima.health.service.ReportService;
import com.itheima.health.service.SetmealService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName CheckItemController
 * @Description TODO
 * @Author ly
 * @Company 深圳黑马程序员
 * @Date 2019/11/28 10:04
 * @Version V1.0
 */
@RestController
@RequestMapping(value = "/report")
public class ReportController {

    @Reference
    MemberService memberService;

    @Reference
    SetmealService setmealService;

    @Reference
    ReportService reportService;

    // 统计会员注册的折线图
    @RequestMapping(value = "/getMemberReport")
    public Result getMemberReport(){
        try {
            // 1：数据集合，存放months
            List<String> months = new ArrayList<>();
            // 统计过去1年的时间，按月统计（格式：年-月，2019-12）
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.MONTH,-12);
            for (int i = 0; i < 12; i++) {
                instance.add(Calendar.MONTH,1);
                Date time = instance.getTime(); // 获取当前时间（2019-01，2019-02， 2019-03）
                String date = new SimpleDateFormat("yyyy-MM").format(time);
                months.add(date);
            }
            // 2：数据集合，存放memberCount，根据注册时间完成查询
            List<Integer> memberCount = memberService.findMemberCountByRegTime(months);
            // 构造Map集合
            Map<String,Object> map = new HashMap<>();
            map.put("months",months); // List<String>
            map.put("memberCount",memberCount); // List<Integer>
            return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_MEMBER_NUMBER_REPORT_FAIL);
        }
    }


    // 统计套餐预约占比饼形图
    @RequestMapping(value = "/getSetmealReport")
    public Result getSetmealReport(){
        try {
            // 存放套餐的名称
            List<String> setmealNames = new ArrayList<>();
            // 存放套餐的名称、对应套餐名称的值
            List<Map> setmealCount = setmealService.findSetmealCount();
            // 遍历套餐的名称，存放到setmealNames中
            if(setmealCount!=null && setmealCount.size()>0){
                for (Map map : setmealCount) {
                    String name = (String)map.get("name");
                    setmealNames.add(name);
                }
            }
            // 构造Map集合
            Map<String,Object> map = new HashMap<>();
            map.put("setmealNames",setmealNames); // List<String>
            map.put("setmealCount",setmealCount); // List<Map>
            return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_SETMEAL_COUNT_REPORT_FAIL);
        }
    }

    // 运营数据统计报表
    @RequestMapping(value = "/getBusinessReportData")
    public Result getBusinessReportData(){
        try {
            Map map = reportService.findBusinessReportData();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    // 运营数据统计报表（导出Excel）
    @RequestMapping(value = "/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try {
            Map map = reportService.findBusinessReportData();
            // 第一步：从Map集合中获取数据
            String reportDate = (String)map.get("reportDate");
            Integer todayNewMember = (Integer)map.get("todayNewMember");
            Integer totalMember = (Integer)map.get("totalMember");
            Integer thisWeekNewMember = (Integer)map.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer)map.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer)map.get("todayOrderNumber");
            Integer todayVisitsNumber = (Integer)map.get("todayVisitsNumber");
            Integer thisWeekOrderNumber = (Integer)map.get("thisWeekOrderNumber");
            Integer thisWeekVisitsNumber = (Integer)map.get("thisWeekVisitsNumber");
            Integer thisMonthOrderNumber = (Integer)map.get("thisMonthOrderNumber");
            Integer thisMonthVisitsNumber = (Integer)map.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>)map.get("hotSetmeal");
            // 第二步：加载webapp下template文件夹下report_template.xlsx文件，将模板文件POI报表的核心对象WorkBook
            String path = request.getSession().getServletContext().getRealPath("template")+ File.separator+"report_template.xlsx";
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(path)));
            // 第三步：使用POI的API向excel的报表中填充数据
            XSSFSheet sheet = workbook.getSheetAt(0);// 获取第1个工作表
            XSSFRow row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);  // 日期

            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);  // 今天新增会员数
            row.getCell(7).setCellValue(totalMember);  // 总会员数

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);//本周新增会员数
            row.getCell(7).setCellValue(thisMonthNewMember);//本月新增会员数

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);//今日预约数
            row.getCell(7).setCellValue(todayVisitsNumber);//今日到诊数

            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);//本周预约数
            row.getCell(7).setCellValue(thisWeekVisitsNumber);//本周到诊数

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);//本月预约数
            row.getCell(7).setCellValue(thisMonthVisitsNumber);//本月到诊数

            // 热门套餐
            int rowNum = 12;
            for (Map map1 : hotSetmeal) {
                String name = (String)map1.get("name");
                Long setmeal_count = (Long)map1.get("setmeal_count");
                BigDecimal proportion = (BigDecimal)map1.get("proportion");
                row = sheet.getRow(rowNum++);
                row.getCell(4).setCellValue(name);   // 套餐名称
                row.getCell(5).setCellValue(setmeal_count);  // 套餐数量
                row.getCell(6).setCellValue(proportion.doubleValue());  // 占比情况
            }

            // 第四步：将生成的excel报表，使用IO流的方式响应到页面
            ServletOutputStream out = response.getOutputStream(); // 默认的导出类型是文本
            // 设置1，设置的文件类型是excel
            response.setContentType("application/vnd.ms-excel"); // 导出类型而是excel
            // 设置2：设置附件形式下载，下载到本地(attachment;filename=report80.xlsx)，还是内联(inline)（默认）
            response.setHeader("Content-Disposition","attachment;filename=report80.xlsx");
            workbook.write(out);
            out.flush();
            out.close();
            workbook.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
