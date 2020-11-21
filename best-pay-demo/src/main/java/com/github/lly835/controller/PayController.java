package com.github.lly835.controller;

import com.github.lly835.config.WechatAccountConfig;
import com.google.gson.Gson;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.*;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import com.lly835.bestpay.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * 支付相关
 * @version 1.0 2017/3/2
 * @auther <a href="mailto:lly835@163.com">廖师兄</a>
 * @since 1.0
 */

/**
 * @Controller用于标记在一个类上，使用它标记的类就是一个SpringMvc Controller对象，分发处理器会扫描使用该注解的类的方法，并检测该方法是否使用了@RequestMapping注解。
 * @Controller只是定义了一个控制器类，而使用@RequestMapping注解的方法才是处理请求的处理器。
 * @Controller标记在一个类上还不能真正意义上说它就是SpringMvc的控制器，应为这个时候Spring还不认识它，这个时候需要把这个控制器交给Spring来管理。
 * 有两种方式可以管理：
 * 基于注解的装配
 * 方式一
 * <bean class="com.HelloWorld"/>
 * 方式二
 * 路径写到controller的上一层
 * <context:component-scan base-package="com"/>
 */
@Controller
/**
 * 如果不想每次都写private  final Logger logger = LoggerFactory.getLogger(当前类名.class); 可以用注解@Slf4j;
 * 1.使用idea首先需要安装Lombok插件;
 * 2.在pom文件加入lombok的依赖
 * 3.类上面添加@Sl4j注解,然后使用log打印日志;
 */
@Slf4j
public class PayController {

    /**
     * @Autowired是用在JavaBean中的注解，通过byType形式，用来给指定的字段或方法注入所需的外部资源。
     */
    @Autowired
    private WechatAccountConfig wechatAccountConfig;

    @Autowired
    private BestPayServiceImpl bestPayService;

    /**
     * 发起支付
     */
    @PostMapping(value = "/pay")
    /**
     * 等价于@RequestMapping(value = "/pay",method = RequestMethod.POST)
     */
    /**
     * @ResponseBody注解作用与原理
     * 1、概念
     *         注解 @ResponseBody，使用在控制层（controller）的方法上。
     * 2、作用
     *         作用：将方法的返回值，以特定的格式写入到response的body区域，进而将数据返回给客户端。
     *         当方法上面没有写ResponseBody,底层会将方法的返回值封装为ModelAndView对象。
     *         如果返回值是字符串，那么直接将字符串写到客户端；如果是一个对象，会将对象转化为json串，然后写到客户端。
     * 3、注意编码
     *         如果返回对象,按utf-8编码。如果返回String，默认按iso8859-1编码，页面可能出现乱码。因此在注解中我们可以手动修改编码格式，
     *         例如@RequestMapping(value="/cat/query",produces="text/html;charset=utf-8")，前面是请求的路径，后面是编码格式。
     *
     * 4、原理
     *         控制层方法的返回值是如何转化为json格式的字符串的？其实是通过HttpMessageConverter中的方法实现的，它本是一个接口，在其实现类完成转换。
     *         如果是bean对象，会调用对象的getXXX（）方法获取属性值并且以键值对的形式进行封装，进而转化为json串。
     *         如果是map集合，采用get(key)方式获取value值，然后进行封装。
     */
    @ResponseBody
    /**
     * @RequestParam：将请求参数绑定到你控制器的方法参数上（是springmvc中接收普通参数的注解）
     * 语法：@RequestParam(value=”参数名”,required=”true/false”,defaultValue=””)
     * value：参数名
     * required：是否包含该参数，默认为true，表示该请求路径中必须包含该参数，如果不包含就报错。
     * defaultValue：默认参数值，如果设置了该值，required=true将失效，自动为false,如果没有传该参数，就使用默认值
     */
    public PayResponse pay(@RequestParam(value = "openid", required = false) String openid,
                           @RequestParam BestPayTypeEnum payType,
                           @RequestParam String orderId,
                           @RequestParam Double amount,
                           @RequestParam(required = false) String buyerLogonId,
                           @RequestParam(required = false) String buyerId) {
        //支付请求参数
        PayRequest request = new PayRequest();
        request.setPayTypeEnum(payType);
        request.setOrderId(orderId);
        request.setOrderAmount(amount);
        request.setOrderName("最好的支付sdk");
        request.setOpenid(openid);
        request.setAttach("这里是附加的信息");

        if (payType == BestPayTypeEnum.ALIPAY_H5) {
            request.setBuyerLogonId(buyerLogonId);
            request.setBuyerId(buyerId);
        }

        log.info("【发起支付】request={}", JsonUtil.toJson(request));

        PayResponse payResponse = bestPayService.pay(request);
        log.info("【发起支付】response={}", JsonUtil.toJson(payResponse));
        return payResponse;
    }

    /**
     * 微信h5支付，要求referer是白名单的地址，这里做个重定向
     * @param prepayId
     * @param packAge
     * @return
     */
    @GetMapping("/wxpay_mweb_redirect")
    public ModelAndView wxpayMweb(@RequestParam("prepay_id") String prepayId,
                                  @RequestParam("package") String packAge,
                                  Map map) {
        String url = String.format("https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=%s&package=%s", prepayId, packAge);
        map.put("url", url);
        return new ModelAndView("pay/wxpayMwebRedirect");
    }

    @GetMapping("/query")
	@ResponseBody
	public OrderQueryResponse query(@RequestParam String orderId,
                                    @RequestParam("platform") BestPayPlatformEnum platformEnum) {
		OrderQueryRequest orderQueryRequest = new OrderQueryRequest();
		orderQueryRequest.setOrderId(orderId);
		orderQueryRequest.setPlatformEnum(platformEnum);
		OrderQueryResponse queryResponse = bestPayService.query(orderQueryRequest);
		return queryResponse;
	}

    @GetMapping("/refund")
    @ResponseBody
    public RefundResponse refund(@RequestParam String orderId) {
        RefundRequest request = new RefundRequest();
        request.setOrderId(orderId);
        request.setPayTypeEnum(BestPayTypeEnum.WXPAY_MWEB);
        request.setOrderAmount(0.1);
        RefundResponse response = bestPayService.refund(request);
        return response;
    }

    /**
     * 小程序支付
     * @param code
     * @return
     */
    @GetMapping(value = "/mini_pay")
    @ResponseBody
    public PayResponse minipay(@RequestParam(value = "code") String code){

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+wechatAccountConfig.getMiniAppId()+"&secret="+wechatAccountConfig.getMiniAppSecret()+"&js_code="+code+"&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String userInfo = restTemplate.getForObject(url, String.class);

        Random random = new Random();
        DateTime dateTime = new DateTime(new Date());
        PayRequest payRequest = new PayRequest();
        payRequest.setOpenid(((String) new Gson().fromJson(userInfo, Map.class).get("openid")));
        payRequest.setOrderAmount(0.01);
        payRequest.setOrderId(System.currentTimeMillis() + String.valueOf(random.nextInt(900000) + 100000)+dateTime.toString("yyyymmdd")+String.valueOf(random.nextInt(90000) + 10000));
        payRequest.setOrderName("小程序支付");
        payRequest.setPayTypeEnum(BestPayTypeEnum.WXPAY_MINI);
        log.info("【发起支付】request={}", JsonUtil.toJson(payRequest));
        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("【发起支付】response={}", JsonUtil.toJson(payResponse));
        return payResponse;
    }

    /**
     * 发起支付
     */
    @GetMapping(value = "/alipay/pay")
    public ModelAndView aliPay(Map<String, Object> map) {
        PayRequest request = new PayRequest();
        Random random = new Random();
        //支付请求参数
        request.setPayTypeEnum(BestPayTypeEnum.ALIPAY_PC);
        request.setOrderId(String.valueOf(random.nextInt(1000000000)));
        request.setOrderAmount(0.01);
        request.setOrderName("最好的支付sdk");
        log.info("【发起支付】request={}", JsonUtil.toJson(request));
        PayResponse payResponse = bestPayService.pay(request);
        log.info("【发起支付】response={}", JsonUtil.toJson(payResponse));
        map.put("payResponse", payResponse);

        return new ModelAndView("pay/alipayPc", map);
    }

    /**
     * 异步回调
     */
    @PostMapping(value = "/notify")
    public ModelAndView notify(@RequestBody String notifyData) {
        log.info("【异步通知】支付平台的数据request={}", notifyData);
        PayResponse response = bestPayService.asyncNotify(notifyData);
        log.info("【异步通知】处理后的数据data={}", JsonUtil.toJson(response));

        //返回成功信息给支付平台，否则会不停的异步通知
        if (response.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            return new ModelAndView("pay/responeSuccessForWx");
        }else if (response.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return new ModelAndView("pay/responeSuccessForAlipay");
        }
        throw new RuntimeException("错误的支付平台");
    }

    @GetMapping("/pay/close")
    @ResponseBody
    public CloseResponse close(@RequestParam String orderId) {
        CloseRequest request = new CloseRequest();
        request.setPayTypeEnum(BestPayTypeEnum.ALIPAY_PC);
        request.setOrderId(orderId);

        CloseResponse close = bestPayService.close(request);
        return close;
    }
}
