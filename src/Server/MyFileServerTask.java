package Server;
		
import java.io.*;
import java.util.*;

import Frame.FileStorage;
import util.SSUtil;
import Server.FileServer;;

public class MyFileServerTask extends TimerTask{
	
	private String StorageNodeName;
	public MyFileServerTask(String StorageNodeName){
		this.StorageNodeName=StorageNodeName;
	}
	public void run(){
		FileStorage ss;
		try{
			ss=SSUtil.find(StorageNodeName,FileServer.sn);
			ss.setName(StorageNodeName);
			
			FileServer.st.get(StorageNodeName).cancel();
			System.out.println("lose contact with "+StorageNodeName);
			ss.setUsed(false);
			//System.out.println("---->"+ss.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
