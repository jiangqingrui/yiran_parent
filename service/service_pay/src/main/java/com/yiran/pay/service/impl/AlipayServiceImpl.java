package com.yiran.pay.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.yiran.client.order.OrderClient;
import com.yiran.model.entity.Orders;
import com.yiran.model.vo.AlipayVo;
import com.yiran.pay.config.AlipayProperties;
import com.yiran.pay.service.IAlipayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yang Song
 * @date 2022/11/3 9:28
 */
@Service
public class AlipayServiceImpl implements IAlipayService {
    private final DefaultAlipayClient client;
    private final OrderClient orderClient;
    private final RabbitTemplate rabbitTemplate;
    private final AlipayProperties alipayProperties;

    public AlipayServiceImpl(DefaultAlipayClient client, OrderClient orderClient,RabbitTemplate rabbitTemplate,AlipayProperties alipayProperties) {
        this.client = client;
        this.orderClient = orderClient;
        this.rabbitTemplate = rabbitTemplate;
        this.alipayProperties = alipayProperties;
    }

    @Override
    public String goAliPay(String orderId) {
        // 查询订单信息
        Orders order = orderClient.queryOrder(orderId).getData().get("order");
        // 订单超时时间
        String timeExpire = order.getCreateTime().plusMinutes(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 调用alipay.trade.page.pay接口 发起支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        // 必选四项参数 --其他参数参考官方文档
        bizContent.put("out_trade_no", order.getOrderId());
        bizContent.put("total_amount", order.getOrderAmount());
        bizContent.put("subject",order.getSubject());
        bizContent.put("time_expire",timeExpire);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        String form = "";
        try {
            form = client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }
}
