package com.language.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class Utils {

    public static void print(String str) {
        System.out.println(str);
    }

    public static void print(Object str) {
        System.out.println(str.toString());
    }

    public static void createFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            file.createNewFile();
        }
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void writeToLocalXml(String path, ArrayList<String> list) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        try {
            Utils.createFile(path);
            fileWriter = new FileWriter(path, false);
            writer = new BufferedWriter(fileWriter);
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writer.newLine();
            writer.write("<resources>");
            writer.newLine();
            for (String string : list) {
                writer.write("<string>" + string + "</string>");
                writer.newLine();
            }
            writer.write("</resources>");
            writer.newLine();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public static Workbook getWorkBook(String path) throws IOException {
        String suffix = getSuffix(path);
        Workbook workbook = null;
        File file = new File(path);
        if(!file.isHidden() && file.exists() && !file.getName().startsWith("~$")) {
            if ("xls".equalsIgnoreCase(suffix)) {
                workbook = new HSSFWorkbook(new FileInputStream(file));
            } else if ("xlsx".equalsIgnoreCase(suffix)) {
                workbook = new XSSFWorkbook(new FileInputStream(file));
            }
        }
        return workbook;
    }

    public static String getSuffix(String path){
        int index = path.lastIndexOf(".");
        return path.substring(index + 1);
    }

    public static boolean isExcel(String path){
        String suffix = getSuffix(path);
        return "xls".equalsIgnoreCase(suffix) || "xlsx".equalsIgnoreCase(suffix);
    }

    public static String replaceMatch(String src){
        String result = src;
        String[] strs = checkMatch(src);
        if(strs.length > 0){
            for(String match : strs){
                result = result.replaceAll("%[0-9]{0,1}[\\$]{0,1}[l,z]{0,1}[@,s,d,u]{1}", "%@");
            }
        }
        return trim(result);
    }

    public static String[] checkMatch(String src) {
        String matcher = "%[0-9]{0,1}[\\$]{0,1}[l,z]{0,1}[@,s,d,u]{1}";
        Pattern p = Pattern.compile(matcher);
        Matcher m = p.matcher(src);
        ArrayList<String> strs = new ArrayList<String>();
        while (m.find()) {
            strs.add(m.group(0));
        }
        return strs.toArray(new String[strs.size()]);
    }

    public static String trim(String src){
        src = src.trim();
        int start = 0;
        int end = src.length();
        if(src.startsWith("\\n")){
            start = src.indexOf("\\n") + "\\n".length();
        }
        if(src.endsWith("\\n")){
            end = src.lastIndexOf("\\n");
        }
        src = src.substring(start, end).trim();
        end = src.length();
        if(src.endsWith("\\t")){
            end = src.lastIndexOf("\\t");
        }
        return src.substring(0, end).trim();
    }

    public static ArrayList<File> listAllFile(String folderPath, String suffix){
        File folder = new File(folderPath);
        ArrayList<File> list = new ArrayList<>();
        if(folder.exists() && folder.isDirectory()){
            File[] files = folder.listFiles();
            for(File file : files){
                if(file.exists()){
                    String path = file.getAbsolutePath();
                    if(file.isFile() && suffix.equalsIgnoreCase(getSuffix(path))){
                        list.add(file);
                    }else if(file.isDirectory()){
                        ArrayList<File> fileListTmp = listAllFile(path, suffix);
                        if(fileListTmp.size() > 0){
                            list.addAll(fileListTmp);
                        }
                    }
                }
            }
        }
        return list;
    }

    private static final String[] WEEKS_EN = new String[8];

    private static final String[] MONTHS_EN = new String[12];

    private static final int FLAG_SHOW_YEAR = 0;
    private static final int FLAG_SHOW_YEAR_MONTH = 1;
    private static final int FLAG_SHOW_YEAR_MONTH_DATE = 2;
    private static final int FLAG_SHOW_MONTH = 3;
    private static final int FLAG_SHOW_MONTH_DATE = 4;
    private static final int FLAG_SHOW_DATE = 5;

    static {
        WEEKS_EN[Calendar.MONDAY] = "MONDAY";
        WEEKS_EN[Calendar.TUESDAY] = "TUESDAY";
        WEEKS_EN[Calendar.WEDNESDAY] = "WEDNESDAY";
        WEEKS_EN[Calendar.THURSDAY] = "THURSDAY";
        WEEKS_EN[Calendar.FRIDAY] = "FRIDAY";
        WEEKS_EN[Calendar.SATURDAY] = "SATURDAY";
        WEEKS_EN[Calendar.SUNDAY] = "SUNDAY";

        MONTHS_EN[Calendar.JANUARY] = "JANUARY";
        MONTHS_EN[Calendar.FEBRUARY] = "FEBRUARY";
        MONTHS_EN[Calendar.MARCH] = "MARCH";
        MONTHS_EN[Calendar.APRIL] = "APRIL";
        MONTHS_EN[Calendar.MAY] = "MAY";
        MONTHS_EN[Calendar.JUNE] = "JUNE";
        MONTHS_EN[Calendar.JULY] = "JULY";
        MONTHS_EN[Calendar.AUGUST] = "AUGUST";
        MONTHS_EN[Calendar.SEPTEMBER] = "SEPTEMBER";
        MONTHS_EN[Calendar.OCTOBER] = "OCTOBER";
        MONTHS_EN[Calendar.NOVEMBER] = "NOVEMBER";
        MONTHS_EN[Calendar.DECEMBER] = "DECEMBER";
    }

    public static String formatDateTime(long millis, int flag) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (flag){
            case FLAG_SHOW_YEAR_MONTH_DATE:
                return String.format("%d %s %d", dayOfMonth, MONTHS_EN[month], year);

            case FLAG_SHOW_YEAR_MONTH:
                return String.format("%s %d", MONTHS_EN[month], year);

            case FLAG_SHOW_YEAR:
                return String.format("%d", year);

            case FLAG_SHOW_MONTH_DATE:
                return String.format("%d %s", dayOfMonth, MONTHS_EN[month]);

            case FLAG_SHOW_MONTH:
                return String.format("%s", MONTHS_EN[month]);

            case FLAG_SHOW_DATE:
                return String.format("%d", dayOfMonth);
        }
        return "";
    }

    public static boolean isStringEmpty(String src){
        return src == null || "".equals(src);
    }

    public static void deleteFile(String path){
        deleteFile(new File(path));
    }

    public static void deleteFile(File file){
        if(file != null){
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File file1 : files){
                    deleteFile(file1);
                }
            }else{
                file.delete();
            }
        }
    }
}
