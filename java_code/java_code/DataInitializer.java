package com.example.app_design;

import android.content.Context;
import android.database.Cursor;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;


public class DataInitializer {
    private DatabaseManager dbManager;
    private Context context;

    public DataInitializer(Context context){
        this.context = context;
        this.dbManager = new DatabaseManager(context);
    }

    public void importChapters() {
        dbManager.open();
        InputStream is = context.getResources().openRawResource(R.raw.chapter);
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String cId = null, cName = null, grade = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("c_id".equals(tagName)) cId = parser.nextText();
                    else if ("c_name".equals(tagName)) cName = parser.nextText();
                    else if ("grade".equals(tagName)) grade = parser.nextText();
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("chapter".equals(tagName) && cId != null){
                        dbManager.insertChapter(cId, cName, grade);
                        cId = null;
                        cName = null;
                        grade = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e){ e.printStackTrace(); }
        finally { dbManager.close(); }
    }

    public void importEduType(){
        dbManager.open();
        InputStream is = context.getResources().openRawResource(R.raw.edu_type);
        XmlPullParser parser = Xml.newPullParser();
        try{
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String grade = null, category = null;
            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagName = parser.getName();
                if(eventType == XmlPullParser.START_TAG){
                    if("grade".equals(tagName)) grade = parser.nextText();
                    else if ("category".equals(tagName)) category = parser.nextText();
                } else if (eventType == XmlPullParser.END_TAG && "edu_type".equals(tagName)) {
                    dbManager.insertEduType(grade, category);
                }
                eventType = parser.next();
            }
        }catch (Exception e){ e.printStackTrace();}
        finally { dbManager.close();}
    }

    public void importTopics(){
        dbManager.open();
        InputStream is = context.getResources().openRawResource(R.raw.topic);
        XmlPullParser parser = Xml.newPullParser();
        try{
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String tName = null, cId = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("t_name".equals(tagName)) tName = parser.nextText();
                    else if ("c_id".equals(tagName)) cId = parser.nextText();
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("topic".equals(tagName) && tName != null && cId != null) {
                        dbManager.insertTopic(tName, cId);
                        tName = null;
                        cId = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e){ e.printStackTrace(); }
        finally { dbManager.close(); }
    }

    public void importSampleQuestions() {
        dbManager.open();
        InputStream is = context.getResources().openRawResource(R.raw.questions);
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String tName = null, qImageName = null, aImageName = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("t_name".equals(tagName)) tName = parser.nextText();
                    else if ("q_image".equals(tagName)) qImageName = parser.nextText();
                    else if ("a_image".equals(tagName)) aImageName = parser.nextText();
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("question".equals(tagName)) {
                        int topicId = dbManager.getTopicByName(tName);
                        if (topicId != -1 && qImageName != null && aImageName != null) {
                            int qResId = context.getResources().getIdentifier(qImageName, "drawable", context.getPackageName());
                            int aResId = context.getResources().getIdentifier(aImageName, "drawable", context.getPackageName());

                            if (qResId != 0 && aResId != 0) {
                                byte[] qBytes = imageToBytes(qResId);
                                byte[] aBytes = imageToBytes(aResId);
                                dbManager.insertQuestion(qBytes, aBytes, topicId);
                            }
                        }
                        tName = null;
                        qImageName = null;
                        aImageName = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbManager.close();
        }
    }

    private byte[] imageToBytes(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}


