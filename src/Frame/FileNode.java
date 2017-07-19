package Frame;

import java.io.*;

public class FileNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uuid;
	private String name;
	private long size;
	private String firstServerIP;
	private int firstServerPort;
	private String secServerIP;//备份
	private int secServerPort;
	private long compressSize;//压缩后大小
	
	public String toString(){
		String fileMess="FileNode uuid="+uuid+", name"+name+", size"+size+", firstServerIP"+firstServerIP
				+", firstServerPort"+firstServerPort+", secServerIP"+secServerIP+", secServerPort"+secServerPort+", compressSize"+compressSize;
		return fileMess;
	}
	
	public FileNode(){
		super();
	}
	
	public FileNode(String uuid,String name,long size,String firstServerIP,int firstServerPort,String secServerIP,int secServerPort){
		this.uuid=uuid;
		this.name=name;
		this.size=size;
		this.firstServerIP=firstServerIP;//主要
		this.firstServerPort=firstServerPort;
		this.secServerIP=secServerIP;//备份
		this.secServerPort=secServerPort;
		//this.compressSize=compressSize;
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getfirstServerIp() {
		return firstServerIP;
	}
	public void setfirstServerIp(String firstServerIP) {
		this.firstServerIP = firstServerIP;
	}
	public int getfirstServerPort() {
		return firstServerPort;
	}
	public void setfirstServerNode(int firstServerPort) {
		this.firstServerPort = firstServerPort;
	}
	public String getsecServerIP() {
		return secServerIP;
	}
	public void setsecServerIP(String secServerIP) {
		this.secServerIP = secServerIP;
	}
	public Integer getsecServerPort() {
		return secServerPort;
	}
	public void setsecServerPort(int secServerPort) {
		this.secServerPort = secServerPort;
	}
}
