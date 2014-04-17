package com.x.server.msg;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.x.server.common.World;

public class MsgSendThread extends Thread{

	private static SendMsgQueue queueManage = SendMsgQueue.getInstance();
	private static World world = World.getInstance();
	private int THREAD_POOL_SIZE = 200;
	private boolean needSleep = false;
	   
	private final Executor pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	public MsgSendThread(){}
	
	public MsgSendThread(int poolsize){
		THREAD_POOL_SIZE = poolsize;
	}
	
	public void run(){
//		logger.info("服务器消息发送线程启动......");
		while(true){
			try {
				/*if(queueManage.getSendMessageQueueSize()==0){
					sleep(10);
				}*/
				if(needSleep){
					sleep(10);
				}
			} catch (InterruptedException e) {
//				logger.error("消息发送线程sleep出错了");
			}
			needSleep = sendMessage();		
		}
	}
	
	public boolean sendMessage() {
		final SendMessage msg = queueManage.getMsg();
		if (msg != null) {
			final AtomicBoolean lock = world.getActorSendMessageGetLock(msg.getPlayerId());
			lock.set(true);
			pool.execute(new Runnable() {
				public void run() {
					try {
//						logger.info("发送通信消息：Command=" + msg.getCommand());

						long t1 = System.currentTimeMillis();

//						// 获取用户Channel
//						Channel channel = msg.getChannelHandlerContent().getChannel();
//
//						// 发送消息
						msg.send();
//						if (channel != null && channel.isConnected()) {
//							byte[] msgbyte = msg.encode();
//							ChannelBuffer cb = ChannelBuffers.buffer(msgbyte.length);
//							cb.writeBytes(msgbyte);
//							channel.write(cb);
//						}

						if ((System.currentTimeMillis() - t1) > 3000) {
//							logger.error("发送消息时间超过3秒：消息command="+ msg.getCommand());
						}
						
						//TODO 测试用
//						Integer count = GameWorld.commandSendNum.get(msg.getCommand());
//						if(count==null){
//							GameWorld.commandSendNum.put(msg.getCommand(), 1);
//						}else{
//							GameWorld.commandSendNum.put(msg.getCommand(), count+1);
//						}

//						logger.info("通信消息发送完成：Command=" + msg.getCommand());

					} catch (Exception ex) {
//						logger.error("发送通信消息出错：" + ex.getMessage());
					}finally{
						world.returnSendMessage(msg);
						lock.set(false);
					}					
				}
			});
			return false;
		}else{
			return true;
		}
	}
}
