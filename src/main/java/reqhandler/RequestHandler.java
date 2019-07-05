package reqhandler;

import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public abstract class RequestHandler {
    public HttpServletRequest request;
    public HashMap<String, String> urlParam;
    public JSONObject postData;
    public JSONObject responseData;
    public HashMap<String, String> cookieData;
    public String token;
    public String phoneNum;

    public void initParam(String phoneNum, HttpServletRequest request, HashMap<String, String> urlParam,
                          JSONObject postData, JSONObject responseData, HashMap<String, String> cookieData) {
        this.request = request;
        this.urlParam = urlParam;
        this.postData = postData;
        this.responseData = responseData;
        this.cookieData = cookieData;
        this.token = cookieData.get("token");
        this.phoneNum = phoneNum;
    }
}
