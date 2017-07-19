package Observer;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.*;

import Frame.FileNode;

public class FileNodeObserver extends JFrame{
	private DefaultTableModel model=null;
	private JTable table=null;
	private Integer time;//刷新的时间间隔
	
	public FileNodeObserver(int time)
	{
		super("FileNodeObserver");
		this.time=time;
		String[][] datas={};
		String[] titles={"文件编号","文件名","文件大小","主存储服务器IP","主存储服务器端口号","备份存储服务器IP","备份存储服务器端口号"};
		model= new DefaultTableModel(datas,titles);
		table= new JTable(model);
		add(new JScrollPane(table));//给table加滚动条
		setDatas();
		setTitle("FileNodeObserver");
		setSize(500,400);
		setLocationRelativeTo(null);//将窗口置于屏幕中央
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		new Timer().schedule(new TimerTask(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setDatas();
			}
		},time*1000,time*1000);
	}
	
	public void setDatas()
	{
		Socket socket;
		try{
			socket= new Socket("localhost", 4321);
			DataOutputStream dos= new DataOutputStream(socket.getOutputStream());
			DataInputStream  dis= new DataInputStream(socket.getInputStream());
			dos.writeInt(4);
			dos.flush();
			//System.out.println("connect");
			String json=dis.readUTF();
			//System.out.println(json);
			//System.out.println("getdata");
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<FileNode>>(){}.getType();  
			List<FileNode> fileNodes=new Gson().fromJson(json,type );
			model.setRowCount(0);
			for(FileNode fileNode : fileNodes)
			{
				model.addRow(new String[]{fileNode.getUuid(),fileNode.getName(),new Long(fileNode.getSize()).toString(),fileNode.getfirstServerIp(),new Integer(fileNode.getfirstServerPort()).toString(),fileNode.getsecServerIP(),new Integer(fileNode.getsecServerPort()).toString()});
			}
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws UnknownHostException, IOException {
		new FileNodeObserver(2);
	}
}
