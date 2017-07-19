package Client;
import java.io.*;
import java.net.*;
import java.net.*;
import java.util.zip.*;
public class FileClient {
	
	private Socket socket=null;
	private DataOutputStream dos=null;
	private DataInputStream dis=null;
	public static int comLength=0;
	public static int deLength=0;
	public static long len;
	public FileClient()throws Exception{
		socket =new Socket("localhost",4321);
		dis=new DataInputStream(socket.getInputStream());
		dos=new DataOutputStream(socket.getOutputStream());
	}
	
	private static final int numOfEncAndDec = 0x99; //加密解密秘钥
	private static int dataOfFile = 0; //文件字节内容
    private static void EncFile(File srcFile, File encFile) throws Exception {//加密
    	if(!srcFile.exists()){
    		System.out.println("source file not exixt");
    		return;
    		}
	        
    	if(!encFile.exists()){
	        System.out.println("encrypt file created");
	        encFile.createNewFile();
	        }
	    InputStream fis  = new FileInputStream(srcFile);
	    OutputStream fos = new FileOutputStream(encFile);
	        
	    while ((dataOfFile = fis.read()) > -1) {
	        fos.write(dataOfFile^numOfEncAndDec);
	    }
	        
	    fis.close();
	    fos.flush();
	    fos.close();
	    }
	
    private static void DecFile(File encFile, File decFile) throws Exception {//解密
    	if(!encFile.exists()){
	    	System.out.println("encrypt file not exixt");
	    	return;
	    }
	    if(!decFile.exists()){
	    	System.out.println("decrypt file created");
	    	decFile.createNewFile();
	    }

	    InputStream fis  = new FileInputStream(encFile);
	    OutputStream fos = new FileOutputStream(decFile);

	    while ((dataOfFile = fis.read()) > -1) {
	    fos.write(dataOfFile^numOfEncAndDec);
	    }
	    fis.close();
	    fos.flush();
	    fos.close();
	    }
	
	public boolean upload(String filePath) throws Exception{
		dos.writeInt(1);//告诉FileServer要上传
		dos.flush();
		File file=new File(filePath);
		/*
		byte[] in =new byte[1000];
		byte[]com =new byte[1000];
		in =getBytes(filePath);
		com=compress(in);
		getFile(com, "/Users/apple/Desktop/", file.getName()+".zip");
		*/
		
		File tmpfile2 =new File("/Users/apple/Documents/workspace/DFS/bin/"+file.getName()+".zip");//压缩后的文件
		zip(file,tmpfile2.getName());
		File tmpfile=new File("/Users/apple/Desktop/"+file.getName()+".zip.enc");//加密后的文件
		if(tmpfile2.exists()==false)
			tmpfile2.createNewFile();
		EncFile(tmpfile2,tmpfile);//加密
		tmpfile2.delete();
		
		dos.writeLong(tmpfile.length());// 文件大小
		dos.flush();
		
		boolean findNode=dis.readBoolean();//Server通知是否有可用结点
		
		if(findNode==false){//没有可用结点
			System.out.println("No useful storage node");
			return false;
		}
		
		dos.writeUTF(file.getName());//通知server文件名
		dos.flush();
		
		String firstIp=dis.readUTF();//server告诉客户主存储点的信息
		int firstPort=dis.readInt();
		
		String secIp=dis.readUTF();//server告诉客户备用结点的信息
		int secPort=dis.readInt();
		
		String fileNameUUID=dis.readUTF();//server通知用户生成的文件名
		System.out.println(file.getName()+"--->"+fileNameUUID);
		
		
		
		//建立与结点的链接 使用的是StorageProtocol
		Socket s1=new Socket(firstIp,firstPort);
		DataInputStream dis0=new DataInputStream(s1.getInputStream());
		DataOutputStream dos0=new DataOutputStream(s1.getOutputStream());
		
		dos0.writeInt(1);//通知Stoarge要上传文件了
		//dos0.writeLong(file.length());//客户通知结点文件大小 便于上传之后的核对
		dos0.writeUTF(fileNameUUID);//以随机生成的名称传递
		
		dos0.writeUTF(secIp);//告诉主存储结点备份结点的信息
		dos0.writeInt(secPort);
		
		//文件传输过程
		byte[] buffer=new byte[1000];
		long alRead=0;//已经传输的部分
		int onceRead=0;//单次传输的部分
		//getFile(compress(getBytes(filePath)), "/Users/apple/Desktop/", "tmp.zip");
		//File tmpfile =new File("/Users/apple/Desktop/tmp.zip");
		//File tmpfile=new File(getFile(compress(getBytes(filePath)), "/Users/apple/Desktop/", "tmp.zip").toString());
		dos0.writeLong(tmpfile.length());//客户通知结点文件大小 便于上传之后的核对
		len=tmpfile.length();
		FileInputStream fis=new FileInputStream(tmpfile);
		BufferedInputStream bis=new BufferedInputStream(fis);
		System.out.println(len);
		
		try{
			/*
			while((onceRead=bis.read(buffer))!=-1){
				alRead+=onceRead;
				dos0.write(buffer,0,onceRead);
				dos0.flush();
			}*/
			while(alRead<len){
				if(len-alRead>=buffer.length){
					onceRead=bis.read(buffer, 0, buffer.length);
				}
				else{
					onceRead=bis.read(buffer,0,(int)(len-alRead));
				}
				dos0.write(buffer,0,onceRead);
				dos0.flush();
				alRead+=onceRead;
			}
			bis.close();
			fis.close();
		}catch(Exception e){//发生意外 与备份结点建立联系
			e.printStackTrace();
			s1.close();
			dos0.close();
			dis0.close();
			
			Socket s2=new Socket(secIp,secPort);
			DataInputStream dis2=new DataInputStream(s2.getInputStream());
			DataOutputStream dos2=new DataOutputStream(s2.getOutputStream());
			
			dos2.writeInt(6);//通知备用结点要上传文件了
			dos2.writeLong(file.length());
			dos2.writeUTF(fileNameUUID);
			
			byte[] buffer2=new byte[1000];
			long alRead2=0;
			int onceRead2=0;
			FileInputStream fis2=new FileInputStream(tmpfile);
			BufferedInputStream bis2=new BufferedInputStream(fis2);
			
			try{
				while(alRead2<len){
					if(len-alRead2>=buffer2.length){
						onceRead2=bis.read(buffer2, 0, buffer.length);
					}
					else{
						onceRead2=bis.read(buffer2,0,(int)(len-alRead2));
					}
					dos0.write(buffer2,0,onceRead2);
					alRead2+=onceRead2;
				}
				fis2.close();
				bis2.close();
			}catch(Exception e2){//再次发生意外
				e2.printStackTrace();
				System.out.println(file.getName()+" upload to second node failed!");
			}finally{
				dos0.close();
				dis0.close();
				fis2.close();
				bis2.close();
				s2.close();
			}
		}finally{
			fis.close();
			bis.close();
			dos0.close();
			dis0.close();
			s1.close();
		}
		//if(alRead==file.length()){
		 //tmpfile.delete();
		if(alRead==len){
			System.out.println(file.getName()+" upload to first node successed!");
			return true;
		}
		else{
			System.out.println(file.getName()+" upload to first node failed!");
			return false;
		}
	}
	
	public boolean download(String FileUUID) throws Exception{
		dos.writeInt(2);//通知server要下载文件了
		dos.writeUTF(FileUUID);
		dos.flush();
		boolean findFile=dis.readBoolean();//server通知用户文件是否存在
		if(findFile==false){
			System.out.println(FileUUID+" isn't exist");
			return false;
		}
		
		int StorageNum=dis.readInt();//server通知用户有几个结点可用
		
		if(StorageNum==0){
			System.out.println("Storages can't be used");
			return false;
		}
		
		if(StorageNum==1){//单个结点可用
			String fileName=dis.readUTF();
			long size=dis.readLong();//server通知client文件大小
			File file=new File("/Users/apple/Desktop/Download/"+fileName);
			
			String IP=dis.readUTF();
			int port=dis.readInt();
			
			dis.close();
			dos.close();
			
			Socket s2=new Socket(IP,port);//建立与StorageNode之间的联系 StorageProtocol
			DataInputStream dis2=new DataInputStream(s2.getInputStream());
			DataOutputStream dos2=new DataOutputStream(s2.getOutputStream());
			
			FileOutputStream fos=new FileOutputStream(file);
			BufferedOutputStream bos=new BufferedOutputStream(fos);
			dos2.writeInt(2);//通知storage要下载了
			dos2.writeUTF(FileUUID);
			dos2.flush();
			
			long alPass=0;
			int read=0;
			byte[] buffer=new byte[1000];
			//byte[] tmpBuffer=new byte[4096];
			try{
				while(alPass<size){
					if(size-alPass>=buffer.length)
						read=dis2.read(buffer,0,buffer.length);
					else
						read=dis2.read(buffer,0,(int)(size-alPass));
					alPass+=read;
					bos.write(buffer,0,read);
				}
				//getFile(decompress(getBytes(file.getPath())), "/Users/apple/Desktop/Download/", fileName);
				//file.delete();
			}catch(Exception e){//下载失败
				fos.close();
				dos2.close();
				dis2.close();
				s2.close();
				return false;
			}//下载成功也要
			finally{
				fos.close();
				dos2.close(); 
				dis2.close();
				s2.close();
				//byte[] de=new byte[1000];
				//byte[] out=new byte[1000];

				//getFile(com,"/Users/apple/Desktop/Download/","zip");
				File temp1=new File("/Users/apple/Desktop/Download/"+fileName);
				File temp2=new File("/Users/apple/Desktop/Download/dec/"+fileName);//解密后文件的位置
				
				DecFile(temp1,temp2);
				temp1.delete();
				
				unzip(temp2.getAbsolutePath(),"/Users/apple/Desktop/Download/"+temp2.getName());
				//de=getBytes("/Users/apple/Desktop/Download/dec/"+fileName);
				//out=decompress(de);
				//getFile(out, "/Users/apple/Desktop/Download/", "back"+fileName);//解压后的文件
				//file.delete();
				//temp1.delete();
				//temp2.delete();
				
			}
			
			if(alPass==size){
				System.out.println("download "+fileName+" successed");
				return true;
			}
			else{
				System.out.println("oops!download "+fileName+" failed");
				return false;
			}
		}
		else if(StorageNum==2){//2个结点均可用
			String fileName=dis.readUTF();
			long size=dis.readLong();
			//System.out.println("file->"+size);
			
			File file=new File("/Users/apple/Desktop/Download/"+fileName);
			
			System.out.println(file.getPath());
			
			String firstIp=dis.readUTF();//主存储的信息
			int firstPort=dis.readInt();
			String secIp=dis.readUTF();//备份结点的
			int secPort=dis.readInt();
			
			dis.close();
			dos.close();
			
			//建立与主存储结点的链接
			Socket s2=new Socket(firstIp,firstPort);
			DataOutputStream dos2=new DataOutputStream(s2.getOutputStream());
			DataInputStream dis2=new DataInputStream(s2.getInputStream());
			FileOutputStream fos=new FileOutputStream(file);
			BufferedOutputStream bos=new BufferedOutputStream(fos);
			
			dos2.writeInt(2);
			dos2.writeUTF(FileUUID);
			dos2.flush();
			
			long alPass=0;
			int read=0;
			byte[] buffer=new byte[1000];
			//byte[] tmpBuffer=new byte[4096];
			try{
				while(alPass<size){
					//System.out.println(alPass);
					if(size-alPass>=buffer.length)
						read=dis2.read(buffer,0,buffer.length);
					else
						read=dis2.read(buffer, 0, (int)(size-alPass));
					alPass+=read;
					bos.write(buffer,0,read);
					bos.flush();
				}
				//file.delete();
			}catch(Exception e){//与主结点链接失败 转为备份结点
				e.printStackTrace();
				fos.close();
				dis2.close();
				dos2.close();
				file.delete();
				
				s2=new Socket(secIp,secPort);
				dos2=new DataOutputStream(s2.getOutputStream());
				dis2=new DataInputStream(s2.getInputStream());
				fos=new FileOutputStream(file);
				
				dos2.writeInt(2);
				dos2.writeUTF(FileUUID);
				dos2.flush();
				
				alPass=0;
				read=0;
				buffer=new byte[1000];
				//tmpBuffer=new byte[4096];
				try{
					while(alPass<size&&read!=-1){
						System.out.println(read);
						if(size-alPass>buffer.length)
							read=dis2.read(buffer,0,buffer.length);
						else
							read=dis2.read(buffer, 0, (int)(size-alPass));
						alPass+=read;
						bos.write(buffer,0,read);
						bos.flush();
					}
					//getFile(decompress(getBytes(file.getPath())), "/Users/apple/Desktop/Download/", fileName+"de");
					//file.delete();
				}catch(Exception e2){
					fos.close();
					dis2.close();
					dos2.close();
					s2.close();
					e2.printStackTrace();
					System.out.println("download "+fileName+" failed");
					return false;
				}finally{
					fos.close();
					dis2.close();
					dos2.close();
					s2.close();
				}
			}finally{
				fos.close();
				dis2.close();
				dos2.close();
				s2.close();
				//byte[] de=new byte[1000];
				//byte[] out=new byte[1000];

				//getFile(com,"/Users/apple/Desktop/Download/","zip");
				File temp1=new File("/Users/apple/Desktop/Download/"+fileName);
				File temp2=new File("/Users/apple/Desktop/Download/dec/"+fileName);//解密后文件的位置
				
				DecFile(temp1,temp2);
				temp1.delete();
				
				unzip(temp2.getAbsolutePath(),"/Users/apple/Desktop/Download/"+temp2.getName());
				//de=getBytes("/Users/apple/Desktop/Download/dec/"+fileName);
				//out=decompress(de);
				//getFile(out, "/Users/apple/Desktop/Download/", "back"+fileName);//解压后的文件
				//file.delete();
				//temp1.delete();
				//temp2.delete();
			}


			if(alPass==size){
				System.out.println("download "+fileName+" successed");
				return true;
			}
			else{
				System.out.println("Sorry!download "+fileName+" failed");
				return false;
			}
		}
		return true;
	}
	
	public boolean delete(String FileUUID)throws Exception{
		dos.writeInt(3);//通知server要删除文件
		dos.writeUTF(FileUUID);
		dos.flush();
		
		boolean findFile=dis.readBoolean();//server通知client文件是否存在
		
		if(findFile==false){
			System.out.println(FileUUID+" doesn't exist");
			return false;
		}
		
		String firstIp=dis.readUTF();
		int firstPort=dis.readInt();
		String secIp=dis.readUTF();
		int secPort=dis.readInt();
		
		try{
			Socket s=new Socket(firstIp,firstPort);
			DataInputStream dis=new DataInputStream(s.getInputStream());
			DataOutputStream dos=new DataOutputStream(s.getOutputStream());
			dos.writeInt(3);//通知storage要删除了
			dos.writeUTF(FileUUID);
			
			boolean result=dis.readBoolean();//主结点通知是否删除成功
			if(result==false){
				System.out.println("main storage failed to delete "+FileUUID);
				return false;
			}
			
			dos.writeUTF(secIp);//通知主结点副结点信息 主结点负责副结点的删除
			dos.writeInt(secPort);
			dos.close();
			dis.close();
		}catch(Exception e){
			System.out.println("main storage failed to delete "+FileUUID);
			return false;
		}
		return true;
	}
	//压缩算法
	public static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;
	public static byte[] compress(byte[] input) {
		byte[] output = new byte[MAX_BUFFER_SIZE];
		// Deflater compresser = new Deflater(Deflater.BEST_SPEED, true);
		Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, true);

		compresser.setInput(input);
		compresser.finish();

		comLength = compresser.deflate(output);
		compresser.end();

		byte[] result = new byte[comLength];
		System.arraycopy(output, 0, result, 0, comLength);
		return result;
	}
	
    public static byte[] getBytes(String filePath){  
        byte[] buffer = null;  
        try {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);  
            byte[] b = new byte[1000];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return buffer;  
    }
    public static void getFile(byte[] bfile, String filePath,String fileName) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try {  
            File dir = new File(filePath);  
            if(!dir.exists()&&dir.isDirectory()){
                dir.mkdirs();  
            }  
            file = new File(filePath+File.separator+fileName);  
            if(file.exists()==false)
            	file.createNewFile();
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(bfile);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            } 

        }
		//return file;  
    }  
	public static byte[] decompress(byte[] input) {
		try {
			Inflater decompresser = new Inflater(true);
			decompresser.setInput(input, 0, input.length);
			byte[] output = new byte[MAX_BUFFER_SIZE];
			int len = decompresser.inflate(output);
			decompresser.end();
			byte[] result = new byte[len];
			System.arraycopy(output, 0, result, 0, len);
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void zip(ZipOutputStream out, File f, String base) throws Exception  
    {  
        if (f.isDirectory()) {  
            File[] files = f.listFiles();  
            base = (base.length() == 0 ? "" : base + "/");  
            for (int i = 0; i < files.length; i++) {  
                zip(out, files[i], base + files[i].getName());  
            }  
        } else {  
            out.putNextEntry(new ZipEntry(base));  
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));  
            int c;  
              
            while ((c = in.read()) != -1) {  
                out.write(c);  
            }  
            in.close();  
        }  
    }  
      
    private void zip(File inputFileName, String zipFileName) throws Exception  
    {  
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));  
        zip(out, inputFileName,"");  
        out.close();  
    }  
      
    //压缩文件，inputFileName表示要压缩的文件（可以为目录）,zipFileName表示压缩后的zip文件  
    public void zip(String inputFileName, String zipFileName) throws Exception  
    {  
        zip(new File(inputFileName), zipFileName);  
    }  
      
    //解压,zipFileName表示待解压的zip文件，unzipDir表示解压后文件存放目录  
    public void unzip(String zipFileName, String unzipDir) throws Exception  
    {  
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFileName));  
        ZipEntry entry;  
        while ((entry = in.getNextEntry()) != null) {  
              
            String fileName = entry.getName();  
             
            //有层级结构，就先创建目录  
            String tmp;  
            int index = fileName.lastIndexOf('/');  
            if (index != -1) {  
                tmp = fileName.substring(0, index);  
                tmp = unzipDir + "/" + tmp;  
                File f = new File(tmp);  
                f.mkdirs();  
            }  
              
            //创建文件  
            fileName = unzipDir + "/" + fileName;  
            File file = new File(fileName);  
            file.createNewFile();  
              
            FileOutputStream out = new FileOutputStream(file);  
            int c;  
            while ((c = in.read()) != -1) {  
                out.write(c);  
            }  
            out.close();  
        }  
        in.close();  
    }  
	
	public static void main(String []args) throws Exception{
		if(args.length<2){
			System.out.println("参数错误");
		}
		FileClient fc=new FileClient();
		if(args[0].equals("upload")){
			fc.upload(args[1]);
		}
		else if(args[0].equals("download")){
			fc.download(args[1]);
		}
		else if(args[0].equals("delete")){
			fc.delete(args[1]);
		}
	}

}
