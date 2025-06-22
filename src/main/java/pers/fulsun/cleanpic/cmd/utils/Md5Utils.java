package pers.fulsun.cleanpic.cmd.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

    public static String calculateMD5(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] md5Bytes = md5.digest(fileBytes);
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : md5Bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }


}
