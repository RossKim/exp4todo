package controllers;

import play.*;
import play.mvc.*;

import models.*;

import javax.persistence.*;
import java.util.*;

/**
 * 課題Aアンケートアプリのplay!による実装．
 */
public class SampleQuestionnaire extends Controller {

    public static void index() {
        // SampleQuestionnaire/index.html に記述されたビューを描画
        render();
    }
    
    public static void genderForm() {
        // SampleQuestionnaire/genderForm.html に記述されたテンプレートを描画
        render();
    }
    
    public static void postGender() {
        // 入力された性別の情報をセッションに書き込む．
        // フォームのデータは params オブジェクトの中に格納されている．
        session.put("gender", params.get("gender"));

        // アクションメソッドを呼び出すことにより，該当URLにリダイレクトできる．
        // ここでは samplequestionnaire/nameform にリダイレクトする．
        nameForm();
    }
    
    public static void nameForm() {
        // SampleQuestionnaire/nameForm.html に記述されたテンプレートを描画
        render();
        // 上記は render("SampleQuestionnaire/nameForm.html") と同義．
    }
    
    public static void postName(String name) {
        // アクションメソッドの引数としてパラメータと同名の変数を記述することに
        // より，パラメータを受け取ることもできる．
        session.put("name", name);
        // 上記は session.put("name", params.get("name")) と同義．

        commentForm();
    }
    
    public static void commentForm() {
        render();
    }
    
    public static void postComment(String comment) {
        session.put("comment", comment);
        confirm();
    }

    public static void confirm() {
        render();
    }
    
    public static void submit() {
        int gender;
        if ("男性".equals(session.get("gender"))) {
            gender = SampleEntry.MALE;
        }
        else {
            gender = SampleEntry.FEMALE;
        }
        String name = session.get("name");
        String comment = session.get("comment");

        System.out.println("性別：" + gender);
        System.out.println("名前：" + name);
        System.out.println("感想：" + comment);

        // アンケートデータをデータベースに書き込む
        SampleEntry entry = new SampleEntry(gender, name, comment);
        entry.save();
        
        // 埋め込むべき変数をテンプレートに渡す．
        // テンプレートからはキー名で参照できる．
        // テンプレートからは，${entry.id} として該当データで自動生成された
        // ID番号を取得している． 
        renderArgs.put("entry", entry);
        render();
    }
    
    public static void list() {
        // 全エントリを取得
        List<SampleEntry> entries = SampleEntry.all().fetch();

        // 男性のみ取得
        // find メソッドにクエリ文（プレースホルダ付き）と引数を指定
        List<SampleEntry> males =
            SampleEntry.find("gender = ?", SampleEntry.MALE).fetch();
        // 女性のみ取得
        List<SampleEntry> females =
            SampleEntry.find("gender = ?", SampleEntry.FEMALE).fetch();

        // ローカル変数に値を書き込み，render の引数に並べることにより
        // 該当の変数をテンプレート側に渡すこともできる
        // （ローカル変数名で参照する）．
        render(entries, males, females);
    }
}
