package Server;

//下面是修改后的线程类，该程序对同步控制，wait和notify方法的调用要求较高
import java.net.*;

public class IOThread extends Thread {
	private Socket socket = null;
	private IOStrategy ios = null;

	public IOThread(IOStrategy ios) { // 请比较上一节中的IOThread类的构造方法
		this.ios = ios; // 有何不同？
	}

	public boolean isIdle() { // 如果socket变量为空，那么这个线程当然是空闲的
		return socket == null;
	}

	public synchronized void setSocket(Socket socket) {
		this.socket = socket; // 传递给这个阻塞的线程一个“任务”，并唤醒它。
		notify();
	}

	public synchronized void run() { // 这个同步方法并不是保护什么共享数据，
		while (true) { // 仅仅因为wait方法调用必须拥有对象锁
			try {
				wait(); // 进入线程体后，立刻进入阻塞，等待状态
				ios.service(socket); // 被唤醒后，立刻开始执行服务协议
				socket = null; // 服务结束后，立刻返回到空闲状态
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
