package com.github.mahui53541.blog.auth;

import com.github.mahui53541.blog.domain.User;
import com.github.mahui53541.blog.exception.OAuth2AuthenticationException;
import com.github.mahui53541.blog.service.UserService;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author mahui
 * @create 2017-06-13 16:17
 **/
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private UserService userService;
    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof OAuth2Token;//表示此Realm只支持OAuth2Token类型
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        OAuth2Token oAuth2Token = (OAuth2Token) token;
        String code = oAuth2Token.getAuthCode();
        User user = extractUser(code);
        System.out.println("登录依次");
        SimpleAuthenticationInfo authenticationInfo =
                new SimpleAuthenticationInfo(user, code, getName());
        return authenticationInfo;
    }

    private User extractUser(String code) {
        try {
            String queryString="code="+code+"&state=blog";
            AccessToken accessTokenObj = (new Oauth()).getAccessTokenByQueryString(queryString,"blog");

            String accessToken = accessTokenObj.getAccessToken();

            //Long expiresIn = accessTokenObj.getExpireIn();
            OpenID openIDObj =  new OpenID(accessToken);
            String openID = openIDObj.getUserOpenID();

            UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);
            UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();

            User user = userService.selectByOpenID(openID);
            if(user!=null){
                user.setGender(userInfoBean.getGender());
                user.setFigureUrl(userInfoBean.getAvatar().getAvatarURL30());
                user.setNickName(userInfoBean.getNickname());
                if(user.getStatus()==1 && user.getDisabledTime().before(new Date())){
                    user.setStatus((byte)0);
                }
                userService.updateByPrimaryKeySelective(user);
            }else{
                user=new User();
                user.setOpenId(openID);
                user.setStatus((byte)0);
                user.setGender(userInfoBean.getGender());
                user.setFigureUrl(userInfoBean.getAvatar().getAvatarURL30());
                user.setNickName(userInfoBean.getNickname());
                userService.insertSelective(user);
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OAuth2AuthenticationException(e);
        }
    }
}
