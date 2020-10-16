package com.aidl.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

//tanjingui 服务器端暴露service  client通过aidl数据接口通讯  这个接口包括双方双向的交互方法
public class MyService extends Service {
    //AIDL不支持正常的接口回调，使用RemoteCallbackList实现接口回调
    private RemoteCallbackList<IReceiveMsgListener> mReceiveListener = new RemoteCallbackList<IReceiveMsgListener>();

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    //tanjingui 每个adli文件会生成一个继承自动生成的Stub的binder
    class MyBinder extends IMsgManager.Stub {               //tanjingui  ------begin  send-------  这个是提供给自己方activity 去调用的
        //发送消息                                          //   同时 也可以暴露给client调用
        public void sendMsg(Msg msg) {
             Log.d("isVertify","333333");
             receiveMsg(msg);
        }

        //注册
        @Override
        public void registerReceiveListener(IReceiveMsgListener receiveListener) throws RemoteException {
            Log.d("isVertify","22222");
            mReceiveListener.register(receiveListener);     //-------------tanjingui  来自于client端的listener 好好保留  【receiveListener】【mReceiveListener】
                                                            //也可能是自己的应用在监听
//            //通知Callback
//            final int N = mReceiveListener.beginBroadcast();
//            //通知Callback循环结束
//            mReceiveListener.finishBroadcast();
        }

        //解除注册
        @Override
        public void unregisterReceiveListener(IReceiveMsgListener receiveListener) throws RemoteException {
            boolean success = mReceiveListener.unregister(receiveListener);
            if (success){
                Log.d("tag","===  解除注册成功");
            }else {
                Log.d("tag","===  解除注册失败 ");
            }
//            final  int N = mReceiveListener.beginBroadcast();
//            mReceiveListener.finishBroadcast();
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }

    //收到消息处理     //tanjingui  服务器调用Binder里面的sendMsg   sendMsg持有client的回调  可以顺利发送
    private void receiveMsg(Msg msg) {
        Log.d("isVertify","4444444");  //tanjingui   ------begin  send-------  给所有client发送消息
        //通知Callback循环开始,返回N为实现mReceiveListener回调的个数
        final int N = mReceiveListener.beginBroadcast();  //tanjingui  RemoteCallbackList 系统监听组件--注册了的监听的集合

        msg.setMsg(msg.getMsg());
        for (int i = 0; i < N; i++){        //tanjingui  有多个client会给service发送消息
            IReceiveMsgListener listener = mReceiveListener.getBroadcastItem(i);
            if (listener != null){
                try {
                    listener.onReceive(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        //通知通知Callback循环结束
        mReceiveListener.finishBroadcast();
    }

}
