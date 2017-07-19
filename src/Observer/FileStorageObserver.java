package Observer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Frame.FileNode;
import Frame.FileStorage;
import java.lang.*;
public class FileStorageObserver extends JFrame{
	private DefaultTableModel model=null;
	private JTable table=null;
	private Integer time;
	
	public FileStorageObserver(int time){
		super("FileStorageObserver");
		this.time =time;
		String[][] datas={};
	      String[] titles = { "服务器名字", "IP地址" ,"端口号","最大存储空间","剩余存储空间","存储路径","是否可用"};
	      model = new DefaultTableModel(datas, titles);
	      table = new JTable(model);
	      add(new JScrollPane(table));
	      setDatas();
	      setSize(500, 400);
	      setLocationRelativeTo(null);
	      setDefaultCloseOperation(EXIT_ON_CLOSE);
	      setVisible(true);
	      
	      new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setDatas();
			}
		}, time*1000,time*1000);
	}
	public void setDatas()
	{
		   Socket socket;
		try {
			socket = new Socket("localhost", 4321);
			 DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
			   DataInputStream dis=new DataInputStream(socket.getInputStream());
			   dos.writeInt(5);
			   dos.flush();
			  // System.out.println("connect");
			   String json=dis.readUTF();
			   //System.out.println(json);
			   java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<FileStorage>>() {}.getType();  
			   List<FileStorage> fileStorages=new Gson().fromJson(json,type );
			   model.setRowCount(0);
			   for (FileStorage fileStorage : fileStorages) {
				   
				model.addRow(new String[]{fileStorage.getName(),fileStorage.getIP(),new Integer(fileStorage.getPort()).toString(),new Long(fileStorage.getVolume()).toString(),new Long(fileStorage.getRestVolume()).toString(),fileStorage.getRootFolder(),new Boolean(fileStorage.getUsed()).toString()});
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws UnknownHostException, IOException{
		new FileStorageObserver(2);
	}
}
