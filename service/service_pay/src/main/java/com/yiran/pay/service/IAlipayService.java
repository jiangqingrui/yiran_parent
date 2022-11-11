package com.yiran.pay.service;


/**
 * @author Yang Song
 * @date 2022/11/3 9:25
 */
public interface IAlipayService {
    /**
     *  拉起页面
     * @param orderId 待支付订单id
     * @return 页面
     */
    String goAliPay(String orderId);

}
