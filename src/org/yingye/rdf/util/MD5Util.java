package org.yingye.rdf.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author 懵懂无知的蜗牛 https://blog.csdn.net/qq_25646191/article/details/78863110
 * @apiNote 文件Hash值计算
 */
public class MD5Util {

    /**
     * 保证文件的MD5值为32位
     *
     * @param filePath 文件路径
     * @return 32位长度的hash值
     */
    public static String md5HashCode32(String filePath) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        return md5HashCode32(fis);
    }

    /**
     * java计算文件32位md5值
     *
     * @param fis 输入流
     * @return 32位长度的md5值
     */
    public static String md5HashCode32(InputStream fis) {
        try {
            // 拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[4096];
            int length;
            while ((length = fis.read(buffer, 0, 4096)) != -1) {
                md.update(buffer, 0, length);
            }

            // 清空
            fis.close();
            // 转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes = md.digest();
            StringBuilder hexValue = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;// 解释参见最下方
                if (val < 16) {
					/*
					  如果小于16，那么val值的16进制形式必然为一位， 因为十进制0,1...9,10,11,12,13,14,15 对应的 16进制为
					  0,1...9,a,b,c,d,e,f; 此处高位补0。
					 */
                    hexValue.append("0");
                }
                // 这里借助了Integer类的方法实现16进制的转换
                hexValue.append(Integer.toHexString(val));
            }

            // 清空
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}