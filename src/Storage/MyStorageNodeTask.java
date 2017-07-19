package Storage;

import java.util.*;
import java.net.*;
import java.io.*;
import Frame.FileStorage;

public class MyStorageNodeTask extends TimerTask{
	private String mess;
	public MyStorageNodeTask(String mess){
		this.mess=mess;
	}
	
	public void run(){
		try{
			byte[] data=mess.getBytes();
			DatagramPacket dp=new DatagramPacket(data,data.length,InetAddress.getByName("localhost"),4321);
			DatagramSocket ds=new DatagramSocket();
			ds.send(dp);
			ds.close();
		}catch(Exception e){
			e.printStackTrace();
		}
				
	}
}
