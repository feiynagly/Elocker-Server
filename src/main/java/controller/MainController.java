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

import static constant.Status.*;

@Controller
public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class);

    /*token超时时间*/
    @Value("${core.api.token_timeout}")
    private int tokenTimeout;

    /*验证码超时时间*/
    @Value("${core.api.verification_code_timeout}")
    private int verificationCodeTimeout;

    /*验证码超时时间前，最大发送验证码次数*/
    @Value("${core.api.max_send_msg_request_count}")
    private int maxSendMsgRequestCount;

    /*请求计数器超时时间*/
    @Value("${core.api.request_count_timeout}")
    private int requestCountTimeout;

    /*在请求计数器超时前最大请求次数*/
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

    private String apiKey;

    private String phoneNum;

    /*获取token和apiKey 查找顺序依次为：1、header , 2、cookie*/
    private void getToken(HttpServletRequest request) {

        this.token = request.getHeader("Token");
        this.apiKey = request.getHeader("Api-Key");
        if (this.token != null && this.apiKey != null) {
            return;
        }

        /*Header中未找到token和apiKey，从cookie中获取*/
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (cookie.getName().toLowerCase().equals("token"))
                    this.token = cookie.getValue();
                else if (cookie.getName().toLowerCase().equals("api-key")) {
                    this.apiKey = cookie.getValue();
                }
            }
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

    private short handleAuthentication(HttpServletRequest request) {
        this.getToken(request);

        if (this.token == null) {
            return INVALID_TOKEN;
        }
        this.phoneNum = request.getHeader("App-Id");
        if (this.phoneNum == null) {
            return INVALID_PHONE_NUMBER;
        }

        Jedis jedis = redisUtil.getJedis();
        if (jedis == null) {
            logger.error("Failed get redis instance from pool");
            return UNKNOWN_ERROR;
        }

        /*在Redis查找是否有缓存*/
        if (jedis.exists(this.phoneNum + "token")) {
            String url = request.getRequestURI();
            String sToken = DigestUtils.md5Hex(url + jedis.get(this.phoneNum + "token"));
            if (this.token.equals(sToken)) {
                /*认证通过更新token超时时间*/
                jedis.expire(this.phoneNum + "token", tokenTimeout);
                redisUtil.returnResource(jedis);
                return SUCCESS;
            } else {
                return UNAUTHORISED_REQUEST;
            }
        }

        /*Redis数据库中没有相应用于计算token的缓存*/
        String sApiKey = userDao.getApiKey(this.phoneNum);
        String sPassword = userDao.getPassword(this.phoneNum);
        String url = request.getRequestURI();

        if (sApiKey == null || sPassword == null) {
            redisUtil.returnResource(jedis);
            return UNKNOWN_ERROR;
        } else if (this.apiKey == null) {
            redisUtil.returnResource(jedis);
            return INVALID_API_KEY;
        } else if (!this.apiKey.equals(sApiKey)) {
            redisUtil.returnResource(jedis);
            return MULTI_ENDPOINT_LOGIN;
        }

        String partToken = DigestUtils.md5Hex(sPassword + sApiKey);
        String sToken = DigestUtils.md5Hex(url + partToken);
        if (sToken.equals(this.token)) {
            jedis.setex(this.phoneNum + "token", tokenTimeout, partToken);
            redisUtil.returnResource(jedis);
            return SUCCESS;
        }
        redisUtil.returnResource(jedis);
        return UNAUTHORISED_REQUEST;
    }

    /*
     * 校验请求是否在规定时间内多次发送，
     * 校验规则：统计单位时间请求次数(相同的源地址+端口号为同一请求)
     * @param request
     * @param uri 对于POST请求，uri=module/handler ，对于get请求,uri=viewname
     * @return boolean
     */
    private boolean isRequestFrequencyValid(HttpServletRequest request, String uri) {
        boolean isValid = true;
        String ip_add = request.getRemoteAddr();
        int port = request.getRemotePort();

        Jedis redis = redisUtil.getJedis();
        String key;
        /*设置验证码指定时间内获取次数*/
        if (uri.toLowerCase().contains("code")) {
            key = ip_add + port + "c";
            redis.set(key, String.valueOf(maxSendMsgRequestCount),
                    "NX", "EX", verificationCodeTimeout);

        } else {
            /*设置其它非验证码请求单位时间内最大请求次数*/
            key = ip_add + port + "r";
            redis.set(key, String.valueOf(maxRequestCount),
                    "NX", "EX", requestCountTimeout);
        }
        redis.decr(key);
        if (Integer.parseInt(redis.get(key)) <= 0) {
            logger.error("Request too frequently !" + " host: " + request.getRemoteAddr()
                    + " ,port: " + request.getRemotePort() + " ,url: " + request.getRequestURL()
                    + "method: " + request.getMethod() + "queryString: " + request.getQueryString());
            isValid = false;
        }
        redisUtil.returnResource(redis);
        return isValid;
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

        /*如果请求过于频繁，返回错误页面*/
        if (!isRequestFrequencyValid(request, viewname)) {
            mav.setViewName(viewMap.get("error"));
            return mav;
        }

        /*对于需要认证的页面进行认证*/
        if (!unNeedAuthenticaionViews.contains(viewname)
                && handleAuthentication(request) != SUCCESS) {
            mav.setViewName(viewMap.get("default"));
            return mav;
        }

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

        /*如果请求过于频繁，返回错误*/
        if (!isRequestFrequencyValid(request, handler)) {
            responseData.put("error", "Request too frequently");
            responseData.put("status", REQUEST_TOO_FREQUENTLY);
            response.setStatus(REQUEST_TOO_FREQUENTLY);
            return responseData;
        }

        /*设置编码格式*/
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        HashMap<String, String> urlParam = handleUrlParam(request);

        /*如果需要认证，进行认证*/
        if (!unNeedAuthenticateHandler.contains(module + "/" + handler)) {
            short status = handleAuthentication(request);
            switch (status) {
                case SUCCESS:
                    Cookie cookie = new Cookie("token", this.token);
                    cookie.setMaxAge(tokenTimeout);
                    response.addCookie(cookie);
                    break;
                case UNAUTHORISED_REQUEST: {
                    responseData.put("message", "Unauthorised request");
                    responseData.put("status", UNAUTHORISED_REQUEST);
                    response.setStatus(UNAUTHORISED_REQUEST);
                    return responseData;
                }
                case UNKNOWN_ERROR: {
                    responseData.put("message", "Unknow error");
                    responseData.put("status", UNKNOWN_ERROR);
                    response.setStatus(UNKNOWN_ERROR);
                    return responseData;
                }
                case INVALID_PHONE_NUMBER: {
                    responseData.put("message", "param appid required");
                    responseData.put("status", INVALID_PHONE_NUMBER);
                    response.setStatus(INVALID_PHONE_NUMBER);
                    return responseData;
                }
                case INVALID_TOKEN: {
                    responseData.put("message", "param sign required");
                    responseData.put("status", INVALID_TOKEN);
                    response.setStatus(INVALID_TOKEN);
                    return responseData;
                }
                case MULTI_ENDPOINT_LOGIN:
                    responseData.put("message", "Multi endpoint login");
                    responseData.put("status", MULTI_ENDPOINT_LOGIN);
                    response.setStatus(MULTI_ENDPOINT_LOGIN);
                    return responseData;
                default:
                    break;
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
            requestHandler.initParam(this.phoneNum, request, urlParam, postData, responseData, this.token);
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
            responseData.put("message", "Internal error");
        } catch (InvocationTargetException e) {
            logger.error("Failed to call method " + handler + " ,No method " + handler + " in class " + handlerClassName + " or parameters are wrong");
            responseData.put("message", "Internal error");
        } catch (Exception e) {
            logger.error("Unknown error, module: " + module + " ,method: " + handler);
            responseData.put("message", "Internal error");
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