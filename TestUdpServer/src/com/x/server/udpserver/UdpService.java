package com.x.server.udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;


public class UdpService {
    public static InetSocketAddress socketAddress = null; // 服务监听个地址  
    public static DatagramSocket datagramSocket = null; //连接对象  
    
	private static UdpService server = null;
	
	private UdpService() {
	}
	
	public static synchronized UdpService getInstance() {
		if (server == null) {
			server = new UdpService();
		}
		return server;
	}
	
	public static int startServer(int serverPort) {
		try {  
            socketAddress = new InetSocketAddress("10.6.6.92", 2333);  
            datagramSocket = new DatagramSocket(socketAddress);  
            datagramSocket.setSoTimeout(5 * 1000);
            System.out.println("服务端已经启动");  
        } catch (Exception e) {  
            datagramSocket = null;  
            System.err.println("服务端启动失败");  
            e.printStackTrace();  
        }
		return 0;
	}
	
	public boolean stopServer() {
		return true;
	}
	
    /** 
     * 接收数据包，该方法会造成线程阻塞 
     * @return 
     * @throws Exception  
     * @throws IOException 
     */  
    public static DatagramPacket receive(DatagramPacket packet) throws Exception {  
        try {  
            datagramSocket.receive(packet);  
            return packet;  
        } catch (Exception e) {  
            throw e;  
        }  
    } 
}
