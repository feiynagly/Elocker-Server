package controller;

import dao.UserDao;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;
import reqhandler.RequestHandler;
import util.RedisUtil;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static constant.Status.*;

@Controller
public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class);

    @Value("${core.http.max_idle_time}")
    private int maxHttpIdle;

    @Value("${core.api.sign_timeout}")
    private int signTimeout;

    @Value("${core.api.max_request_count}")
    private int maxRequestCount;

    @Resource(name = "requestHandlerMap")
    private HashMap<String, String> handlerClassMap;

    @Resource(name = "viewMap")
    private HashMap<String, String> viewMap;

    @Resource(name = "unNeedAuthenticateView")
    private List<String> unNeedAuthenticaionViews;

    @Resource(name = "unNeedAuthenticateHandler")
    private List<String> unNeedAuthenticateHandler;

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserDao userDao;

    private String token;

    private String phoneNum;

    /*处理Cookie*/
    private HashMap<String, String> handleCookies(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, String> cookieData = new HashMap<String, String>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                cookieData.put(cookie.getName(), cookie.getValue());
        this.token = cookieData.get("token");
        if (this.token == null) {
            this.token = UUID.randomUUID().toString().replace("-", "");
            Cookie cookie = new Cookie("token", this.token);
            cookie.setMaxAge(maxHttpIdle);
            response.addCookie(cookie);
            cookieData.put("token", this.token);
        }
        return cookieData;
    }

    /*将URL请求字符串转换为Map类型*/
    //?mt=1&deviceId=2
    private HashMap<String, String> handleUrlParam(HttpServletRequest request) throws UnsupportedEncodingException {
        HashMap<String, String> urlParam = new HashMap<String, String>();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.equals("")) {
            /*进行两次解码，解决中文乱码问题（对应前端也需要两次编码）*/
            queryString = URLDecoder.decode(queryString, "UTF-8");
            queryString = URLDecoder.decode(queryString, "UTF-8");
            String[] queryParam = queryString.split("&");
            for (int i = 0; i < queryParam.length; i++) {
                String[] keyValuePaire = queryParam[i].split("=");
                if (keyValuePaire.length == 2)
                    urlParam.put(keyValuePaire[0].trim(), keyValuePaire[1].trim());
            }
        }
        return urlParam;
    }

    /*认证成功返回SUCCESS，
     *认证失败返回UNAUTHORISED_REQUEST，
     *请求过于频繁返回REQUEST_TOO_FREQUENTLY
     * 内部错误返回 UNKNOWN_ERROR
     */
    private short handleAuthentication(HashMap<String, String> urlParam,
                                       JSONObject postData, HttpServletRequest request) {
        Jedis jedis = redisUtil.getJedis();
        if (jedis == null) {
            return UNKNOWN_ERROR;
        }
        short result = UNAUTHORISED_REQUEST;
        /*对于包含Cookie的请求（WEB页面请求），直接验证token是否正确即可*/
        if (this.token != null && jedis.exists(this.token)) {
            result = SUCCESS;
            this.phoneNum = jedis.get(token);
            /*认证通过更新token超时时间*/
            jedis.expire(this.token, maxHttpIdle);
        } else {
            /*对于不包含cookie的GET请求（API请求）,通过数字签名认证。把整个URL请求路径后的部分和用户名、
             *密码进行hash作为签名
             * 例如 https://api.elocker.com.cn/locker/get?appid=phoneNum,sign=abcdef
             * sign=md5(locker/get+md5(phoneNum+md5(password))*/
            String url = request.getRequestURI();
            String requestSign = "";
            String password = "";
            if (request.getMethod().equals("GET")) {
                this.phoneNum = urlParam.get("appid");
                requestSign = urlParam.get("sign");
            }
            /*对于不包含cookie的POST请求（API请求）,通过数字签名认证。把整个URL和用户名、密码进行hash作为签名
             * 例如 https://api.elocker.com.cn/locker/get
             * postData中必须至少包含appid,sign
             * sign=md5(locker/get+md5(phoneNum+md5(password))*/
            else if (request.getMethod().equals("POST")) {
                this.phoneNum = postData.has("appid") ? postData.getString("appid") : null;
                requestSign = postData.has("sign") ? postData.getString("sign") : null;
            }

            if (this.phoneNum != null && requestSign != null) {
                /*如果单位时间内访问次数过多，直接返回错误*/
                jedis.set(this.phoneNum + "c", String.valueOf(maxRequestCount), "NX", "EX", signTimeout);
                if (Integer.parseInt(jedis.get(this.phoneNum + "c")) <= 0) {
                    logger.warn(this.phoneNum + " request too frequently");
                    redisUtil.returnResource(jedis);
                    return REQUEST_TOO_FREQUENTLY;
                }
                /*首先查找redis缓存是否有对应的签名sign，如果存在则直接返回认证通过*/
                if (jedis.exists(requestSign)) {
                    result = SUCCESS;
                } else {
                    password = userDao.getPassword(this.phoneNum);
                    String sign = DigestUtils.md5Hex(url + password);
                    if (sign.equalsIgnoreCase(requestSign)) {
                        result = SUCCESS;
                        jedis.set(sign, this.phoneNum, "NX", "EX", signTimeout);
                    }
                }
                jedis.decr(this.phoneNum + "c");
            }
        }
        redisUtil.returnResource(jedis);
        if (this.phoneNum == null)
            result = INVALID_PHONE_NUMBER;
        return result;
    }

    /*处理页面请求*/
    @RequestMapping(value = "{viewname}", method = RequestMethod.GET)
    public ModelAndView viewRequestHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String viewname
    ) throws UnsupportedEncodingException {
        ModelAndView mav = new ModelAndView();

        /*设置编码*/
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HashMap<String, String> cookieData = handleCookies(request, response);

        /*对于需要认证的页面，如果redis数据库中查询不到该token，则直接返回登录页*/
        Jedis jedis = redisUtil.getJedis();
        if (!jedis.exists(this.token) && !unNeedAuthenticaionViews.contains(viewname)) {
            mav.setViewName(viewMap.get("default"));
            jedis.close();
            return mav;
        }
        redisUtil.returnResource(jedis);

        /*视图处理*/
        if (viewMap.containsKey(viewname))
            mav.setViewName(viewMap.get(viewname));
        else {
            logger.error("View " + viewname + " does not exist,redirect to login page");
            mav.setViewName(viewMap.get("default"));
        }
        return mav;
    }

    /*处理浏览器Ajax请求,根据module确定执行类，根据handler确定具体执行的函数*/
    @RequestMapping(value = "{module}/{handler}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JSONObject requestHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String module,
            @PathVariable String handler,
            @RequestBody(required = false) JSONObject postData
    ) throws UnsupportedEncodingException {
        JSONObject responseData = new JSONObject();

        /*设置编码格式*/
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        /*获取参数*/
        HashMap<String, String> urlParam = handleUrlParam(request);
        HashMap<String, String> cookieData = handleCookies(request, response);

        /*如果需要认证*/
        if (!unNeedAuthenticateHandler.contains(module + "/" + handler)) {
            short status = handleAuthentication(urlParam, postData, request);
            switch (status) {
                case UNAUTHORISED_REQUEST: {
                    responseData.put("error", "Unauthorised request");
                    responseData.put("status", UNAUTHORISED_REQUEST);
                    response.setStatus(UNAUTHORISED_REQUEST);
                    return responseData;
                }
                case REQUEST_TOO_FREQUENTLY: {
                    responseData.put("error", "Request too frequency");
                    responseData.put("status", REQUEST_TOO_FREQUENTLY);
                    response.setStatus(REQUEST_TOO_FREQUENTLY);
                    return responseData;
                }
                case UNKNOWN_ERROR: {
                    responseData.put("error", "Unknow error");
                    responseData.put("status", UNKNOWN_ERROR);
                    response.setStatus(UNKNOWN_ERROR);
                    return responseData;
                }
                case INVALID_PHONE_NUMBER: {
                    responseData.put("error", "Invalid user phone number");
                    responseData.put("status", INVALID_PHONE_NUMBER);
                    response.setStatus(INVALID_PHONE_NUMBER);
                    return responseData;
                }
                case SUCCESS:
                default:
                    logger.debug("request authentication successfully");
            }
        }

        /*查找具体执行类，如果找不到则返回报错信息*/
        String handlerClassName = handlerClassMap.get(module);
        if (handlerClassName == null) {
            responseData.put("error", "Internal error");
            responseData.put("status", UNKNOWN_ERROR);
            response.setStatus(UNKNOWN_ERROR);
            logger.error("Failed to find handlerClassName in handlerClassMap for " + module);
            return responseData;
        }

        ApplicationContext applicationContext = applicationContextProvider.getApplicationContext();
        RequestHandler requestHandler;

        try {
            requestHandler = (RequestHandler) applicationContext.getBean(handlerClassName);
            requestHandler.initParam(this.phoneNum, request, urlParam, postData, responseData, cookieData);
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("No bean named:" + handlerClassName);
            responseData.put("error", "Internal Error");
            responseData.put("status", UNKNOWN_ERROR);
            response.setStatus(UNKNOWN_ERROR);
            return responseData;
        } catch (Exception e) {
            logger.error("unknow error, can not find java bean " + handlerClassName);
            responseData.put("error", "Unknown internal error");
            responseData.put("status", UNKNOWN_ERROR);
            response.setStatus(UNKNOWN_ERROR);
            return responseData;
        }

        /*根据参数handler调用执行类的具体方法*/
        HashMap<String, Method> methodData = new HashMap<String, Method>();

        for (Method method : requestHandler.getClass().getDeclaredMethods())
            methodData.put(method.getName(), method);
        try {
            methodData.get(handler).invoke(requestHandler);
        } catch (IllegalAccessException e) {
            logger.error("Failed to call method " + handler);
            responseData.put("error", "Internal error");
        } catch (InvocationTargetException e) {
            logger.error("Failed to call method " + handler + " ,No method " + handler + " in class " + handlerClassName + " or parameters are wrong");
            responseData.put("error", "Internal error");
        } catch (Exception e) {
            logger.error("Unknown error, module: " + module + " ,method: " + handler);
            responseData.put("error", "Internal error");
        }

        /*设置状态码*/
        if (responseData.has("status"))
            response.setStatus(Integer.parseInt(responseData.getString("status")));
        return responseData;
    }

    public void setHandlerClassMap(HashMap<String, String> handlerClassMap) {
        this.handlerClassMap = handlerClassMap;
    }

    public void setViewMap(HashMap<String, String> viewMap) {
        this.viewMap = viewMap;
    }

    public void setUnNeedAuthenticaionViews(List<String> unNeedAuthenticaionViews) {
        this.unNeedAuthenticaionViews = unNeedAuthenticaionViews;
    }

    public void setUnNeedAuthenticateHandler(List<String> unNeedAuthenticateHandler) {
        this.unNeedAuthenticateHandler = unNeedAuthenticateHandler;
    }

    public void setApplicationContextProvider(ApplicationContextProvider applicationContextProvider) {
        this.applicationContextProvider = applicationContextProvider;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}