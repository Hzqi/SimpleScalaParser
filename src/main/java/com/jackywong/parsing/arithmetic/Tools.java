package com.jackywong.parsing.arithmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by huangziqi on 2020/2/18
 */
public class Tools {
    private static List<Character> digits = Arrays.asList('0','1','2','3','4','5','6','7','8','9');
    public static boolean isDigit(Character c) {
        return digits.contains(c);
    }

    public static boolean isSpace(Character c) {
        return c.equals(" ");
    }

    public static Integer cvt(Character c) {
        return c - '0';
    }

    public static Integer shiftl(Integer m, Integer n) {
        return 10 * m + n;
    }
}
