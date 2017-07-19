package Server;
import java.io.*;
import java.net.*;
import java.util.*;
import Frame.FileStorage;
import Storage.StorageNode;
import Frame.FileNode;
import util.SSUtil;

public class FileServer {
	public static HashMap<String,Timer> st=new HashMap<String,Timer>();
	public static List<FileStorage> sn=new ArrayList<FileStorage>();//存储结点信息
	public static List<FileNode> fn=new ArrayList<FileNode>();//记录文件信息
	
	public static void main(String []args) throws Exception{
		SSUtil.initServer();
		new Thread(new Runnable(){
			public void run(){
				try{
					new UDPServer(4321);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();//UDP
		new NwServer(4321,new ThreadPoolSupport(new FileProtocol()));
	}

}
