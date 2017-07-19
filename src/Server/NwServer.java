package Server;

import java.net.*;

public class NwServer { // NwServer.java，负责接受连接请求，并将创建的Socket对象
	// 通过IOStrategy接口传递给ThreadSupport对象
	public NwServer(int port, IOStrategy ios) { // 这个方法将在主线程中执行
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("server is ready");
			while (true) {
				Socket socket = ss.accept(); // 负责接受连接请求，
				ios.service(socket); // 将服务器端的socket对象传递给
			} // ThreadSupport对象
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
