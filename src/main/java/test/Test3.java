package test;

import org.apache.commons.codec.digest.DigestUtils;

public class Test3 {
    public static void main(String args[]) {
        String url = "/locker/add";
        System.out.println("MD5: " + DigestUtils.md5Hex(url));
        String pass = DigestUtils.md5Hex("15851841387" + DigestUtils.md5Hex("12345678"));
        String apiKey = "e0855de893f3438ba0ba7fcb0d8aab7b";
        String partToken = DigestUtils.md5Hex(pass + apiKey);
        System.out.println("Pass: " + pass);
        System.out.println("partToken: " + partToken);
        System.out.println("Token:" + DigestUtils.md5Hex(url + partToken));
    }
}
