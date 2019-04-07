package com.cyrs.finallove;

import com.cyrs.finallove.Util.MessageUtil;
import com.cyrs.finallove.Util.TextMessageUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@RestController
@EnableAutoConfiguration
public class App {
    private String TOKEN = "good";


    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    @RequestMapping(value = "/wx/test",method= RequestMethod.GET)
    public String wxCheck(@RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("echostr") String echostr, HttpServletRequest request, HttpServletResponse response) {


        return wxCheck(signature, timestamp, nonce, echostr);

    }


    @RequestMapping(value = "/wx/test",method= RequestMethod.POST)
    public void doPostMsg(HttpServletRequest request,HttpServletResponse response)
    {
        System.out.println("开始回复消息");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = null;
        //将微信请求xml转为map格式，获取所需的参数
        Map<String,String> map = MessageUtil.xmlToMap(request);
        String ToUserName = map.get("ToUserName");
        String FromUserName = map.get("FromUserName");
        String MsgType = map.get("MsgType");
        String Content = map.get("Content");

        String message = null;
        //处理文本类型，实现输入1，回复相应的封装的内容
        if("text".equals(MsgType)){
            if("linlexuan".equals(Content)){
                TextMessageUtil textMessage = new TextMessageUtil();
                message = textMessage.initMessage(FromUserName, ToUserName);
            }
        }
        try {
            out = response.getWriter();
            out.write(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        out.close();
    }

    private String wxCheck(@RequestParam("signature") String signature, @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce, @RequestParam("echostr") String echostr) {
        String sortString = sort(TOKEN, timestamp, nonce);
        //加密
        String myString = sha1(sortString);
        //校验
        if (myString != null && myString != "" && myString.equals(signature)) {
            System.out.println("签名校验通过");
            //如果检验成功原样返回echostr，微信服务器接收到此输出，才会确认检验完成。
            System.out.println("签名校验通过");
            return echostr;
        } else {
            System.out.println("签名校验失败");
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    public String sort(String token, String timestamp, String nonce) {
        String[] strArray = {token, timestamp, nonce};
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (String str : strArray) {
            sb.append(str);
        }

        return sb.toString();
    }

    public String sha1(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}