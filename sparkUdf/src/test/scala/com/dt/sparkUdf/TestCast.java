package com.dt.sparkUdf;

/**
 * @Auther: zhaoxin
 * @Date: 2019/2/15 10:12
 * @Description:
 */
public class TestCast {
    public static void main(String[] args) {
        int a = (int)(999999999999999999L % 1000000000);
        int b = 1111111111;

        System.out.println(a);
    }
}
