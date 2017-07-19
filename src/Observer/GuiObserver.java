package Observer;
import java.awt.*;
import java.awt.event.*;
import java.util.Observer;

import javax.swing.JTextArea;
import Observer.FileNodeObserver;
import Observer.FileStorageObserver;

public class GuiObserver {
	private Frame frame;//gui边框
	private JTextArea title;//标题
	private JTextArea topic1;//小标题
	private JTextArea topic2;
	private Button but1;
	private Button but2;
	//private JTextArea topic3;//空行
	
	GuiObserver() {
		// TODO Auto-generated constructor stub
		init();
	}
	
	public void init() {
		System.out.println("test Gui");
		frame =new Frame("Observer");
		frame.setBounds(600,300,600,500);
		frame.setLayout(new FlowLayout());//流式布局
		title=new JTextArea(3,70);
		title.setFont(new Font("宋体", Font.BOLD, 27));
		title.setText("\n\n"+"                             选择监控信息：");
    	topic1=new JTextArea(1,20);
    	topic1.setText("1.查看文件信息");
    	topic1.setFont(new Font("宋体",Font.BOLD,25));
    	topic2=new JTextArea(1,20);
    	topic2.setText("2.查看存储节点信息");
    	topic2.setFont(new Font("宋体",Font.BOLD,25));
    	but1=new Button("选择");
    	but2=new Button("选择");
		eventRespond();
		frame.add(title);
		frame.add(topic1);
		frame.add(but1);
		frame.add(topic2);
		frame.add(but2);
		frame.setVisible(true);
	}
	
	public void eventRespond()
	{
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		but1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new FileNodeObserver(3);
			}
		});
		
		but2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new FileStorageObserver(3);
			}
		});
	}
	
	public static void main(String[] args) {
		new GuiObserver();
	}
}
