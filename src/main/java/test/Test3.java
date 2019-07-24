package test;

import org.apache.commons.codec.digest.DigestUtils;

public class Test3 {
    public static void main(String args[]) {
        String data = "/locker/add";
        String enc = DigestUtils.md5Hex("15851841387" + DigestUtils.md5Hex("12345"));
        String result = DigestUtils.md5Hex(data + enc);
        System.out.println(result);
    }
}
