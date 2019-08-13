package test;

import org.apache.commons.codec.digest.DigestUtils;

public class Test3 {
    public static void main(String args[]) {
        String url = "/locker/get";
        String pass = DigestUtils.md5Hex("15851841387" + DigestUtils.md5Hex("12345678"));
        String apiKey = "d35c4ac83609df51410d0995b3987843";
        String partToken = DigestUtils.md5Hex(pass + apiKey);
        System.out.println("Pass: " + pass);
        System.out.println("partToken: " + partToken);
        System.out.println("Token:" + DigestUtils.md5Hex(url + partToken));
    }
}
