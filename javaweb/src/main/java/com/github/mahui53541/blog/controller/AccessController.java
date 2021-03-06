package com.github.mahui53541.blog.controller;

import com.github.mahui53541.blog.po.User;
import com.github.mahui53541.blog.po.VisitorRecord;
import com.github.mahui53541.blog.service.CommentService;
import com.github.mahui53541.blog.service.PostService;
import com.github.mahui53541.blog.service.VisitorRecordService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mahui
 * @create 2017-06-13 22:18
 **/
@Controller
public class AccessController extends BaseController{

    @Autowired
    private VisitorRecordService visitorRecordService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private SessionDAO sessionDAO;

    @RequestMapping(value="/oauth2-login",method = RequestMethod.GET)
    @ResponseBody
    public User login(HttpServletRequest request)throws Exception{
        //如果登陆失败从request中获取认证异常信息，shiroLoginFailure就是shiro异常类的全限定名
        String exceptionClassName = (String) request.getAttribute("shiroLoginFailure");
        //根据shiro返回的异常类路径判断，抛出指定异常信息
        if(exceptionClassName!=null){
            if (LockedAccountException.class.getName().equals(exceptionClassName)) {
                //账户被锁定
                throw new LockedAccountException("账号不存在");
            } else {
                throw new Exception();//最终在异常处理器生成未知错误
            }
        }
        User user= (User) SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> logout() throws Exception {
        try {
            Session session=SecurityUtils.getSubject().getSession(false);
            if(session!=null){
                VisitorRecord visitorRecord= (VisitorRecord) session.getAttribute("visitorRecord");
                if(visitorRecord!=null){
                    visitorRecord.setOutDate(new Date());
                    visitorRecordService.updateByPrimaryKeySelective(visitorRecord);
                }
            }else{
                return this.ajaxFailureResponse("回话已过期");
            }
        }catch (Exception e){
            SecurityUtils.getSubject().logout();
            return this.ajaxSuccessResponse();
        }
        SecurityUtils.getSubject().logout();
        return this.ajaxSuccessResponse();
    }

    @RequestMapping(value="/info",method = RequestMethod.GET)
    @ResponseBody
    public Object info()throws Exception{
        if(!SecurityUtils.getSubject().isAuthenticated()){
            return this.ajaxFailureResponse("未登录");
        }else{
            Session session=SecurityUtils.getSubject().getSession(false);
            if(session!=null){
                HashMap<String,Object> result=new HashMap<String,Object>();
                result.put("user",session.getAttribute("user"));
                result.put("permission",session.getAttribute("permission"));
                return result;
            }else{
                return this.ajaxFailureResponse("回话已过期");
            }
        }
    }

    @RequestMapping(value="/siteStat",method = RequestMethod.GET)
    @ResponseBody
    public HashMap<String,Object> siteStat()throws Exception{
        HashMap<String,Object> result=new HashMap<String,Object>();
        result.put("onlineUsers",sessionDAO.getActiveSessions().size());
        result.put("postNum",postService.countAll());
        result.put("readNum",postService.countReadTimes());
        result.put("commentNum",commentService.countAll());
        return result;
    }
}
