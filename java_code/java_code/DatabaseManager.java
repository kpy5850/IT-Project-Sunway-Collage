package com.example.app_design;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {

    private DatabaseHelper dbHelper;
    private final Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context c) {
        context = c;
    }

    public DatabaseManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertEduType(String grade, String category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.GRADE, grade);
        contentValues.put(DatabaseHelper.CATEGORY, category);
        database.insert(DatabaseHelper.TABLE_EDUTYPE, null, contentValues);
    }

    public void insertChapter(String c_id, String c_name, String grade) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.C_ID, c_id);
        contentValues.put(DatabaseHelper.C_NAME, c_name);
        contentValues.put(DatabaseHelper.GRADE, grade);
        database.insert(DatabaseHelper.TABLE_CHAPTER, null, contentValues);
    }

    public void insertTopic(String t_name, String c_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.T_NAME, t_name);
        contentValues.put(DatabaseHelper.C_ID, c_id);
        database.insert(DatabaseHelper.TABLE_TOPIC, null, contentValues);
    }

    public void insertQuestion(byte[] q_img, byte[] ans_img, int t_id) {
        ContentValues contentValues = new ContentValues();

        byte[] compressedQ = compressImage(q_img);
        byte[] compressedAns = compressImage(ans_img);

        contentValues.put(DatabaseHelper.Q_IMG, compressedQ);
        contentValues.put(DatabaseHelper.ANS_IMG, compressedAns);
        contentValues.put(DatabaseHelper.T_ID, t_id);
        database.insert(DatabaseHelper.TABLE_QUESTION, null, contentValues);
    }

    public void insertResult(String r_date, boolean r_status, byte[] ai_ans, byte[] s_ans, int q_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.R_DATE, r_date);
        contentValues.put(DatabaseHelper.R_STATUS, r_status);
        contentValues.put(DatabaseHelper.AI_ANS, ai_ans);
        contentValues.put(DatabaseHelper.S_ANS, s_ans);
        contentValues.put(DatabaseHelper.Q_ID, q_id);
        database.insert(DatabaseHelper.TABLE_RESULT, null, contentValues);
    }

    public int getChapterIdByName(String chapterName) {
        int id = -1;
        Cursor cursor = database.query(DatabaseHelper.TABLE_CHAPTER,
                new String[]{DatabaseHelper.C_ID},
                DatabaseHelper.C_NAME + " LIKE ?",
                new String[]{chapterName + "%"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.C_ID));
            cursor.close();
        }
        return id;
    }

    public Cursor getQuestionsByChapter(String cId) {
        String sql = "SELECT q.* FROM " + DatabaseHelper.TABLE_QUESTION + " q " +
                "JOIN " + DatabaseHelper.TABLE_TOPIC + " t ON q." + DatabaseHelper.T_ID + " = t." + DatabaseHelper.T_ID +
                " WHERE t." + DatabaseHelper.C_ID + " = ?";
        return database.rawQuery(sql, new String[]{cId});
    }

    public byte[] getQuestionImage(String topicName, int questionIndex) {
        byte[] image = null;
        String query = "SELECT q." + DatabaseHelper.Q_IMG +
                " FROM " + DatabaseHelper.TABLE_QUESTION + " q" +
                " JOIN " + DatabaseHelper.TABLE_TOPIC + " t ON q." + DatabaseHelper.T_ID + " = t." + DatabaseHelper.T_ID +
                " WHERE t." + DatabaseHelper.T_NAME + " = ? " +
                " ORDER BY q." + DatabaseHelper.Q_ID + " ASC LIMIT 1 OFFSET ?";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{topicName, String.valueOf(questionIndex)});
            if (cursor != null && cursor.moveToFirst()) {
                image = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.Q_IMG));
            }
        } catch (Exception e) {
            android.util.Log.e("DB_ERROR", "Image too large at index " + questionIndex);
        } finally {
            if (cursor != null) cursor.close();
        }
        return image;
    }
    public byte[] getQuestionImageById(int qId){
        byte[] image = null;
        String query = "SELECT " + DatabaseHelper.Q_IMG + " FROM " + DatabaseHelper.TABLE_QUESTION +
                " WHERE " + DatabaseHelper.Q_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(qId)});
        if(cursor != null && cursor.moveToFirst()){
            image = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.Q_IMG));
            cursor.close();
        }
        return image;
    }

    public int getQuestionCount(String topicName){
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_QUESTION + " q " +
                " JOIN " + DatabaseHelper.TABLE_TOPIC + " t ON q." + DatabaseHelper.T_ID + " = t." + DatabaseHelper.T_ID +
                " WHERE t." + DatabaseHelper.T_NAME + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{topicName});
        if(cursor != null && cursor.moveToFirst()){
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getQuestionId(String topicName, int questionIndex){
        int qId = -1;
        String query = "SELECT q." + DatabaseHelper.Q_ID +
                " FROM " + DatabaseHelper.TABLE_QUESTION + " q" +
                " JOIN " + DatabaseHelper.TABLE_TOPIC + " t ON q." + DatabaseHelper.T_ID + " = t." + DatabaseHelper.T_ID +
                " WHERE t." + DatabaseHelper.T_NAME + " = ? " +
                " ORDER BY q." + DatabaseHelper.Q_ID + " ASC LIMIT 1 OFFSET ?";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{topicName, String.valueOf(questionIndex)});
            if(cursor != null && cursor.moveToFirst()){
                qId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.Q_ID));
            }
        } catch (Exception e) {
            android.util.Log.e("DB_ERROR", "getQuestionId failed at index " + questionIndex + ": " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return qId;
    }

    public byte[] getAnswerImageById(int qId){
        byte[] image = null;
        String query = "SELECT " + DatabaseHelper.ANS_IMG + " FROM " + DatabaseHelper.TABLE_QUESTION +
                " WHERE " + DatabaseHelper.Q_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(qId)});
        if (cursor != null && cursor.moveToFirst()) {
            image = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.ANS_IMG));
            cursor.close();
        }
        return image;

    }

    public int getTopicByName(String tName){
        int id = -1;
        Cursor cursor = database.query(DatabaseHelper.TABLE_TOPIC,
                new String[]{DatabaseHelper.T_ID},
                DatabaseHelper.T_NAME + " = ?",
                new String[]{tName}, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.T_ID));
            cursor.close();
        }
        return id;
    }

    public List<String> getAllTopicNames(){
        List<String> list = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_TOPIC,
                new String[]{DatabaseHelper.T_NAME}, null, null, null, null, DatabaseHelper.T_ID + " ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                list.add(cursor.getString(0));
            }
            cursor.close();
        }
        return list;
    }

    private byte[] compressImage(byte[] originalData) {
        if (originalData == null) return null;

        if (originalData.length < 1024 * 1024) return originalData;

        Bitmap bitmap = BitmapFactory.decodeByteArray(originalData, 0, originalData.length);
        if (bitmap == null) return originalData;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        byte[] compressedData = stream.toByteArray();
        android.util.Log.d("DB_COMPRESS", "Original: " + originalData.length/1024 + "KB -> Compressed: " + compressedData.length/1024 + "KB");

        return compressedData;
    }

    public void updateStatus(int qId, int newStatus) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_RESULT,
                null,
                DatabaseHelper.Q_ID + " = ?",
                new String[]{String.valueOf(qId)}, null, null, null);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.R_STATUS, newStatus);

        String currentDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        values.put(DatabaseHelper.R_DATE, currentDate);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int currentStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.R_STATUS));
            if (newStatus > currentStatus) {
                database.update(DatabaseHelper.TABLE_RESULT, values,
                        DatabaseHelper.Q_ID + " = ?", new String[]{String.valueOf(qId)});
            }
            cursor.close();
        } else {
            values.put(DatabaseHelper.Q_ID, qId);
            database.insert(DatabaseHelper.TABLE_RESULT, null, values);
        }
    }

    public Cursor getAllQuestionsWithStatus() {
        String sql = "SELECT q." + DatabaseHelper.Q_ID + ", " +
                "t." + DatabaseHelper.T_NAME + " as topic_name, " +
                "r." + DatabaseHelper.R_STATUS + ", " +
                "r." + DatabaseHelper.R_DATE + ", " +
                "(SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_QUESTION + " q2 " +
                " WHERE q2." + DatabaseHelper.T_ID + " = q." + DatabaseHelper.T_ID +
                " AND q2." + DatabaseHelper.Q_ID + " <= q." + DatabaseHelper.Q_ID + ") as q_number " +
                " FROM " + DatabaseHelper.TABLE_QUESTION + " q " +
                " JOIN " + DatabaseHelper.TABLE_TOPIC + " t ON q." + DatabaseHelper.T_ID + " = t." + DatabaseHelper.T_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_RESULT + " r ON q." + DatabaseHelper.Q_ID + " = r." + DatabaseHelper.Q_ID +
                " ORDER BY q." + DatabaseHelper.Q_ID + " ASC";

        return database.rawQuery(sql, null);
    }

    public void clearAllHistory(){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.R_STATUS, -1);
        values.put(DatabaseHelper.R_DATE, "No record");

        database.delete(DatabaseHelper.TABLE_RESULT, null, null);
    }

    public void saveOrUpdateHistory(String r_date, boolean r_status, byte[] ai_ans, byte[] s_ans, int q_id){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.R_DATE, r_date);
        values.put(DatabaseHelper.R_STATUS, r_status);
        values.put(DatabaseHelper.AI_ANS, ai_ans);
        values.put(DatabaseHelper.S_ANS, s_ans);

        Cursor cursor = database.query(DatabaseHelper.TABLE_RESULT,
                new String[]{DatabaseHelper.Q_ID},
                DatabaseHelper.Q_ID + " = ?",
                new String[]{String.valueOf(q_id)},
                null, null, null);

        if(cursor != null && cursor.getCount() > 0){
            database.update(DatabaseHelper.TABLE_RESULT, values,
                    DatabaseHelper.Q_ID + " = ?",
                    new String[]{String.valueOf(q_id)});
            cursor.close();
        }else{
            values.put(DatabaseHelper.Q_ID, q_id);
            database.insert(DatabaseHelper.TABLE_RESULT, null, values);
            if (cursor != null)
                cursor.close();
        }
    }

    public Cursor getHistoryByQId(int qId){
        return database.query(DatabaseHelper.TABLE_RESULT,
                null,
                DatabaseHelper.Q_ID + " = ?",
                new String[]{String.valueOf(qId)},
                null, null, null);
    }

}
