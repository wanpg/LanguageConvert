package com.language;

import com.language.dao.DaoManager;
import com.language.jianfan.JianFan;
import com.language.model.CnKey;
import com.language.model.KeyValuePair;
import com.language.model.Trans;
import com.language.utils.Utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class ReadFromXls {

    public static String EXCEL_TRANS_FOLDER_EN = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/excel";
    public static String EXCEL_TRANS_FOLDER_TW = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/excel";

    public static String EXCEL_EN_INDEXS = "0,1";
    public static String EXCEL_TW_INDEXS = "0,1";

    public static String ANDROID_CN_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/cn/android";
    public static String IOS_CN_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/cn/ios";

    public static String ANDROID_EN_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/en/android";
    public static String ANDROID_CN_TW_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/cn/ios";

    public static String IOS_EN_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/en/ios";
    public static String IOS_CN_TW_FOLDER = "/Users/wangjinpeng/Documents/亿方云/国际化/英文已校对/cn/ios";


    public static boolean ANDROID = true;
    public static boolean IOS = true;

    public static boolean FANTI_DICT_ENABLE = true;

    public static String IOS_FILE_FORMAT = "strings";

    static ArrayList<String> listNoEnAndroid = new ArrayList<>();
    static ArrayList<String> listNoEnIos = new ArrayList<>();

    static final ArrayList<KeyValuePair> fuzzies = new ArrayList<>();
    static final ArrayList<KeyValuePair> replaces = new ArrayList<>();

    static final ArrayList<CnKey> androidCnkeys = new ArrayList<>();
    static final ArrayList<CnKey> iosCnkeys = new ArrayList<>();

    static boolean ANDROID_CHECK_EXIST = false;
    static boolean IOS_CHECK_EXIST = false;

    static int EXCEL_LANGUAGE_EXIST_TAG_INDEX = -1;

    public static void main(String[] arg) {
        Utils.print("正在读取配置");
        readPro();
        Utils.print("读取配置结束");
        readExcel();
    }

    /**
     * 读取配置信息
     */
    private static void readPro(){
        String userDir = System.getProperty("user.dir");
        Utils.print("当前目录:" + userDir);//user.dir指定了当前的路径
        String configPath =  userDir + (userDir.endsWith(File.separator) ? "" : File.separator) + "config/config.properties";
        Properties properties = new Properties();
        try {
            properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(configPath))));
            EXCEL_TRANS_FOLDER_EN = properties.getProperty("EXCEL_TRANS_FOLDER_EN");
            EXCEL_TRANS_FOLDER_TW = properties.getProperty("EXCEL_TRANS_FOLDER_TW");

            ANDROID_CN_FOLDER = properties.getProperty("ANDROID_CN_FOLDER");
            IOS_CN_FOLDER = properties.getProperty("IOS_CN_FOLDER");

            ANDROID_EN_FOLDER = properties.getProperty("ANDROID_EN_FOLDER");
            IOS_EN_FOLDER = properties.getProperty("IOS_EN_FOLDER");

            ANDROID_CN_TW_FOLDER = properties.getProperty("ANDROID_CN_TW_FOLDER");
            IOS_CN_TW_FOLDER = properties.getProperty("IOS_CN_TW_FOLDER");

            ANDROID = Boolean.valueOf(properties.getProperty("ANDROID"));
            IOS = Boolean.valueOf(properties.getProperty("IOS"));

            IOS_FILE_FORMAT = properties.getProperty("IOS_FILE_FORMAT");

            FANTI_DICT_ENABLE = Boolean.valueOf(properties.getProperty("FANTI_DICT_ENABLE"));

            EXCEL_EN_INDEXS = properties.getProperty("EXCEL_EN_INDEXS");
            EXCEL_TW_INDEXS = properties.getProperty("EXCEL_TW_INDEXS");

            ANDROID_CHECK_EXIST = Boolean.valueOf(properties.getProperty("ANDROID_CHECK_EXIST"));
            IOS_CHECK_EXIST = Boolean.valueOf(properties.getProperty("IOS_CHECK_EXIST"));
            EXCEL_LANGUAGE_EXIST_TAG_INDEX = Integer.valueOf(properties.getProperty("EXCEL_LANGUAGE_EXIST_TAG_INDEX"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        String fuzzyPath =  userDir + (userDir.endsWith(File.separator) ? "" : File.separator) + "config/fuzzy_match_words.txt";
        readKeyValues(fuzzyPath, fuzzies);
        CnKey.initFuzzies(fuzzies);

        String replacePath = userDir + (userDir.endsWith(File.separator) ? "" : File.separator) + "config/dynamic_replace_words.txt";
        readKeyValues(replacePath, replaces);
    }

    private static void readExcel(){
        Utils.print("正在读取英文excel");
        //读取英文资源
        ArrayList<Trans> listEn = new ArrayList<>();
        //先读取英文
        File folder = new File(EXCEL_TRANS_FOLDER_EN);
        for (File f : folder.listFiles()) {
            if (!f.isHidden() && f.isFile() && Utils.isExcel(f.getAbsolutePath())) {
                listEn.addAll(readExcel(f.getAbsolutePath(), "en", EXCEL_EN_INDEXS));
            }
        }
        Utils.print("正在读取繁体中文excel");
        //读取繁体资源
        ArrayList<Trans> listTw = new ArrayList<>();
        //先读取英文
        File folderTw = new File(EXCEL_TRANS_FOLDER_TW);
        for (File f : folderTw.listFiles()) {
            if (!f.isHidden() && f.isFile() && Utils.isExcel(f.getAbsolutePath())) {
                listTw.addAll(readExcel(f.getAbsolutePath(), "tw", EXCEL_TW_INDEXS));
            }
        }

        Utils.print("正在读取整合繁体英文资源");
        //整合繁体资源和英文资源的信息
        while (listTw.size() > 0){
            Trans tw = listTw.remove(0);
            int index = listEn.indexOf(tw);
            Trans en = null;
            if(index >= 0 && index < listEn.size()){
                en = listEn.get(index);
                en.twValue = tw.twValue;
            }
            if(en != null){

            }else{
                listEn.add(tw);
            }
        }


        //将数据保存入map, 并过滤重复key的
        final LinkedHashMap<CnKey, Trans> map = new LinkedHashMap<>();
        for(Trans trans : listEn){
            CnKey cnKey = new CnKey(trans.cnValue.trim());
            Trans old = map.get(cnKey);
            if(old == null){
                map.put(cnKey, trans);
            }else{
                if(Utils.isStringEmpty(old.enValue)){
                    old.enValue = trans.enValue;
                }
                if(Utils.isStringEmpty(old.twValue)){
                    old.twValue = trans.twValue;
                }
            }
        }

        //将map组装为list并存入数据库
        ArrayList<Trans> transArrayList = new ArrayList<>();
        Iterator<Map.Entry<CnKey, Trans>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<CnKey, Trans> entry = iterator.next();
            if(entry.getValue() != null) {
                transArrayList.add(entry.getValue());
            }
        }

        DaoManager.getInstance().getLangeageDao().writeToSqlite(transArrayList);

        Utils.print("翻译数据整理完毕");
        iosCnkeys.clear();
        androidCnkeys.clear();
        formatAll(map, ANDROID_EN_FOLDER, IOS_EN_FOLDER, "en");
        formatAll(map, ANDROID_CN_TW_FOLDER, IOS_CN_TW_FOLDER, "tw");

        if(ANDROID_CHECK_EXIST && androidCnkeys.size() > 0 && EXCEL_LANGUAGE_EXIST_TAG_INDEX >= 0){
            Utils.print("检测android的存在的字符串");
            checkExist("ANDROID", androidCnkeys, EXCEL_TRANS_FOLDER_EN, EXCEL_EN_INDEXS);
        }

        if(IOS_CHECK_EXIST && iosCnkeys.size() > 0 && EXCEL_LANGUAGE_EXIST_TAG_INDEX >= 0){
            Utils.print("检测ios的存在的字符串");
            checkExist("iOS", iosCnkeys, EXCEL_TRANS_FOLDER_EN, EXCEL_EN_INDEXS);
        }

        Utils.print("运行结束");
    }

    private static void checkExist(String value, ArrayList<CnKey> existList, String excelFolder, String indexs){
        //先读取英文
        File folder = new File(EXCEL_TRANS_FOLDER_EN);
        for (File f : folder.listFiles()) {
            if (!f.isHidden() && f.isFile() && Utils.isExcel(f.getAbsolutePath())) {
                Workbook workbook = null;
                String[] indexArr = indexs.split(",");
                try {
                    workbook = Utils.getWorkBook(f.getAbsolutePath());
                    if(workbook != null) {
                        Sheet sheet = workbook.getSheetAt(0);
                        int begin = sheet.getFirstRowNum();
                        int end = sheet.getLastRowNum();
                        for (int i = begin; i <= end; i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                Cell cnCell = row.getCell(Integer.valueOf(indexArr[0]));
                                try {
                                    if (cnCell != null) {
                                        String cnValue = cnCell.getStringCellValue();
                                        CnKey cnKey = new CnKey(cnValue.trim());
                                        if (existList.contains(cnKey)) {
                                            Cell cellTag = row.getCell(EXCEL_LANGUAGE_EXIST_TAG_INDEX);
                                            String tag = cellTag.getStringCellValue();
                                            if (Utils.isStringEmpty(tag)) {
                                                cellTag.setCellValue(value);
                                            } else if (tag.contains(value)) {
                                                //do nothing
                                            } else {
                                                cellTag.setCellValue(tag + "\\" + value);
                                            }
                                        } else {
                                            Cell cellTag = row.getCell(EXCEL_LANGUAGE_EXIST_TAG_INDEX);
                                            String tag = cellTag.getStringCellValue();
                                            if (Utils.isStringEmpty(tag)) {
                                                //do nothing
                                            } else if (tag.contains(value)) {
                                                if (tag.contains("\\" + value + "\\")) {
                                                    tag = tag.replace("\\" + value, "");
                                                } else if (tag.startsWith(value + "\\")) {
                                                    tag = tag.replace(value + "\\", "");
                                                } else if (tag.endsWith("\\" + value)) {
                                                    tag = tag.replace("\\" + value, "");
                                                } else {
                                                    tag = tag.replace(value, "");
                                                }
                                                cellTag.setCellValue(tag);
                                            }
                                        }
                                    }
                                }catch (IllegalStateException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    workbook.write(new FileOutputStream(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void formatAll(HashMap<CnKey, Trans> srcMap, String androidOutFolder, String iosOutFolder, String language){
        try {
            listNoEnAndroid.clear();
            listNoEnIos.clear();

            if(ANDROID) {
                Utils.print("正在转换android的string-"+language);
                Utils.deleteFile(androidOutFolder);
                File fileAndroid = new File(ANDROID_CN_FOLDER);
                File[] files = fileAndroid.listFiles();
                if(files != null && files.length > 0) {
                    for (File f : files) {
                        if (!f.isHidden()) {
                            String path = f.getAbsolutePath();
                            formatAndroid(srcMap, path, androidOutFolder, language);
//                            formatAndroidCnTw(path, ANDROID_CN_TW_FOLDER);
                        }
                    }
                    Utils.writeToLocalXml(androidOutFolder + (androidOutFolder.endsWith(File.separator) ? "" : File.separator) + "android_未翻译.xml", listNoEnAndroid);
                    Utils.print("Android转换结束");
                }else{
                    Utils.print("没有找到Android的源文件");
                }
            }

            if(IOS) {
                Utils.print("正在转换Ios的string-"+language);
                Utils.deleteFile(iosOutFolder);
                File iosFolder = new File(IOS_CN_FOLDER);
                if(iosFolder != null && iosFolder.exists() && iosFolder.isDirectory()) {
                    String absFolder = iosFolder.getAbsolutePath();
                    ArrayList<File> listIos = Utils.listAllFile(absFolder, IOS_FILE_FORMAT);
                    if (listIos != null && listIos.size() > 0) {
                        for (File file : listIos) {
                            if (!file.isHidden()) {
                                String path = file.getAbsolutePath();
                                if ("strings".equalsIgnoreCase(IOS_FILE_FORMAT)) {
                                    formatIosStrings(srcMap, path, iosOutFolder, absFolder, language);
                                } else if ("xliff".equalsIgnoreCase(IOS_FILE_FORMAT)) {
                                    formatIosXml(srcMap, path, iosOutFolder, language);
                                }
//                            formatIosCnTw(path, IOS_CN_TW_FOLDER);
                            }
                        }
                        Utils.writeToLocalXml(iosOutFolder + (iosOutFolder.endsWith(File.separator) ? "" : File.separator) + "ios_未翻译.xml", listNoEnIos);
                        Utils.print("Ios转换结束");
                    } else {
                        Utils.print("没有找到Ios的源文件");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private static void readKeyValues(String path, ArrayList<KeyValuePair> list){
        list.clear();
        try {
            BufferedReader replaceReader = new BufferedReader(new FileReader(path));
            String input = "";
            while ((input = replaceReader.readLine()) != null){
                if(!"".equals(input)){
                    String[] arr = input.substring(0, input.lastIndexOf(";")).split("=");
                    KeyValuePair keyValuePair = new KeyValuePair();
                    keyValuePair.key = arr[0];
                    keyValuePair.value = arr[1];
                    list.add(keyValuePair);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void formatAndroidCnTw(String path, String outFolder) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        String fileName = file.getName();
        String outPath = outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + fileName;
        Utils.createFile(outPath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        NodeList nodeList = document.getElementsByTagName("string");
        for (int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String value = node.getFirstChild().getNodeValue();
            String cnTwValue = JianFan.j2f(value);
            node.getFirstChild().setNodeValue(cnTwValue);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tfer = tf.newTransformer();
        DOMSource dsource = new DOMSource(document);
        StreamResult sr = new StreamResult(outPath);
        tfer.transform(dsource, sr);

    }

    private static void formatIosCnTw(String path, String outFolder) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        String fileName = file.getName();
        String outPath = outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + fileName;
        Utils.createFile(outPath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList nodeList = document.getElementsByTagName("file");
//        ((Element)((Element)nodeList.item(0)).getElementsByTagName("trans-unit").item(0)).getElementsByTagName("source").item(0).getFirstChild().getNodeValue()
        for (int i = 0; i < nodeList.getLength(); i++){
            Node fileNode = nodeList.item(i);
            NodeList transList = ((Element) fileNode).getElementsByTagName("trans-unit");
            for(int j = 0; j < transList.getLength(); j++){
                Element trans = (Element) transList.item(j);
                NodeList nodeSrcList = trans.getElementsByTagName("source");
                NodeList nodeTarList = trans.getElementsByTagName("target");
                if(nodeSrcList != null && nodeTarList != null && nodeSrcList.getLength() > 0 && nodeTarList.getLength() > 0) {
                    if(nodeSrcList.item(0) != null && nodeTarList.item(0)!=null && nodeSrcList.item(0).getFirstChild() != null && nodeTarList.item(0).getFirstChild() !=null) {
                        String src = nodeSrcList.item(0).getFirstChild().getNodeValue();
                        Node nodeTar = nodeTarList.item(0);
                        String cnTw = JianFan.j2f(src);
                        nodeTar.getFirstChild().setNodeValue(cnTw);
                    }
                }
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tfer = tf.newTransformer();
        DOMSource dsource = new DOMSource(document);
        StreamResult sr = new StreamResult(outPath);
        tfer.transform(dsource, sr);
    }

    private static String checkAndReplace(String srcCnValue, String tarEnvalue){
        if(Utils.isStringEmpty(tarEnvalue)){
            return null;
        }
        String[] cnMatchs = Utils.checkMatch(srcCnValue);
        String[] enMatchs = Utils.checkMatch(tarEnvalue);
        String enValue = tarEnvalue;
        if(cnMatchs.length == enMatchs.length){
            for(int j = 0; j < cnMatchs.length; j++){
                String enMatch = enMatchs[j];
                String cnMatch = cnMatchs[j];
                enValue = enValue.replace(enMatch, cnMatch);
            }
            return enValue;
        }else{
            return null;
        }
    }

    /**
     * 读取excel,按照第一列是中文,第二列是英文的格式读取
     * @param path
     * @return
     */
    static ArrayList<Trans> readExcel(String path, String tarLanguage, String indexs) {
        Workbook workbook = null;
        ArrayList<Trans> list = new ArrayList<>();
        String[] indexArr = indexs.split(",");
        try {
            workbook = Utils.getWorkBook(path);
            if(workbook != null) {
                Sheet sheet = workbook.getSheetAt(0);
                int begin = sheet.getFirstRowNum();
                int end = sheet.getLastRowNum();
                for (int i = begin; i <= end; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        try {
                            Cell cnCell = row.getCell(Integer.valueOf(indexArr[0]));
                            Cell enCell = row.getCell(Integer.valueOf(indexArr[1]));
                            if (cnCell != null && enCell != null) {
                                String cnValue = cnCell.getStringCellValue();
                                String enValue = replaceTargetDynamic(enCell.getStringCellValue());

                                Trans model = null;
                                if (!Utils.isStringEmpty(cnValue)) {
                                    if ("en".equals(tarLanguage)) {
                                        model = Trans.create(Utils.trim(cut(cnValue)), Utils.trim(cut(enValue)), null);
                                    } else if ("tw".equals(tarLanguage)) {
                                        model = Trans.create(Utils.trim(cut(cnValue)), null, Utils.trim(cut(enValue)));
                                    }
                                }

                                if (model != null) {
                                    list.add(model);
                                }
                            }
                        }catch (IllegalStateException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    static String replaceTargetDynamic(String tar){
        for(KeyValuePair keyValuePair : replaces){
            tar = tar.replace(keyValuePair.key, keyValuePair.value);
        }
        return tar;
    }

    /**
     * 裁剪,如果包含<string></string> 那么只截取他们之间的内容
     * @param orgin
     * @return
     */
    static String cut(String orgin) {

        if(orgin.contains("<string>") && orgin.contains("</string>")) {

            int startIndex = orgin.indexOf(">") + 1;
            int endIndex = orgin.lastIndexOf("<");
            try {
                return orgin.substring(startIndex, endIndex).trim();
            } catch (StringIndexOutOfBoundsException e) {

            }
            return null;
        }else{
            return orgin.trim();
        }
    }

    private static void formatAndroid(HashMap<CnKey, Trans> dataMap, String path, String outFolder, String language) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        File file = new File(path);
        if(!file.exists()){
            return;
        }

        String fileName = file.getName();
        String outPath = outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + fileName;
        Utils.createFile(outPath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        NodeList nodeList = document.getElementsByTagName("string");
        for (int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String value = node.getFirstChild().getNodeValue();
            CnKey cnKey = new CnKey(value.trim());
            Trans model = dataMap.get(cnKey);//  getMatchTrans(dataMap, Utils.replaceMatch(value.trim()));
            if(ANDROID_CHECK_EXIST) {
                androidCnkeys.add(cnKey);
            }
            if(model != null && model.isValid()){
                String enValue = checkAndReplace(value, "tw".equals(language) ? model.twValue : model.enValue);
                if(enValue == null || "".equalsIgnoreCase(enValue)){
                    listNoEnAndroid.add(value);
                }else{
                    if(enValue.contains("'")){
                        //在android的res中," ' "是不允许单独存在的,必须要求与" \ "搭配使用,否则无法打包
                        enValue = enValue.replaceAll("\'", "\\\\'");
                    }
                    node.getFirstChild().setNodeValue(enValue);
                }
            }else{
                listNoEnAndroid.add(value);
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tfer = tf.newTransformer();
        DOMSource dsource = new DOMSource(document);
        StreamResult sr = new StreamResult(outPath);
        tfer.transform(dsource, sr);
    }

    private static void formatIosXml(HashMap<CnKey, Trans> dataMap, String path, String outFolder, String language) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        String fileName = file.getName();
        String outPath = outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + fileName;
        Utils.createFile(outPath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList nodeList = document.getElementsByTagName("file");
//        ((Element)((Element)nodeList.item(0)).getElementsByTagName("trans-unit").item(0)).getElementsByTagName("source").item(0).getFirstChild().getNodeValue()
        for (int i = 0; i < nodeList.getLength(); i++){
            Node fileNode = nodeList.item(i);
            NodeList transList = ((Element) fileNode).getElementsByTagName("trans-unit");
            for(int j = 0; j < transList.getLength(); j++){
                Element trans = (Element) transList.item(j);
                NodeList nodeSrcList = trans.getElementsByTagName("source");
                NodeList nodeTarList = trans.getElementsByTagName("target");
                if(nodeSrcList != null && nodeTarList != null && nodeSrcList.getLength() > 0 && nodeTarList.getLength() > 0) {
                    if(nodeSrcList.item(0) != null && nodeTarList.item(0)!=null && nodeSrcList.item(0).getFirstChild() != null && nodeTarList.item(0).getFirstChild() !=null) {
                        String src = nodeSrcList.item(0).getFirstChild().getNodeValue();
                        Node nodeTar = nodeTarList.item(0);
                        CnKey cnKey = new CnKey(src.trim());
                        Trans model = dataMap.get(cnKey);//getMatchTrans(dataMap, Utils.replaceMatch(src.trim()));
                        if(IOS_CHECK_EXIST) {
                            iosCnkeys.add(cnKey);
                        }
                        if (model != null && model.isValid() && nodeTar != null) {
                            String enValue = checkAndReplace(src, "tw".equals(language) ? model.twValue : model.enValue);
                            if(enValue == null || "".equalsIgnoreCase(enValue)){
                                listNoEnAndroid.add(src);
                            }else{
                                nodeTar.getFirstChild().setNodeValue(enValue);
                            }
                        } else {
                            listNoEnIos.add(src);
                        }
                    }
                }
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tfer = tf.newTransformer();
        DOMSource dsource = new DOMSource(document);
        StreamResult sr = new StreamResult(outPath);
        tfer.transform(dsource, sr);
    }

    private static void formatIosStrings(HashMap<CnKey, Trans> dataMap, String path, String outFolder, String absInFolder, String language) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File file = new File(path);
        if(!file.exists()){
            return;
        }
        String endding = file.getAbsolutePath().replace(absInFolder, "");

        String outPath = outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + endding;
        Utils.createFile(outPath);

        FileReader srcFileReader;
        BufferedReader srcFileBufferReader;

        FileWriter destfileWriter = null;
        BufferedWriter destWriter = null;


        srcFileReader = new FileReader(file);
        srcFileBufferReader = new BufferedReader(srcFileReader);


        destfileWriter = new FileWriter(outPath, false);
        destWriter = new BufferedWriter(destfileWriter);

        String input = "";
        while ((input = srcFileBufferReader.readLine()) != null){
            if(input.startsWith("\"") && input.endsWith("\";") && input.contains("=")){
                String[] arr = input.split("=");
                String outPut = "";
                if(arr.length == 2) {
                    String cnValue = arr[1];
                    cnValue = Utils.trim(cnValue.substring(cnValue.indexOf("\"") + 1, cnValue.lastIndexOf("\"")));
                    CnKey cnKey = new CnKey(cnValue.trim());
                    Trans model = dataMap.get(cnKey);//getMatchTrans(dataMap, Utils.replaceMatch(cnValue.trim()));
                    if(IOS_CHECK_EXIST) {
                        iosCnkeys.add(cnKey);
                    }
                    if (model != null && model.isValid()) {
                        String enValue = checkAndReplace(cnValue.trim(), "tw".equals(language) ? model.twValue : model.enValue);
                        if(enValue == null || "".equalsIgnoreCase(enValue)){
                            listNoEnIos.add(cnValue);
                            outPut = input;
                        }else{
//                            outPut = input.replace(cnValue, enValue);
                            outPut = arr[0] + "=" + arr[1].replace(cnValue, enValue);
                        }
                    } else {
                        listNoEnIos.add(cnValue);
                        outPut = input;
                    }
                }
                destWriter.write(outPut);
            }else{
                destWriter.write(input);
            }
            destWriter.newLine();
        }
        destWriter.flush();
        destWriter.close();
        destfileWriter.close();
        srcFileBufferReader.close();
        srcFileReader.close();
    }
}
