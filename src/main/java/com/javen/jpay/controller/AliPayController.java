package com.javen.jpay.controller;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCancelModel;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.javen.jpay.alipay.AliPayApi;
import com.javen.jpay.util.ParamsUtils;
import com.javen.jpay.util.StringUtils;
import com.javen.jpay.vo.AjaxResult;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;

public class AliPayController extends Controller {
	private Log log = Log.getLog(AliPayController.class);
	private AjaxResult result = new AjaxResult();

	
	public void index() {
		renderText("test");
	}
	
	/**
	 * app支付
	 */
	public void appPay(){
		try {
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			model.setBody("我是测试数据");
			model.setSubject("App支付测试-By Javen");
			model.setOutTradeNo(StringUtils.getOutTradeNo());
			model.setTimeoutExpress("30m");
			model.setTotalAmount("0.01");
			model.setPassbackParams("callback params");
			model.setProductCode("QUICK_MSECURITY_PAY");
			String orderInfo = AliPayApi.startAppPayStr(model,AliPayApi.notify_domain+"/alipay/app_pay_notify");
			result.success(orderInfo);
			renderJson(result);
			
		} catch (AlipayApiException e) {
			e.printStackTrace();
			result.addError("system error");
		}
	}


	/**
	 * Wap支付
	 */
	public void wapPay() {
		String body = "我是测试数据";
		String subject = "Javen 测试";
		String totalAmount = "0.01";
		String passbackParams = "1";
		String returnUrl = AliPayApi.notify_domain+"/alipay/return_url";
		String notifyUrl = AliPayApi.notify_domain+"/alipay/notify_url";

		AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
		model.setBody(body);
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setPassbackParams(passbackParams);
		model.setOutTradeNo(StringUtils.getOutTradeNo());
		model.setProductCode("QUICK_WAP_PAY");
		
		try {
			AliPayApi.wapPay(getResponse(), model,returnUrl,notifyUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderNull();
	}

	public void return_url() {
		// total_amount=0.01&timestamp=2016-12-02+18%3A11%3A42&sign=vPhxaI5bf7uSab9HuqQ4fvjLOggzpnnLK9svOdZCZ9N1mge4qm63R4k%2FowlTHbwyGCNG0%2F4PthfYbjFx22%2B2WpBNvccxajw%2Btba1Aab6EKPOAW8BoLLFFwgExtLB9ydhWL5kpP8YSLolO%2F9pkGBy5TNldz7HxdB2j6vISrD8qCs%3D&trade_no=2016120221001004200200187882&sign_type=RSA&auth_app_id=2016102000727659&charset=UTF-8&seller_id=2088102180432465&method=alipay.trade.wap.pay.return&app_id=2016102000727659&out_trade_no=120218111214806&version=1.0
		String params = getRequest().getQueryString();
		System.out.println("return_url回调参数：" + params);
		log.info("return_url回调参数：" + params);
		renderText("return_url回调参数：" + params);

	}

	/**
	 * 条形码支付
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.Yhpibd&treeId=194&articleId=105170&docType=1#s4
	 */
	public void tradePay() {
		String authCode = getPara("auth_code");
		String subject = "Javen 支付宝条形码支付测试";
		String totalAmount = "100";
		String alipayStoreId = "123";

		
		AlipayTradePayModel model = new AlipayTradePayModel();
		model.setAlipayStoreId(alipayStoreId);
		model.setAuthCode(authCode);
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setOutTradeNo(StringUtils.getOutTradeNo());
		model.setTimeoutExpress("5m");
		model.setProductCode("bar_code");
		
		try {
			String resultStr = AliPayApi.tradePay(model);
			renderText(resultStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 扫码支付
	 */
	public void tradePrecreatePay() {
		String subject = "Javen 支付宝扫码支付测试";
		String totalAmount = "86";
		String storeId = "123";
		
		AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
		model.setSubject(subject);
		model.setTotalAmount(totalAmount);
		model.setStoreId(storeId);
		model.setTimeoutExpress("5m");
		model.setOutTradeNo(StringUtils.getOutTradeNo());

		try {
			String resultStr = AliPayApi.tradePrecreatePay(model);
			JSONObject jsonObject = JSONObject.parseObject(resultStr);
			String qr_code = jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");
			renderText(qr_code);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单笔转账到支付宝账户
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.54Ty29&treeId=193&articleId=106236&docType=1
	 */
	public void transfer() {
		boolean isSuccess = false;
		String total_amount = "100";
		AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
		model.setOutBizNo(StringUtils.getOutTradeNo());
		model.setPayeeType("ALIPAY_LOGONID");
		model.setPayeeAccount("abpkvd0206@sandbox.com");
		model.setAmount(total_amount);
		model.setPayerShowName("测试退款");
		model.setPayerRealName("沙箱环境");
		model.setRemark("javen测试单笔转账到支付宝");
		
		try {
			isSuccess = AliPayApi.transfer(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderJson(isSuccess);
	}
	/**
	 * 下载对账单
	 */
	public void dataDataserviceBill(){
		String para = getPara("billDate");
		try {
			AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
			model.setBillType("trade");
			model.setBillDate(para);
			String resultStr = AliPayApi.billDownloadurlQuery(model);
			renderText(resultStr);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 退款
	 */
	public void tradeRefund(){
		
		try {
			AlipayTradeRefundModel model = new AlipayTradeRefundModel();
			model.setOutTradeNo("042517111114931");
			model.setTradeNo("2017042521001004200200236813");
			model.setRefundAmount("86.00");
			model.setRefundReason("正常退款");
			String resultStr = AliPayApi.tradeRefund(model);
			renderText(resultStr);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}
	
	public void tradeCancel(){
		try {
			AlipayTradeCancelModel model = new AlipayTradeCancelModel();
			model.setOutTradeNo("042518024814931");
			model.setTradeNo("2017042521001004200200236814");
			
			boolean isSuccess = AliPayApi.isTradeCancel(model);
			renderJson(isSuccess);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}
	public void tradeQuery(){
		try {
			AlipayTradeQueryModel model = new AlipayTradeQueryModel();
			model.setOutTradeNo("042518024814931");
			model.setTradeNo("2017042521001004200200236814");
			
			boolean isSuccess = AliPayApi.isTradeQuery(model);
			renderJson(isSuccess);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}
	public void tradeQueryByStr(){
		String out_trade_no = getPara("out_trade_no");
//		String trade_no = getPara("trade_no");
		
		AlipayTradeQueryModel model = new AlipayTradeQueryModel();
		model.setOutTradeNo(out_trade_no);

		try {
			String resultStr = AliPayApi.tradeQuery(model).getBody();
			renderText(resultStr);;
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}

	public void notify_url() {
		String params = HttpKit.readData(getRequest());
		log.debug("notify_url readData>>"+params);
		System.out.println("notify_url readData>>"+params);
		try {
			Map<String, String> requestParams = ParamsUtils.getUrlParams(params);
			String alipayPulicKey = AliPayApi.alipayPulicKey;
			System.out.println(alipayPulicKey.equals("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuPkP2VJMR6vWCX8RwSFqNIa3klCdvRFJbuS1PN1anzQeeL9eOwtU7kGdI85yxb0dcdPzOYlG+jf9go8W9hBlgjxSRoXxLx03Yfl7cLmzJO9l9vIM1+HmNF0Ctm+el4Yi9dGs/P6q7lcHPUqs/RXGfeLrg33GMVwJbLmRcDZYeIcqPAA1OVF/4SHYr+f+O7glDOd60z+veOOexyoHmvUzYWlEz5+R4kOCNM/Z0w7KGgEYvHbZopexuTuFgUWy/9tYlNrnX+cZUWXVTskLUgD1UGWM1dS5+qfriqY9MPEwJjetcPJkoCK7A4IReE4q1DffUY9KS50/1ML+7na3R/p/UQIDAQAB")+" length>"+alipayPulicKey.length()+" >>"+"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuPkP2VJMR6vWCX8RwSFqNIa3klCdvRFJbuS1PN1anzQeeL9eOwtU7kGdI85yxb0dcdPzOYlG+jf9go8W9hBlgjxSRoXxLx03Yfl7cLmzJO9l9vIM1+HmNF0Ctm+el4Yi9dGs/P6q7lcHPUqs/RXGfeLrg33GMVwJbLmRcDZYeIcqPAA1OVF/4SHYr+f+O7glDOd60z+veOOexyoHmvUzYWlEz5+R4kOCNM/Z0w7KGgEYvHbZopexuTuFgUWy/9tYlNrnX+cZUWXVTskLUgD1UGWM1dS5+qfriqY9MPEwJjetcPJkoCK7A4IReE4q1DffUY9KS50/1ML+7na3R/p/UQIDAQAB".length());
			boolean signVerified = AlipaySignature.rsaCheckV1(requestParams, AliPayApi.alipayPulicKey, AliPayApi.charset ,AliPayApi.signType); // 调用SDK验证签名
			if (signVerified) {
				// TODO
				// 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
				renderText("success");
				return;
			} else {
				// TODO 验签失败则记录异常日志，并在response中返回failure.
				renderText("failure");
				return;
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			renderText("failure");
		}
	}
	
	
	/**
	 * App支付支付回调通知
	 * https://doc.open.alipay.com/docs/doc.htm?treeId=54&articleId=106370&docType=1#s3
	 */
	public void app_pay_notify() {
		try {
			//获取支付宝POST过来反馈信息
			Map<String,String> params = new HashMap<String,String>();
			Map<String, String[]> requestParams = getRequest().getParameterMap();
			for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			    String name = (String) iter.next();
			    String[] values = (String[]) requestParams.get(name);
			    String valueStr = "";
			    for (int i = 0; i < values.length; i++) {
			        valueStr = (i == values.length - 1) ? valueStr + values[i]
			                    : valueStr + values[i] + ",";
			  }
			  //乱码解决，这段代码在出现乱码时使用。
			  //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			  params.put(name, valueStr);
			 }
			//切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
			//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
			boolean flag = AlipaySignature.rsaCheckV1(params, AliPayApi.alipayPulicKey, AliPayApi.charset, AliPayApi.signType);
			if (flag) {
				// TODO
				System.out.println("success");
				renderText("success");
				return;
			}else {
				// TODO
				System.out.println("failure");
				renderText("failure");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			renderText("failure");
		}
	}
}