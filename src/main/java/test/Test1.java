package test;

import redis.clients.jedis.Jedis;

public class Test1 {
    public static void main(String args[]) {
        Jedis jedis = new Jedis("10.0.10.100", 6379);
        jedis.auth("feiyang");
        String key = "15851841387" + "c";
        jedis.set("15851841387" + "c", String.valueOf(10), "NX", "EX", 30);
        System.out.println("remain time: " + jedis.get("15851841387" + "c"));
        jedis.decr("15851841387" + "c");
        System.out.println("ttl: " + jedis.ttl("15851841387" + "c"));
        jedis.close();

    }
}
