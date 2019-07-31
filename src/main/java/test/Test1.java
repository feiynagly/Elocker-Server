package test;

import net.sf.json.JSONObject;
import util.HttpsUtil;

import java.util.HashMap;

public class Test1 {
    public static void main(String args[]) {
        JSONObject params = new JSONObject();
        params.put("apikey", 123);
        params.put("text", "text");
        params.put("mobile", "12345678");

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json;charset=utf-8");
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        JSONObject res = HttpsUtil.post("https://sms.yunpian.com/v2/sms/single_send.json", params, headers);
        System.out.println(res.getInt("status"));
        System.out.println(res.getJSONObject("response"));

    }
}
