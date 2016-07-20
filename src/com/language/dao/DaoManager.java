package com.language.dao;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class DaoManager {

    private static DaoManager instance = new DaoManager();

    public static DaoManager getInstance() {
        return instance;
    }

    private LanguageDao langeageDao = new LanguageDao();

    public LanguageDao getLangeageDao() {
        return langeageDao;
    }
}
