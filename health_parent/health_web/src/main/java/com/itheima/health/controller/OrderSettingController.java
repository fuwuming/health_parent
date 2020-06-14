package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.pojo.OrderSetting;
import com.itheima.health.service.OrderSettingService;
import com.itheima.health.utils.POIUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CheckItemController
 * @Description TODO
 * @Author ly
 * @Company 深圳黑马程序员
 * @Date 2019/11/28 10:04
 * @Version V1.0
 */
@RestController
@RequestMapping(value = "/ordersetting")
public class OrderSettingController {

    @Reference
    OrderSettingService orderSettingService;


    // 从Excel文件中读取预约设置数据，批量导入到数据库
    @RequestMapping(value = "/upload")
    public Result upload(@RequestParam(value = "excelFile") MultipartFile excelFile){
        try {
            //1：读取excel的数据
            List<String[]> list = POIUtils.readExcel(excelFile);
            //2：封装导入的数据库的实体类型
            List<OrderSetting> orderSettingList = new ArrayList<>();
            if(list!=null && list.size()>0){
                for (String[] strings : list) {
                    OrderSetting orderSetting = new OrderSetting(new Date(strings[0]),Integer.parseInt(strings[1]));
                    orderSettingList.add(orderSetting);
                }
            }
            // 3：完成批量导入数据库
            orderSettingService.addList(orderSettingList);
            return new Result(true, MessageConstant.IMPORT_ORDERSETTING_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.IMPORT_ORDERSETTING_FAIL);
        }
    }

    // 初始化日历中的预约设置信息，根据当前年月时间完成初始化
    @RequestMapping(value = "/findOrderSettingByOrderDate")
    public Result findOrderSettingByOrderDate(String date){
        try {
            List<Map> list = orderSettingService.findOrderSettingByOrderDate(date);
            return new Result(true, MessageConstant.GET_ORDERSETTING_SUCCESS,list);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_ORDERSETTING_FAIL);
        }
    }

    // 基于当前时间，更新最多预约人数（单条更新）
    @RequestMapping(value = "/updateNumberByOrderDate")
    public Result updateNumberByOrderDate(@RequestBody OrderSetting orderSetting){
        try {
            orderSettingService.updateNumberByOrderDate(orderSetting);
            return new Result(true, MessageConstant.ORDERSETTING_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.ORDERSETTING_FAIL);
        }
    }
}
