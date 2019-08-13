package test;

import java.util.UUID;

public class Test1 {
    public static void main(String args[]) {
        /*Jedis redis  = new Jedis("10.0.10.100",6379);
        redis.auth("feiyang");
        System.out.println("".equals(redis.get("code")));
        redis.close();*/

        System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
    }
}
