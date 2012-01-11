package com.funlib.db;

import java.util.List;

/**
 * 创建DB Table时，回调获取将要创建的Table
 * 
 * @author 陶建立
 * 
 */
public interface DBTableCreateCB {

	public List<Class<?>> getAllTablesClass();
}
