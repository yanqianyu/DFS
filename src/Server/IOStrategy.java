package Server;

public interface IOStrategy { // IOStrategy.java
	public void service(java.net.Socket socket);  //对传入的socket对象进行处理
}
