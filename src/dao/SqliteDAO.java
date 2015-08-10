package dao;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import model.*;

public class SqliteDAO {
	
	private static SQLiteConnection db;

	private SqliteDAO(SQLiteConnection db) {
		this.db = db;
	}
	
	public static SqliteDAO connect(String dbname) throws SQLiteException {
		SQLiteConnection db = new SQLiteConnection(new File(dbname));
		SqliteDAO dao = new SqliteDAO(db);
		return dao;
	}
	
	public void close() {
		this.db.dispose();
	}
	
	public void open() throws SQLiteException {
		this.db.open(true);
	}
	
	public void insertArchive(Archive archive) {
		SQLiteStatement st = null;
		try {
			db.exec("BEGIN TRANSACTION;");
	    	db.exec("create table if not exists archives(archiveId varchar(512) PRIMARY KEY, fileName varchar(255), vault varchar(255), size INTEGER, date INTEGER);");
			st = db.prepare("INSERT OR REPLACE INTO archives (archiveId,fileName,vault,size,date) VALUES(?,?,?,?,?)");
			st.bind(1, archive.getArchiveId());
			st.bind(2, archive.getFileName());
			st.bind(3, archive.getVault());
			st.bind(4, archive.getSize());
			st.bind(5, archive.getDate());
			st.step();
			this.db.exec("COMMIT;");
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Archive> retrieveArchives(String vault) {
		List<Archive> archives = new ArrayList<Archive>();
		SQLiteStatement st = null;
	    try {
	    	if (vault != null) {
	    		st = db.prepare("SELECT * FROM archives WHERE vault = ?");
	    		st.bind(1, vault);
	    	} else {
	    		st = db.prepare("SELECT * FROM archives");
	    	}
	    	while (st.step()) {
	    		System.out.println(st.columnString(0));
	    		archives.add(new Archive(st.columnString(0),
	    				st.columnString(1),
	    				st.columnString(2),
	    				st.columnLong(3),
	    				st.columnLong(4)));
	    	}
	    } catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(st != null) {
				st.dispose();
			}
	    }
	    
		return archives;
	}
	
	public static void main(String[] args) {
		SqliteDAO conn = null;
		try {
			String homeDir = System.getProperty("user.home");
			System.out.println(homeDir);
			conn = SqliteDAO.connect(homeDir + "/.db/archives");
			conn.open();
			Archive archive = new Archive("1111111112","filename.txt","example",11l,System.currentTimeMillis());
			conn.insertArchive(archive);
			//conn.close();
			String vault = "example";
			//conn.open();
			List<Archive> archives = conn.retrieveArchives(vault);
			for(Archive arc:archives) {
				System.out.println(arc.toString());
			}
			
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}
		
	}
}
