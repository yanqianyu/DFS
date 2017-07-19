package Storage;

import java.io.*;
import java.util.*;
import Frame.FileStorage;
import Server.NwServer;
import Server.ThreadPoolSupport;
import util.SSUtil;

public class StorageNode {
	public static FileStorage fs;
	
	public static void main(String[] args) throws Exception{
		fs=SSUtil.getStorageServerByFile(args[0]);
		new Timer().schedule(new MyStorageNodeTask(fs.toString()), 0,5000);//每5s调度一次
		new NwServer(fs.getPort(),new ThreadPoolSupport(new StorageProtocol()));
	}
	
}
