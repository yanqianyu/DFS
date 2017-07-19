package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import Frame.*;
import util.*;

public class UDPProtocol extends Thread{
	String mess="";
	public UDPProtocol(String mess){
		this.mess=mess;
	}
	public void run(){
		try{
			updateStorageNode(mess);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void updateStorageNode(String mess) throws Exception{
		List<FileStorage> fs=FileServer.sn;
		String[] Message=mess.split(" ");
		String name=Message[0];
		
		boolean flag;
		
		if(Message[6].equals("true"))
			flag=true;
		else
			flag=false;
		
		FileStorage NewStorage=new FileStorage(Message[0],Message[1],Integer.parseInt(Message[2]),Long.parseLong(Message[3]),Long.parseLong(Message[4]),Message[5], true,Integer.parseInt(Message[7].trim()));
		
		if(SSUtil.Find(NewStorage)==false){
			FileServer.sn.add(NewStorage);
			
			FileServer.st.put(NewStorage.getName(), new Timer());
			FileServer.st.get(NewStorage.getName()).schedule(new MyFileServerTask(NewStorage.getName()),10000);//10s后启动任务
			System.out.println(name+" start working");
			//System.out.println(FileServer.sn.size());
			//System.out.println(NewStorage.toString());
		}else{
			SSUtil.find(Message[0], FileServer.sn).setUsed(true);
			//SSUtil.find(Message[0], FileServer.sn).setRestVolume(Long.parseLong(Message[4]));
			
			//SSUtil.writeStorageNode();
			FileServer.st.get(name).cancel();
			FileServer.st.put(name, new Timer());
			FileServer.st.get(name).schedule(new MyFileServerTask(name), 10000);//10s后启动任务
			System.out.println(name+" is working");
			
		}
		
		//System.out.println(FileServer.sn.size());
		
		
	}
}
