package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class UDPServer {
	public UDPServer(int port) throws Exception{
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
		DatagramSocket socket=new DatagramSocket(port);
		while(true){
			byte[] data=new byte[1024];
			DatagramPacket packet=new DatagramPacket(data,data.length);
			socket.receive(packet);
			String mess=new String(data);
			fixedThreadPool.execute(new UDPProtocol(mess));
		}
	}
}
