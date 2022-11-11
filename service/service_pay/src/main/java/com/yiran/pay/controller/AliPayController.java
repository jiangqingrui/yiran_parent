package com.yiran.pay.controller;

import com.yiran.model.vo.AlipayVo;
import com.yiran.pay.config.AlipayProperties;
import com.yiran.pay.service.IAlipayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Yang Song
 * @date 2022/11/3 9:20
 */
@RestController
@RequestMapping("/pay")
public class AliPayController {
    private final IAlipayService alipayService;
    private final AlipayProperties alipayProperties;
    private final RabbitTemplate rabbitTemplate;
    public AliPayController(IAlipayService iAlipayService,AlipayProperties alipayProperties,RabbitTemplate rabbitTemplate){
        this.alipayService = iAlipayService;
        this.alipayProperties = alipayProperties;
        this.rabbitTemplate = rabbitTemplate;
    }
    /**
     * 根据订单id 拉起支付页面
     * @param orderId 订单id
     * @return 支付页面
     */
    @GetMapping("/goAliPay/{orderId}")
    public String goPay(@PathVariable("orderId") String orderId){
        return alipayService.goAliPay(orderId);
    }

    /**
     * 用户付款成功支付宝同步回调
     * @param alipayVo 支付宝同步回调参数
     */
    @GetMapping("/aliReturn")
    public void callBack2(AlipayVo alipayVo, HttpServletResponse response){
        System.out.println("同步回调"+alipayVo);
        rabbitTemplate.convertAndSend("yiran.order.topic","order.pay.finish",alipayVo);
        try {
            response.sendRedirect(alipayProperties.getRedirectUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
