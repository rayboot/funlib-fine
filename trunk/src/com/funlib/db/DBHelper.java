package com.funlib.db;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * 基于ormlite操作数据库
 * 
 * 用法：
 * 		DBHelper.DB_VERSION = 1;	//必须在getDbHelper之前调用
        DBHelper dbHelper = DBHelper.getDbHelper(this);
        dbHelper.setDBTableCreateCB(this);
        
        Dao<SimpleData, Integer> simpleDao = dbHelper.getDao(SimpleData.class);//获取操作对象
        List<SimpleData> list = simpleDao.queryForAll();//查询所有
        simpleDao.delete(simple);//删除某个对象 
        simpleDao.create(simple);//增加对象
        simpleDao.update(simple)//更新对象
        
        @Override
		public List<Class<?>> getAllTablesClass() {
		// TODO Auto-generated method stub
		
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(SimpleData.class);
		
		return list;
		}
 * 
 * @author taojianli
 * 
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

	private final static String DB_NAME = "orm_db.db";
	private DBTableCreateCB mDbTableCreateCB;
	private List<Class<?>> mDBTableClasses;

	public static int DB_VERSION = 1;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		// TODO Auto-generated method stub

		mDBTableClasses = mDbTableCreateCB.getAllTablesClass();

		try {

			int size = mDBTableClasses.size();
			for (int i = 0; i < size; ++i) {

				TableUtils
						.createTable(connectionSource, mDBTableClasses.get(i));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

		try {
			
			mDBTableClasses = mDbTableCreateCB.getAllTablesClass();
			int size = mDBTableClasses.size();
			for (int i = 0; i < size; ++i) {

				TableUtils.dropTable(connectionSource, mDBTableClasses.get(i),
						true);
			}

			onCreate(db, connectionSource);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置DB Table回调
	 * 
	 * @param cb
	 */
	public void setDBTableCreateCB(DBTableCreateCB cb) {

		mDbTableCreateCB = cb;
	}
	
	/**
	 * 获取数据库single instance
	 * 
	 * @param context
	 * @return
	 */
	private static DBHelper sDbHelper;

	public static DBHelper getDbHelper(Context context) {

		if (sDbHelper == null)
			sDbHelper = OpenHelperManager.getHelper(context, DBHelper.class);

		return sDbHelper;
	}

	/**
	 * 关闭数据库
	 */
	public static void closeDB() {

		if (sDbHelper != null) {

			OpenHelperManager.releaseHelper();
			sDbHelper = null;
		}
	}
}
