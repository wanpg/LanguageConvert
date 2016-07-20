package com.language;

import com.language.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wangjinpeng on 16/3/11.
 */
public class DiffString {

    public static void main(String[] arg) {

//        document = Jsoup.parse(is,"utf-8","",new Parser(new XmlTreeBuilder()));
        String androidPath = "/Users/wangjinpeng/Documents/亿方云/国际化/主工程/strings_android_1.xml";
        String iosPath = "/Users/wangjinpeng/Documents/亿方云/国际化/主工程/strings_ios_1.xml";
        try {
            HashMap<String, String> mapAndroid = parseAndroid(androidPath);
            HashMap<String, String> mapIos = parseAndroid(iosPath);
            Utils.print("android string total count:"+ mapAndroid.size());
            Utils.print("ios string total count:"+ mapIos.size());
            ArrayList<String> listSame = new ArrayList<>();
            ArrayList<String> listIos = new ArrayList<>();
            ArrayList<String> listAndroid = new ArrayList<>();

            Iterator<Map.Entry<String, String>> iterator = mapAndroid.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                String value = entry.getValue();
                if(mapIos.containsKey(value)){
                    listSame.add(value);
                    mapIos.remove(value);
                }else{
                    listAndroid.add(value);
                }
            }
            listIos.addAll(mapIos.values());
            Utils.print("android  only string total count:"+ listAndroid.size());
            Utils.print("ios only string total count:"+ listIos.size());
            Utils.print("same string total count:"+ listSame.size());
            String resultPath = "/Users/wangjinpeng/Documents/亿方云/国际化/主工程/diff_1/";
            Utils.writeToLocalXml(resultPath + "android.xml", listAndroid);
            Utils.writeToLocalXml(resultPath + "ios.xml", listIos);
            Utils.writeToLocalXml(resultPath + "same.xml", listSame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static HashMap<String, String> parseAndroid(String path) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        Document document = Jsoup.parse(new File(path), "utf-8");
        Elements elements = document.getElementsByTag("string");

        for(int i = 0;i < elements.size(); i++){
            Element element = elements.get(i);
            String name = element.attr("name");
            String value = element.text().trim();
            map.put(value, value);
        }
        return map;
    }

    /**
     * 解析每行的
     * @return
     * @throws IOException
     */
    static HashMap<String, String> parseIos() throws IOException {
        String str = "/Users/wangjinpeng/Documents/亿方云/国际化/主工程/Localizable.strings";
        HashMap<String, String> map = new HashMap<>();
        FileReader fr=new FileReader(str);//获取文件流
        BufferedReader br = new BufferedReader(fr); //将流整体读取。
        String line;
        while((line = br.readLine())!=null){//判断是否是最后一行
            if(line.contains("=")) {
                String[] keyValue = line.split("=");
                if(keyValue != null && keyValue.length >= 2){
                    map.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        Utils.print(map.size() + "");
        return map;
    }

    static void writeToLocal(String name, ArrayList<String> list) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        try{
            Utils.createFile(name);
            fileWriter = new FileWriter(name, false);
            writer = new BufferedWriter(fileWriter);
            for (String string : list){
                writer.write(string);
                writer.newLine();
            }
            writer.flush();
        }finally {
            if(writer != null){
                writer.close();
            }
            if(fileWriter != null){
                fileWriter.close();
            }
        }
    }
}
