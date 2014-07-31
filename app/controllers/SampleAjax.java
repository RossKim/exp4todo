package controllers;

import java.util.HashMap;
import java.util.Map;

import play.data.validation.Required;
import play.mvc.Controller;

public class SampleAjax extends Controller {
    
    public static void index() {
        render();
    }
    
    public static void convert(@Required String p) {
        // 標準出力に入力されたパラメータの名前を出力
        System.out.println(params.all().keySet());

        // pの最後の文字を最初に移動させた文字列を作成
        String last = p.substring(p.length() - 1, p.length());
        String rest = p.substring(0, p.length() - 1);
        String result =  last + rest;
        
        // Map に結果を蓄え，JSON として出力
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "OK");
        map.put("result", result);
        renderJSON(map);
    }
}
