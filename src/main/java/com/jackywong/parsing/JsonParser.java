package com.jackywong.parsing;

import com.jackywong.scala.parsing.my.Parser;
import com.jackywong.scala.parsing.my.examples.json.*;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;

import java.util.HashMap;
import java.util.Map;

import static com.jackywong.scala.parsing.my.instances.AbstractParsers.*;

/**
 * Created by huangziqi on 2019/7/2
 */
public class JsonParser {
    public static Parser<? extends JValue> root() {
        return whitespace().skipL(() -> jObj().or(() -> jArr()) );
    }

    public static Parser<JObj> jObj() {
        return surround(string("{"),string("}"), () ->
                token(keyval()).sep(string(",")).map(kvs ->{
                    Map<String,JValue> map = new HashMap<>();
                    kvs.foreach(i -> map.put(i._1(),i._2()));
                    scala.collection.immutable.Map<String,JValue> stringJValueMap = JavaConverters.mapAsScalaMapConverter(map).asScala().toMap(Predef.$conforms());
                    return new JObj(stringJValueMap);
                })
        );
    }

    public static Parser<JAry> jArr() {
        return surround(string("["),string("]"), () ->
                token(value()).sep(string(",")).map(vs -> new JAry((List<JValue>) vs))
        ).scope("array");
    }

    public static Parser<Tuple2<String,JValue>> keyval() {
        return quoted().product(() ->
            token(string(":")).skipL(() -> (Parser<JValue>) value())
        );
    }

    public static Parser<? extends JValue> lit() {
        return string("null").as(JNull$.MODULE$).or( () ->
                pdouble().map(i -> new JNumber((Double) i)).or(() ->
                        escapedQuoted().map(JString::new).or(() ->
                                string("true").as(new JBool(true)).or(() ->
                                        string("false").as(new JBool(true))
                                )
                        )
                )
        ).scope("literal");
    }

    public static Parser<? extends JValue> value() {
        return lit().or(() -> jObj().or(() -> jArr()));
    }
}
