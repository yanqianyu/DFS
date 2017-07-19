package Frame;

import java.io.*;

public class FileStorage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String IP;
	private int port;
	private long volume;
	private long restVolume;
	private String rootFolder;
	private boolean canBeUsed;
	private int fileNum;
	
	
	public FileStorage(){
		super();
	}
	
	public FileStorage(String name,String IP,int port,long volume,long restVolume,String rootFolder,boolean canBeUsed,int fileNum){
		this.name=name;
		this.IP=IP;
		this.port=port;
		this.volume=volume;
		this.restVolume=restVolume;
		this.rootFolder=rootFolder;
		this.canBeUsed=canBeUsed;
		this.fileNum=fileNum;
	}
	
	
	public boolean getUsed(){
		return canBeUsed;
	}
	
	public void setUsed(boolean cbu){
		this.canBeUsed=cbu;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getIP(){
		return IP;
	}
	
	public void setIP(String IP){
		this.IP=IP;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int port){
		this.port=port;
	}
	
	public long getVolume(){
		return volume;
	}
	
	public void setVolume(long volume){
		this.volume=volume;
	}
	
	public long getRestVolume(){
		return restVolume;
	}
	
	public void setRestVolume(long restVolume){
		this.restVolume=restVolume;
	}
	
	public String getRootFolder(){
		return rootFolder;
	}
	
	public void setRootFolder(String rootFolder){
		this.rootFolder=rootFolder;
	}
	
	public int getFileNum(){
		return this.fileNum;
	}
	
	public void setFileNum(int fileNum){
		this.fileNum=fileNum;
	}
	
	public String toString(){
		//String message="FileStorage name:"+name+", IP="+IP+", port"+port+
			//	", volume"+volume+", restVolume"+restVolume+", rootFolder"+rootFolder+", canBeUsed"+canBeUsed;
		
		String message=name+" "+IP+" "+port+" "+volume+" "+restVolume+" "+rootFolder+" "+canBeUsed+" "+fileNum;
		return message;
	}
}
