package com.language.model;

import com.language.utils.Utils;

import java.util.ArrayList;

/**
 * Created by wangjinpeng on 16/5/30.
 */
public class CnKey {
    private static ArrayList<KeyValuePair> FUZZIES = new ArrayList<>();
    public static void initFuzzies(ArrayList<KeyValuePair> fuzzies){
        FUZZIES.clear();
        if(fuzzies != null){
            FUZZIES.addAll(fuzzies);
        }
    }

    private String key;

    public CnKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return 1;//key == null ? super.hashCode() : key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(!(obj instanceof CnKey)){
            return false;
        }
        if(key == null){
            return ((CnKey) obj).key == null;
        }
        if(((CnKey) obj).key == null){
            return false;
        }
        if("群组名：%s".equals(key)){
            int i = 0;
            i++;
        }
        String curKey = Utils.replaceMatch(key);
        String tarKey = Utils.replaceMatch(((CnKey) obj).key);
        if(curKey.equals(tarKey)){
            return true;
        }
        for(KeyValuePair keyValuePair : FUZZIES){
            if(curKey.equals(tarKey.replace(keyValuePair.key, keyValuePair.value))){
                return true;
            }
        }
        return false;
//        return Utils.replaceMatch(key).equals(Utils.replaceMatch(((CnKey) obj).key));
    }

    /*private static Trans getMatchTrans(String tarCn){
        Trans trans = dataMap.get(tarCn);
        if(trans != null){
            return trans;
        }
        for(KeyValuePair fuzzy : FUZZIES){
            trans = dataMap.get(tarCn.replace(fuzzy.key, fuzzy.value));
            if(trans != null){
                return trans;
            }
        }
        return trans;
    }*/
}
