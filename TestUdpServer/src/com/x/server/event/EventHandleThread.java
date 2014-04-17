package com.x.server.event;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import tutorial.Basemessage.BaseMessage;

import com.google.protobuf.ByteString;
import com.x.server.common.World;
import com.x.server.msg.SendMessage;
import com.x.util.ProtocolDefine;

/**
 * Class name:EventHandleThread
 * Description:事件处理线程，处理方式为从事件队列中获取一个事件，
 * 调用影响角色选择器得到所有需要处理事件的角色列表，然后调用事件处理接口进行处理。
 */
public class EventHandleThread extends Thread implements ProtocolDefine{
   private static EventQueue queue = EventQueue.getInstance();//使用玩家事件获取锁
   private static World world = World.getInstance();  
   private int THREAD_POOL_SIZE = 100;//线程池的大小，默认值
   private final Executor pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
   
   public EventHandleThread(){
   }
   
   public EventHandleThread(int poolsize){
	   this.THREAD_POOL_SIZE = poolsize;
   }
   
   public void run(){
//		logger.info("服务器事件处理线程启动......");
		boolean needSleep = false;
		while (true) {
			try {
				if (needSleep) {
					sleep(10);
				}
			} catch (Exception e) {
//				logger.error("事件处理线程sleep出错了");
			}
			needSleep = enventHandle();
		}
   }
   
   public boolean enventHandle(){
	   final ServerEvent event = queue.getEvent();	   
	   if(event!=null){
		   final AtomicBoolean lock = world.getActorEventGetLock(event.getPlayerId());
		   lock.set(true);
		   pool.execute(new Runnable() {
			   public void run(){
				   try{ 
//						logger.info("事件处理线程获取到事件:" + event.getCommand()+";actorid="+event.getActorid());
//						logger.debug("事件处理线程处理事件中");
						long t1 = System.currentTimeMillis();
//						try{
//							if(event.getCommand()>ProtocolDefine.MAX_BASE_PROTOCAL_NUM){
//								//把事件投递给业务层处理
//								handler.onEvent(event);
//							}else{
//								//把事件投递给底层处理
//								baseHandler.onEvent(event);
//							}
////							logger.info("事件处理线程处理事件完成:" + event.getCommand());
//						}catch(Exception ee){
//							logger.error("事件处理出错：", ee);
//						}
						//事件投递
						SendMessage sm = world.getSendMessage(event);
						BaseMessage.Builder builder = BaseMessage.newBuilder();
						builder.setCommand(ProtocolDefine.FIRST_CONNECT_LOGIN);
						builder.setMsgType(ByteString.copyFrom(new byte[]{1}));
						builder.addIntValue(10086);
						builder.addStrValue("Hello world");
						
						System.out.println("事件分发: " + event.getbMsg().getStrValue(0));
						
						sm.setMsg(builder.build());
						world.replyMessage(sm, event);
						
						long usetime = System.currentTimeMillis()-t1;
						if(usetime>1000){
//							logger.error("处理事件耗时超过1秒：事件command="+event.getCommand()+";actorid="+event.getActorid()+";处理时间:"+usetime);
						}
				   }catch(Exception ex){
//					   logger.error("事件处理线程出错：",ex);
				   }finally{
					   //把事件对象返回对象池
					   world.returnServerEvent(event);
					   //解锁
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