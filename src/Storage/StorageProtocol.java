package Storage;

import java.io.*;
import java.net.*;
import java.util.*;
import Server.*;
import util.SSUtil;

public class StorageProtocol implements IOStrategy{
	
	public boolean get(Socket socket,DataOutputStream dos,DataInputStream dis) throws Exception{
		
		String fileName=dis.readUTF();
		
		String secIp=dis.readUTF();//接受用户传达的备份结点的信息
		int secPort=dis.readInt();
		long size=dis.readLong();
		String path=StorageNode.fs.getRootFolder()+File.separator+fileName;
		File file=new File(path);
		
		FileOutputStream fos=new FileOutputStream(file);
		BufferedOutputStream bos=new BufferedOutputStream(fos);
		
		long alPass=0;
		int onceRead=0;
		byte[] buffer=new byte[1000];
		while(alPass<size){
			if(size-alPass>=buffer.length){
				onceRead=dis.read(buffer,0,buffer.length);
			}
			else{
				onceRead=dis.read(buffer,0,(int)(size-alPass));
			}
			alPass+=onceRead;
			bos.write(buffer,0,onceRead);
			bos.flush();
		}
		bos.close();
		fos.close();
		
		if(alPass==size){
			System.out.println("主存储结点接受文件成功");
			Storage.StorageNode.fs.setFileNum(Storage.StorageNode.fs.getFileNum()+1);
			long tempSize=Storage.StorageNode.fs.getRestVolume()-size;
			Storage.StorageNode.fs.setRestVolume(tempSize);
			
			//主结点与备份建立链接 传输
		
			Socket s2=new Socket(secIp,secPort);
			DataInputStream dis1=new DataInputStream(s2.getInputStream());
			DataOutputStream dos1=new DataOutputStream(s2.getOutputStream());
			
			dos1.writeInt(4);//通知备份结点
			dos1.writeLong(size);
			dos1.writeUTF(fileName);
			
			long alRead=0;
			int oncePass=0;
			buffer=new byte[4096];
			
			FileInputStream fis=new FileInputStream(file);
			BufferedInputStream bis=new BufferedInputStream(fis);
			
			try{
				while((oncePass=bis.read(buffer))!=-1){
					alRead+=oncePass;
					dos1.write(buffer,0,oncePass);
					dos1.flush();
				}
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("主结点备份文件失败");
				return false;
			}
			
			bis.close();
			fis.close();
			dos1.close();
			dis1.close();
			s2.close();
			
			if(alRead==size){
				System.out.println("文件备份发送成功");
				return true;
			}
			else{
				System.out.println("文件备份发送失败");
				return false;
			}
		}
		return true;
	}
	
    public boolean send(Socket socket,DataOutputStream dos,DataInputStream dis) throws Exception{
		String fileName=dis.readUTF();
		
		String path=Storage.StorageNode.fs.getRootFolder()+File.separator+fileName;
		
		File file=new File(path);
		FileInputStream fis=new FileInputStream(file);
		
		byte[] buffer=new byte[1000];
		long alPass=0;
		int onceRead=0;
		try{
			/*while((onceRead=fis.read(buffer))!=-1){
				alPass+=onceRead;
				dos.write(buffer, 0, onceRead);
			}*/
			while(alPass<file.length()){
				if(file.length()-alPass>=buffer.length){
					onceRead=fis.read(buffer,0,buffer.length);
				}
				else{
					onceRead=fis.read(buffer, 0, (int)(file.length()-alPass));
				}
				alPass+=onceRead;
				dos.write(buffer, 0, onceRead);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fis.close();
		if(alPass==file.length()){
			System.out.println("结点向客户发送文件成功");
			return true;
		}
		else{
			System.out.println("结点向客户发送文件失败");
			return false;
		}
	}
    
    public boolean remove(Socket socket,DataOutputStream dos,DataInputStream dis) throws Exception{
		String fileName=dis.readUTF();//uuid
		
		String path=Storage.StorageNode.fs.getRootFolder()+File.separator+fileName;
		File file=new File(path);
		long size=file.length();
		if(file.delete()){
			dos.writeBoolean(true);//通知客户主结点已经删除成功
			Storage.StorageNode.fs.setFileNum(Storage.StorageNode.fs.getFileNum()-1);
			long tempSize=Storage.StorageNode.fs.getRestVolume()+size;
			Storage.StorageNode.fs.setRestVolume(tempSize);
			dos.flush();
		}else{
			dos.writeBoolean(false);
			return false;
		}
		String IP=dis.readUTF();
		int port=dis.readInt();
		Socket s2=new Socket(IP,port);//主与备份建立链接
		DataOutputStream dos2=new DataOutputStream(s2.getOutputStream());
		dos2.writeInt(5);
		dos2.writeUTF(fileName);
		dos2.flush();
		dos2.close();
		return true;
	}
    
    public boolean copy(Socket socket,DataOutputStream dos,DataInputStream dis) throws Exception{
		long size=dis.readLong();
		String fileName=dis.readUTF();
		//String Ip=dis.readUTF();
		//int port=dis.readInt();
		
		//System.out.println(Ip+" "+port);
		//System.out.println(FileServer.sn.size());
		
		String path=Storage.StorageNode.fs.getRootFolder()+File.separator+fileName;
		
		File file=new File(path);
		FileOutputStream fos=new FileOutputStream(file);
		BufferedOutputStream bos=new BufferedOutputStream(fos);
		
		long alPass=0;
		int onceRead=0;
		byte[] buffer=new byte[1000];
		while(alPass<size){
			if(size-alPass>=buffer.length){
				onceRead=dis.read(buffer,0,buffer.length);
			}
			else{
				onceRead=dis.read(buffer,0,(int)(size-alPass));
			}
			alPass+=onceRead;
			bos.write(buffer,0,onceRead);
		}
		
		bos.close();
		fos.close();
		if(alPass==size){
			Storage.StorageNode.fs.setFileNum(Storage.StorageNode.fs.getFileNum()+1);
			long tempSize=Storage.StorageNode.fs.getRestVolume()-size;
			Storage.StorageNode.fs.setRestVolume(tempSize);
			System.out.println("备份存储成功");
			return true;
		}
		else{
			System.out.println("备份存储失败");
			return false;
		}
	}
    
    public boolean deleteCopy(Socket socket,DataOutputStream dos,DataInputStream dis) throws Exception{
		String fileName=dis.readUTF();
		String path=Storage.StorageNode.fs.getRootFolder()+File.separator+fileName;
		File file=new File(path);
		long size=file.length();
		if(file.delete()){
			Storage.StorageNode.fs.setFileNum(Storage.StorageNode.fs.getFileNum()-1);
			Storage.StorageNode.fs.setRestVolume(Storage.StorageNode.fs.getRestVolume()+size);
			System.out.println("备份删除成功");
			return true;
		}
		else{
			System.out.println("备份删除失败");
			return false;
		}
	}
    
    public boolean uploadWithCopy(Socket socket,DataOutputStream dos,DataInputStream dis)throws Exception{
    	long size=dis.readLong();
    	String fileName=dis.readUTF();
    	
    	File file=new File(Storage.StorageNode.fs.getRootFolder()+File.separator+fileName);
    	
    	FileOutputStream fos=new FileOutputStream(file);
    	BufferedOutputStream bos=new BufferedOutputStream(fos);
    	
    	long alPass=0;
		int onceRead=0;
		byte[] buffer=new byte[1000];
		while(alPass<size){
			if(size-alPass>=buffer.length){
				onceRead=dis.read(buffer,0,buffer.length);
			}
			else{
				onceRead=dis.read(buffer,0,(int)(size-alPass));
			}
			alPass+=onceRead;
			bos.write(buffer,0,onceRead);
		}
		bos.close();
		fos.close();
		
		if(alPass==size){
			System.out.println("备份结点与客户直接通信成功");
			Storage.StorageNode.fs.setFileNum(Storage.StorageNode.fs.getFileNum()+1);
			long tempSize=Storage.StorageNode.fs.getRestVolume()-size;
			Storage.StorageNode.fs.setRestVolume(tempSize);
			return true;
		}
		else{
			System.out.println("备份结点与客户直接通信失败");
			return false;
		}
    	
    }

	@Override
	public void service(Socket socket) {
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
				case 4://主结点与备份之间的链接
					copy(socket,dos,dis);
					break;
				case 5:
					deleteCopy(socket,dos,dis);
					break;
				case 6://client与主结点通信失败后直接与copy通信
					uploadWithCopy(socket,dos,dis);
					break;
				default:
					break;
				}
			//}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
