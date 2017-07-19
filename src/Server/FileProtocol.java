package Server;

import java.io.*;
import java.net.*;
import java.util.*;

import Frame.*;
import Server.*;
import util.SSUtil;
import com.google.gson.Gson;
import java.net.Socket;

public class FileProtocol implements IOStrategy{
	
	//收到客户的上传命令
	public boolean get(Socket socket,DataOutputStream dos,DataInputStream dis)
	throws Exception{
		long size=dis.readLong();//客户通知server文件大小
		List<FileStorage> lfs=new ArrayList<FileStorage>();
		lfs=SSUtil.getMaxFileStorage(FileServer.sn,size);
		
		int num=0;
		if(lfs.get(0).getRestVolume()>=size){
			num++;
		}
		if(lfs.get(1).getRestVolume()>=size){
			num++;
		}
		if(num<2){
			dos.writeBoolean(false);
			dos.flush();
			System.out.println("No storage can be used!");
			return false;
		}
		
		dos.writeBoolean(true);
		dos.flush();//通知server有可用结点
		
		String fileName=dis.readUTF();//获取文件名称
		
		dos.writeUTF(lfs.get(0).getIP());//通知client 主存储点的ip port
		dos.writeInt(lfs.get(0).getPort());
		
		dos.writeUTF(lfs.get(1).getIP());//通知client copy存储点的ip port
		dos.writeInt(lfs.get(1).getPort());
		
		String uuid=SSUtil.getUUID();
		dos.writeUTF(uuid);//通知client生成的文件名
		
		List<FileNode> fn=FileServer.fn;
		FileNode filenode=new FileNode(uuid,fileName,size,lfs.get(0).getIP(),lfs.get(0).getPort(),
				lfs.get(1).getIP(),lfs.get(1).getPort());
		fn.add(filenode);//server添加文件相关记录
		SSUtil.writeFileNode();//序列化
		
		dos.flush();
		
		for(FileStorage tempFS:FileServer.sn){
			if(tempFS.getIP().equals(lfs.get(0).getIP())&&
					tempFS.getPort()==(lfs.get(0).getPort())){
				tempFS.setRestVolume(tempFS.getRestVolume()-size);
			}
			if(tempFS.getIP().equals(lfs.get(1).getIP())&&
					tempFS.getPort()==(lfs.get(1).getPort())){
				tempFS.setRestVolume(tempFS.getRestVolume()-size);
			}
		}
		//SSUtil.writeStorageNode();
		return true;
	}
	
	//收到客户的下载命令  
	public boolean send(Socket socket,DataOutputStream dos,DataInputStream dis)
	throws Exception{
		String fileUUID=dis.readUTF();//客户通知server文件的uuid
		
		//server 查找文件 返回结果通知客户
		boolean result=false;
		FileNode temp=new FileNode();
		for(FileNode tempFN:FileServer.fn){
			if(tempFN.getUuid().equals(fileUUID)){
				temp=tempFN;
				result=true;
				break;
			}
		}
		if(result==false){
			System.out.println("this file does not exist");
			dos.writeBoolean(false);
			dos.flush();
			return false;
		}
		System.out.println("this file exists");
		dos.writeBoolean(true);
		dos.flush();
		List<FileStorage> lfs=new ArrayList<FileStorage>();
		
		//找主存储点
		for(FileStorage tempFS:FileServer.sn){
			if(tempFS.getIP().equals(temp.getfirstServerIp())&&tempFS.getPort()==temp.getfirstServerPort()){
				lfs.add(tempFS);
			}
		}
		
		//找copy点
		for(FileStorage tempFS:FileServer.sn){
			if(tempFS.getIP().equals(temp.getsecServerIP())&&tempFS.getPort()==temp.getsecServerPort()){
				lfs.add(tempFS);
			}
		}
		
		if(lfs.size()==2){
			dos.writeInt(2);//通知客户两个点可用
			
			dos.writeUTF(temp.getName());//通知客户文件名
			dos.writeLong(temp.getSize());//通知客户文件大小
			
			dos.writeUTF(lfs.get(0).getIP());//通知客户结点的信息
			dos.writeInt(lfs.get(0).getPort());
			
			dos.writeUTF(lfs.get(1).getIP());
			dos.writeInt(lfs.get(1).getPort());
		}
		else if(lfs.size()==1){
			dos.writeInt(1);
			dos.writeUTF(temp.getName());
			dos.writeLong(temp.getSize());
			dos.writeUTF(lfs.get(0).getIP());
			dos.writeInt(lfs.get(0).getPort());
			dos.flush();
		}
		return true;
	}
	
	//收到客户的删除文件信息
	public boolean remove(Socket socket,DataOutputStream dos,DataInputStream dis)
	throws Exception{
		try{
			String uuid=dis.readUTF();//接受客户发来的uuid
			FileNode temp=new FileNode();
			boolean find=false;
			for(FileNode tempFN:FileServer.fn){
				if(tempFN.getUuid().equals(uuid)){
					find=true;
					temp=tempFN;
					break;
				}
			}
			if(find==false){//没找到文件
				dos.writeBoolean(false);
				return false;
			}
			
			long size=temp.getSize();
			dos.writeBoolean(true);///通知客户找到文件了可以删除
			
			List<FileStorage> lfs=new ArrayList<FileStorage>();
			
			for(FileStorage tempFS:FileServer.sn){
				if(tempFS.getIP().equals(temp.getfirstServerIp())&&tempFS.getPort()==temp.getfirstServerPort()){
					lfs.add(tempFS);
				}
			}
			
			for(FileStorage tempFS:FileServer.sn){
				if(tempFS.getIP().equals(temp.getsecServerIP())&&tempFS.getPort()==temp.getsecServerPort()){
					lfs.add(tempFS);
				}
			}
			
			dos.writeUTF(lfs.get(0).getIP());
			dos.writeInt(lfs.get(0).getPort());
			
			dos.writeUTF(lfs.get(1).getIP());
			dos.writeInt(lfs.get(1).getPort());
			
			dos.flush();
			
			FileServer.fn.remove(temp);//server删除文件相关记录
			SSUtil.writeFileNode();//序列化
			
			for(FileStorage tempFS:FileServer.sn){
				if(tempFS.getIP().equals(lfs.get(0).getIP())&&
						tempFS.getPort()==(lfs.get(0).getPort())){
					tempFS.setRestVolume(tempFS.getRestVolume()+size);
				}
				if(tempFS.getIP().equals(lfs.get(1).getIP())&&
						tempFS.getPort()==(lfs.get(1).getPort())){
					tempFS.setRestVolume(tempFS.getRestVolume()+size);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void getFile(Socket socket, DataInputStream dis, DataOutputStream dos) {
		// TODO Auto-generated method stub
		List<FileNode> fileNodes=FileServer.fn;
		try {
			 dos.writeUTF(new Gson().toJson(fileNodes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 向界面返回文件存储节点信息
	 */
	private boolean getFileStorage(Socket socket, DataInputStream dis, DataOutputStream dos) {
		// TODO Auto-generated method stub
		List<FileStorage> fileStorages=FileServer.sn;
		try {
			dos.writeUTF(new Gson().toJson(fileStorages));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public void service(Socket socket)  {
		// TODO Auto-generated method stub
		
		try{
			DataInputStream dis=new DataInputStream(socket.getInputStream());
			DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
			int command=0;
			//while(true){
				command=dis.readInt();
				switch(command){
				case 1:
					get(socket,dos,dis);
					break;
				case 2:
					send(socket,dos,dis);
					break;
				case 3:
					remove(socket,dos,dis);
					break;

				case 4:
					getFile(socket, dis, dos);
					break;
				case 5:
					getFileStorage(socket, dis, dos);
					break;
				}
			//}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
