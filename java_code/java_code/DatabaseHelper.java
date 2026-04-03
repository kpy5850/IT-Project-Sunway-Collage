package com.example.app_design;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Maths.DB";
    public static final int DB_VERSION = 4;

    // 1st Table
    public static final String TABLE_EDUTYPE = "EduType";
    public static final String E_ID = "e_id";
    public static final String GRADE = "grade";
    public static final String CATEGORY = "category";

    // 2nd Table
    public static final String TABLE_CHAPTER = "Chapter";
    public static final String C_ID = "c_id";
    public static final String C_NAME = "c_name";

    // 3rd Table
    public static final String TABLE_TOPIC = "Topic";
    public static final String T_ID = "t_id";
    public static final String T_NAME = "t_name";

    // 4th Table
    public static final String TABLE_QUESTION = "Question";
    public static final String Q_ID = "q_id";
    public static final String Q_IMG = "q_img";
    public static final String ANS_IMG = "ans_img";

    // 5th Table
    public static final String TABLE_RESULT = "Result";
    public static final String R_ID = "r_id";
    public static final String R_DATE = "r_date";
    public static final String R_STATUS = "r_status";
    public static final String AI_ANS = "ai_ans";
    public static final String S_ANS = "s_ans";


    // create table
    private static final String CREATE_TABLE_1 = "CREATE TABLE " + TABLE_EDUTYPE + "("
            + GRADE + " TEXT PRIMARY KEY, "
            + CATEGORY + " TEXT NOT NULL" + ")";

    private static final String CREATE_TABLE_2 = " CREATE TABLE " + TABLE_CHAPTER + "("
            + C_ID + " TEXT PRIMARY KEY, "
            + C_NAME + " TEXT NOT NULL, "
            + GRADE + " TEXT NOT NULL, "
            + "FOREIGN KEY(" + GRADE + ") REFERENCES " + TABLE_EDUTYPE + "(" + GRADE + ")" + ")";

    private static final String CREATE_TABLE_3 = " CREATE TABLE " + TABLE_TOPIC + "("
            + T_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + T_NAME + " TEXT NOT NULL, "
            + C_ID + " TEXT, "
            + "FOREIGN KEY(" + C_ID + ") REFERENCES " + TABLE_CHAPTER + "(" + C_ID + ")" + ")";

    private static final String CREATE_TABLE_4 = " CREATE TABLE " + TABLE_QUESTION + "("
            + Q_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Q_IMG + " BLOB, "
            + ANS_IMG + " BLOB, "
            + T_ID + " INTEGER, "
            + "FOREIGN KEY(" + T_ID + ") REFERENCES " + TABLE_TOPIC + "(" + T_ID + ")" + ")";

    private static final String CREATE_TABLE_5 = " CREATE TABLE " + TABLE_RESULT + "("
            + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + R_DATE + " DATE NOT NULL, "
            + R_STATUS + " BOOLEAN, "
            + AI_ANS + " BLOB, "
            + S_ANS + " BLOB, "
            + Q_ID + " INTEGER, "
            + "FOREIGN KEY(" + Q_ID + ") REFERENCES " + TABLE_QUESTION + "(" + Q_ID + ")" + ")";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_1);
        db.execSQL(CREATE_TABLE_2);
        db.execSQL(CREATE_TABLE_3);
        db.execSQL(CREATE_TABLE_4);
        db.execSQL(CREATE_TABLE_5);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPIC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDUTYPE);
        onCreate(db);
    }
}

