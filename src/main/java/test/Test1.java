package test;

import org.apache.commons.codec.digest.DigestUtils;

public class Test1 {
    public static void main(String args[]) {
        /*Jedis redis  = new Jedis("10.0.10.100",6379);
        redis.auth("feiyang");
        System.out.println("".equals(redis.get("code")));
        redis.close();*/
        String hexString = "0C61CF373F02123322e6ba13";
        int length = hexString.length() / 2;
        hexString = hexString.toUpperCase();
        System.out.println(hexString);
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        System.out.println(d);
        System.out.println(DigestUtils.md5Hex(d));
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
