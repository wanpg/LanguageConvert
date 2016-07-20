/** 
 * File    : Converter.java 
 * Created : 2014年1月16日 
 * By      : luhuiguo 
 */
package com.language.jianfan;

import com.language.ReadFromXls;
import com.language.tire.GetWord;
import com.language.tire.domain.Forest;
import com.language.tire.domain.Value;
import com.language.tire.library.Library;
import com.language.utils.StringUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author luhuiguo
 */
public enum Converter {
    SIMPLIFIED(false), TRADITIONAL(true);

    public static final char CJK_UNIFIED_IDEOGRAPHS_START = '\u4E00';
    public static final char CJK_UNIFIED_IDEOGRAPHS_END = '\u9FA5';
    public static final String SIMPLIFIED_MAPPING_FILE = "/simp.txt";
    public static final String SIMPLIFIED_LEXEMIC_MAPPING_FILE = "/simplified.txt";
    public static final String TRADITIONAL_MAPPING_FILE = "/trad.txt";
    public static final String TRADITIONAL_LEXEMIC_MAPPING_FILE = "/traditional.txt";

    public static final String EMPTY = "";
    public static final String SHARP = "#";
    public static final String EQUAL = "=";

    private char[] chars = null;

    private Forest dict = null;

    private int maxLen = 2;

    Converter(boolean s2t) {
        loadCharMapping(s2t);
        loadLexemicMapping(s2t);
    }

    public InputStream getResourceStream(String mappingFile) throws FileNotFoundException {
        String userDir = System.getProperty("user.dir");
        String path =  userDir + (userDir.endsWith(File.separator) ? "" : File.separator) + "config/resources" + File.separator + mappingFile;
        return new FileInputStream(path);
    }

    public void loadCharMapping(boolean s2t) {

        String mappingFile = SIMPLIFIED_MAPPING_FILE;

        if (s2t) {
            mappingFile = TRADITIONAL_MAPPING_FILE;
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResourceStream(mappingFile)), StandardCharsets.UTF_8));

            CharArrayWriter out = new CharArrayWriter();
            String line = null;
            while (null != (line = in.readLine())) {
                // line = line.trim();
                out.write(line);
            }
            chars = out.toCharArray();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLexemicMapping(boolean s2t) {

        String mappingFile = SIMPLIFIED_LEXEMIC_MAPPING_FILE;

        if (s2t) {
            mappingFile = TRADITIONAL_LEXEMIC_MAPPING_FILE;
        }

        dict = new Forest();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResourceStream(mappingFile)), StandardCharsets.UTF_8));

            String line = null;
            while (null != (line = in.readLine())) {
                // line = line.trim();
                if (line.length() == 0 || line.startsWith(SHARP)) {
                    continue;
                }
                String[] pair = line.split(EQUAL);

                if (pair.length < 2) {
                    continue;
                }
                maxLen = maxLen < pair[0].length() ? pair[0].length() : maxLen;

                Library.insertWord(dict, new Value(pair[0], pair[1]));
            }

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char convert(char ch) {
        if (ch >= CJK_UNIFIED_IDEOGRAPHS_START && ch <= CJK_UNIFIED_IDEOGRAPHS_END) {
            return chars[ch - CJK_UNIFIED_IDEOGRAPHS_START];
        } else {
            return ch;
        }
    }

    private void strConvert(String str, StringBuilder sb) {
        if (StringUtil.isBlank(str)) {
            return;
        }
        for (int i = 0; i < str.length(); i++) {
            sb.append(convert(str.charAt(i)));
        }
    }


    public String convert(String str) {
        if (StringUtil.isBlank(str)) {
            return str;
        }


        StringBuilder sb = new StringBuilder(str.length());

        int beginOffe = 0;
        if(ReadFromXls.FANTI_DICT_ENABLE) {
            GetWord word = dict.getWord(str);

            String temp = null;
            while ((temp = word.getFrontWords()) != null) {
                strConvert(str.substring(beginOffe, word.offe), sb);
                sb.append(word.getParam(0));
                beginOffe = word.offe + temp.length();
            }
        }

        if (beginOffe < str.length()) {
            strConvert(str.substring(beginOffe, str.length()), sb);
        }
        return sb.toString();
    }

}
