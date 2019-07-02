package com.jackywong.parsing;

import com.jackywong.scala.parsing.my.instances.ParserMethods;

/**
 * Created by huangziqi on 2019/7/2
 */
public class JsonExample {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Object res = ParserMethods.run(JsonParser.root(), com.jackywong.scala.parsing.my.examples.json.JsonExample.json());
        long end = System.currentTimeMillis();
        System.out.println(res + ": \n"+(end-start)+"ms");
    }
}
