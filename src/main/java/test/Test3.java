package test;

import org.apache.commons.codec.digest.DigestUtils;

public class Test3 {
    public static void main(String args[]) {
        String url = "/locker/get";
        System.out.println("MD5: " + DigestUtils.md5Hex(url));
        String pass = DigestUtils.md5Hex("15851841387" + DigestUtils.md5Hex("12345678"));
        String apiKey = "033f4ac44451480b86816f3a14933081";
        String partToken = DigestUtils.md5Hex(pass + apiKey);
        System.out.println("Pass: " + pass);
        System.out.println("partToken: " + partToken);
        System.out.println("Token:" + DigestUtils.md5Hex(url + partToken));
    }
}
