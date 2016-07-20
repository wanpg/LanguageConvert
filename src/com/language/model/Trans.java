package com.language.model;

/**
 * Created by wangjinpeng on 16/5/27.
 */
public class Trans {

    public String cnValue;
    public String enValue;
    public String twValue;

    public static Trans create(String cn, String en, String twValue) {
        if (cn != null) {
            Trans model = new Trans();
            model.cnValue = cn;
            model.enValue = en;
            model.twValue = twValue;
            return model;
        }
        return null;
    }

    @Override
    public String toString() {
        return cnValue + " " + enValue;
    }

    public boolean isValid() {
        return enValue != null && !"".equals(enValue);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Trans)){
            return false;
        }
        if(cnValue == null){
            return cnValue == ((Trans) obj).cnValue;
        }

        return cnValue.equals(((Trans) obj).cnValue);
    }
}
