package test;

import constant.Constant;

import java.text.SimpleDateFormat;

public class Test1 {
    public static void main(String args[]) {
        /*Jedis redis  = new Jedis("10.0.10.100",6379);
        redis.auth("feiyang");
        System.out.println("".equals(redis.get("code")));
        redis.close();*/

        String date = "2019-7-1 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_PATTERN);
        try {
            System.out.println(sdf.format(sdf.parse(null)));
        } catch (Exception e) {
            System.out.println("Error");
        }
    }
}
