/***
 * @pName management
 * @name UserController
 * @user DF
 * @date 2018/8/16
 * @desc
 */
package com.kafka.sms.apiController;

import com.fasterxml.jackson.annotation.JsonView;
import com.kafka.sms.entity.JsonArrayResult;
import com.kafka.sms.entity.JsonResult;
import com.kafka.sms.entity.resp.MerchantBusiness;
import com.kafka.sms.entity.resp.UserResp;
import com.kafka.sms.utils.JsonUtil;
import com.kafka.sms.utils.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserApiController {
    @Autowired
    private com.kafka.sms.biz.IUserService userService;


    @GetMapping("/getUserInfo")
    @JsonView(UserResp.FinanceView.class)
    /**
     * 查询用户详情  韦德 2018年8月17日00:34:37
     * @param token
     * @return
     */
    public JsonResult<UserResp> getUserInfo(String token){
        UserResp userResp = userService.getUserDetailByToken(token);
        return new JsonResult<>().successful(userResp);
    }


    @PostMapping("/editPassword")
    /**
     * 修改密码 韦德 2018年8月17日00:34:25
     * @param token
     * @param password
     * @param newPassword
     * @return
     */
    public JsonResult editPassword(String token, String password, String newPassword){
        userService.editPassword(token, password, newPassword);
        return new JsonResult<>().successful();
    }


    @PostMapping("/bindFinanceAccount")
    /**
     * 绑定财务账户 韦德 2018年8月17日00:34:25
     * @param token
     * @param account
     * @param accountName
     * @return
     */
    public JsonResult bindFinanceAccount(String token, String account, String accountName){
        userService.bindFinanceAccount(token, account, accountName);
        return JsonResult.successful();
    }



    @PostMapping("/bindPandaAccount")
    /**
     * 绑定熊猫麻将账户 韦德 2018年9月19日22:13:01
     * @param token
     * @param account
     * @return
     */
    public JsonResult bindPandaAccount(String token, String account){
        userService.bindPandaAccount(token, account);
        return JsonResult.successful();
    }

    @GetMapping("/getMerchantBusinessList")
    /**
     * 绑定熊猫麻将账户 韦德 2018年9月19日22:13:01
     * @param token
     * @param account
     * @return
     */
    public JsonResult<MerchantBusiness> getMerchantBusinessList(String token){
        MerchantBusiness list = userService.getMerchantBusinessList(token);
        return new JsonResult<>().successful(list);
    }

    @GetMapping("/getMerchantQRCode")
    public String getMerchantQRCode(String content, HttpServletResponse response) throws Exception {
        /*String path = "c:\\images_server";
        String file =  QRCodeUtil.encode(content, null, path, true);
        path += "\\" + file;
        BufferedImage read = ImageIO.read(new File(path));
        OutputStream outputStream = response.getOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(read, "jpg", out);
        outputStream.write(out.toByteArray());*/
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QRCodeUtil.encode(content, outputStream);

       /* ServletOutputStream stream = response.getOutputStream();
        stream.write(outputStream.toByteArray());
        stream.flush();
        stream.close();*/
       return "data:image/png;base64," + JsonUtil.getJson(outputStream.toByteArray());
    }

    @GetMapping("/getMerchantQRCodeImage")
    public void getMerchantQRCodeImage(String content, HttpServletResponse response) throws Exception {
        /*String path = "c:\\images_server";
        String file =  QRCodeUtil.encode(content, null, path, true);
        path += "\\" + file;
        BufferedImage read = ImageIO.read(new File(path));
        OutputStream outputStream = response.getOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(read, "jpg", out);
        outputStream.write(out.toByteArray());*/
        response.setContentType("image/png");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QRCodeUtil.encode(content, outputStream);

        ServletOutputStream stream = response.getOutputStream();
        stream.write(outputStream.toByteArray());
        stream.flush();
        stream.close();
    }
}