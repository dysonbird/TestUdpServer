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
 * Description:�¼������̣߳�����ʽΪ���¼������л�ȡһ���¼���
 * ����Ӱ���ɫѡ�����õ�������Ҫ�����¼��Ľ�ɫ�б�Ȼ������¼�����ӿڽ��д���
 */
public class EventHandleThread extends Thread implements ProtocolDefine{
   private static EventQueue queue = EventQueue.getInstance();//ʹ������¼���ȡ��
   private static World world = World.getInstance();  
   private int THREAD_POOL_SIZE = 100;//�̳߳صĴ�С��Ĭ��ֵ
   private final Executor pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
   
   public EventHandleThread(){
   }
   
   public EventHandleThread(int poolsize){
	   this.THREAD_POOL_SIZE = poolsize;
   }
   
   public void run(){
//		logger.info("�������¼������߳�����......");
		boolean needSleep = false;
		while (true) {
			try {
				if (needSleep) {
					sleep(10);
				}
			} catch (Exception e) {
//				logger.error("�¼������߳�sleep������");
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
//						logger.info("�¼������̻߳�ȡ���¼�:" + event.getCommand()+";actorid="+event.getActorid());
//						logger.debug("�¼������̴߳����¼���");
						long t1 = System.currentTimeMillis();
//						try{
//							if(event.getCommand()>ProtocolDefine.MAX_BASE_PROTOCAL_NUM){
//								//���¼�Ͷ�ݸ�ҵ��㴦��
//								handler.onEvent(event);
//							}else{
//								//���¼�Ͷ�ݸ��ײ㴦��
//								baseHandler.onEvent(event);
//							}
////							logger.info("�¼������̴߳����¼����:" + event.getCommand());
//						}catch(Exception ee){
//							logger.error("�¼��������", ee);
//						}
						//�¼�Ͷ��
						SendMessage sm = world.getSendMessage(event);
						BaseMessage.Builder builder = BaseMessage.newBuilder();
						builder.setCommand(ProtocolDefine.FIRST_CONNECT_LOGIN);
						builder.setMsgType(ByteString.copyFrom(new byte[]{1}));
						builder.addIntValue(10086);
						builder.addStrValue("Hello world");
						
						System.out.println("�¼��ַ�: " + event.getbMsg().getStrValue(0));
						
						sm.setMsg(builder.build());
						world.replyMessage(sm, event);
						
						long usetime = System.currentTimeMillis()-t1;
						if(usetime>1000){
//							logger.error("�����¼���ʱ����1�룺�¼�command="+event.getCommand()+";actorid="+event.getActorid()+";����ʱ��:"+usetime);
						}
				   }catch(Exception ex){
//					   logger.error("�¼������̳߳���",ex);
				   }finally{
					   //���¼����󷵻ض����
					   world.returnServerEvent(event);
					   //����
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