package com.mall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.mall.auth.feign.MemberServiceFeign;
import com.mall.auth.service.IOAuthService;
import com.mall.auth.vo.WeiBoInfoVo;
import com.mall.common.utils.HttpUtils;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class OAuthServiceImpl implements IOAuthService {


    public static final String ACCESS_TOKEN_HOST = "https://api.weibo.com";
    public static final String ACCESS_TOKEN_PATH = "/oauth2/access_token";

    @Autowired
    MemberServiceFeign memberServiceFeign;

    @Override
    public MemberEntity oauthLogin(String code) throws Exception {
        HashMap<String, String> header = new HashMap<>();
        HashMap<String, String> query = new HashMap<>();
        HashMap<String, String> body = new HashMap<>();
        body.put("client_id", "1462238754");
        body.put("client_secret", "0c4078d8cdaab4f17915dd37013a8ded");
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", "http://auth.mall.com/auth/oauth2/weibo");
        HttpResponse response = HttpUtils.doPost(ACCESS_TOKEN_HOST, ACCESS_TOKEN_PATH, "POST", header, query, body);
        if (response.getStatusLine().getStatusCode() != 200) {
            return null;
        } else {
            String info = EntityUtils.toString(response.getEntity());
            WeiBoInfoVo infoVo = JSON.parseObject(info, WeiBoInfoVo.class);
            R r = memberServiceFeign.oAuthWeiBo(infoVo);
            MemberEntity entity = r.get("user", MemberEntity.class);
            return entity;
        }
    }
}
