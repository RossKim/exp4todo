package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class SampleEntry extends Model {
    /**
     * 男性であることを表す値．
     */
    public static int MALE = 1;
    
    /**
     * 女性であることを表す値．
     */
    public static int FEMALE = 2;

    /**
     * 性別
     */
    public int gender;

    /**
     * 名前
     */
    public String name;

    /**
     * 感想コメント
     */
    public String comment;
    
    /**
     * コンストラクタ．
     * @param gender 性別
     * @param name 名前
     * @param comment 感想コメント
     */
    public SampleEntry(int gender, String name, String comment) {
        this.gender = gender;
        this.name = name;
        this.comment = comment;
    }
}
