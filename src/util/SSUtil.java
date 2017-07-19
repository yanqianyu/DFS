package util;

import java.io.*;
import java.net.*;
import java.util.*;
import Frame.FileStorage;
import Frame.FileNode;
import Server.FileServer;
import Server.MyFileServerTask;

public class SSUtil {
	
	public static synchronized void initServer()throws Exception{
		File file=new File("/Users/apple/Desktop/Obj/server.obj");
		if(file.length()==0){
			FileServer.fn=new ArrayList<FileNode>();
			return ;
		}
		try{
			FileInputStream fis=new FileInputStream(file);
			ObjectInputStream ois=new ObjectInputStream(fis);
			FileServer.fn=(List<FileNode>)ois.readObject();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static synchronized void writeFileNode()throws Exception{
		File file=new File("/Users/apple/Desktop/Obj/server.obj");
		if(file.exists()==false){
			file.createNewFile();
		}
		try{
			FileWriter fileWriter=new FileWriter(file);
			fileWriter.write("");
			fileWriter.flush();
			fileWriter.close();
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(FileServer.fn);
			oos.flush();
			oos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	//结点配置
	public static synchronized FileStorage getStorageServerByFile(String fileName) throws Exception{
		Properties p=new Properties();
		FileStorage fs=new FileStorage();
		p.load(new FileInputStream(fileName));
		fs.setName(p.getProperty("NodeName"));
		fs.setPort(Integer.parseInt(p.getProperty("port")));
		fs.setRootFolder(p.getProperty("RootFolder"));
		fs.setIP(p.getProperty("IP"));
		
		File f=new File(fs.getRootFolder());
		f.mkdirs();//创建文件夹
		
		String tempV=p.getProperty("Volume");
		String v="";
		long volume=0;
		
		for(int i=0;i<tempV.length();i++){
			if(Character.isDigit(tempV.charAt(i))){
				v+=tempV.charAt(i);
				continue;
			}
			volume=Long.parseLong(v);
			switch(tempV.charAt(i)){
			case 'B':
				break;
			case 'K':
				volume*=1024;
				break;
			case 'M':
				volume*=1024*1024;
				break;
			case 'G':
				volume*=1024*1024*1024;
				break;
				default:
					break;
			}
			break;
		}
		fs.setVolume(volume);
		fs.setRestVolume(fs.getVolume());
		fs.setUsed(true);
		
		return fs;
	}
	
	
	//按名查找结点
	public static synchronized FileStorage find(String name,List<FileStorage> fs) throws Exception{
		for(int i=0;i<fs.size();i++){
			if(name.equals(fs.get(i).getName()))
				return fs.get(i);
		}
		return null;
	}
	
	//查找最适合的结点 0 最大 1 次大
	public static synchronized ArrayList<FileStorage> getMaxFileStorage(List<FileStorage> fs,long size){
		//负载均衡
		List<FileStorage> maxFs=new ArrayList<FileStorage>();
		FileStorage f1=new FileStorage();
		FileStorage f2=new FileStorage();
		
		double sec=0;
		double temp=fs.get(0).getRestVolume()/fs.get(0).getVolume();
		f1=fs.get(0);
		for(int i=0;i<fs.size();i++){
			if(fs.get(i).getRestVolume()>=size&&fs.get(i).getUsed()==true){
				if(temp<=fs.get(i).getRestVolume()/fs.get(i).getVolume()){
					sec=temp;
					temp=fs.get(i).getRestVolume()/fs.get(i).getVolume();
					f2=f1;
					f1=fs.get(i);
				}
			}
		}
		maxFs.add(f1);
		maxFs.add(f2);
		return (ArrayList<FileStorage>) maxFs;
	}
	
	//依照时间生成独立的文件标示
	public static synchronized String getUUID(){
		String s=UUID.randomUUID().toString();
		return s;
	}
	
	//依照ip与port来查找结点路径
	public static synchronized String findByIpAndPort(String IP,int port){
		//List<FileStorage> afs=FileServer.sn;
		FileStorage temp=new FileStorage();
		for(int i=0;i<FileServer.sn.size();i++){
			temp=FileServer.sn.get(i);
			if(temp.getIP().equals(IP)&&temp.getPort()==port){
				return temp.getRootFolder();
			}
		}
		return null;
	}
	
	public static synchronized boolean Find(FileStorage temp){
		for(FileStorage t:FileServer.sn){
			if(temp.getName().equals(t.getName())){
				return true;
			}
		}
		return false;
	}
}
