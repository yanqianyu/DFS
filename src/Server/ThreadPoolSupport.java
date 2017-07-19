package Server;

import java.util.*; //下面的程序可以替换ThreadSupport.java
import java.net.Socket;

public class ThreadPoolSupport implements IOStrategy { // ThreadPoolSupport.java
	private ArrayList threads = new ArrayList();
	private final int INIT_THREADS = 50;
	private final int MAX_THREADS = 100;
	private IOStrategy ios = null;

	public ThreadPoolSupport(IOStrategy ios) { // 创建线程池
		this.ios = ios;
		for (int i = 0; i < INIT_THREADS; i++) {
			IOThread t = new IOThread(ios); // 传递协议对象，但是还没有socket
			t.start(); // 启动线程，进入线程体后都是wait
			threads.add(t);
		}
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		} // 等待线程池的线程都“运行”
	}

	public void service(Socket socket) { // 遍历线程池，找到一个空闲的线程，
		IOThread t = null; // 把客户端交给“它”处理
		boolean found = false;
		for (int i = 0; i < threads.size(); i++) {
			t = (IOThread) threads.get(i);
			if (t.isIdle()) {
				found = true;
				break;
			}
		}
		if (!found) // 线程池中的线程都忙，没有办法了，只有创建
		{ // 一个线程了，同时添加到线程池中。
			t = new IOThread(ios);
			t.start();
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}
			threads.add(t);
		}
		t.setSocket(socket); // 将服务器端的socket对象传递给这个空闲的线程
	} // 让其开始执行协议，向客户端提供服务
}
