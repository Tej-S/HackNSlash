package com.mephestokhaan.hacknslash;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */

class HandeldToHandeldCommunicator
{
    private Context context;
    private String IP;
    private boolean isServer;
    private boolean listening = true;

    MessageReceiverListener messageDelegate;

    private UDPReceiver receiver = new UDPReceiver();

    public HandeldToHandeldCommunicator(String ip, boolean isServer, Context context, MessageReceiverListener delegate)
    {
        this.IP = ip;
        this.isServer = isServer;
        this.context = context;
        this.listening = true;
        this.messageDelegate = delegate;

        receiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void Stop()
    {
        listening = false;
        receiver.cancel(true);
    }

    public void SendMessage(String message)
    {
        new UDPSender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,message);
    }

    class UDPSender extends AsyncTask<String, Void, Void>
    {
        protected Void doInBackground(String... urls)
        {
            if (urls.length > 0 )
            {
                try
                {
                    String ip = IP;
                    int port = isServer?4444:4445;
                    String message = urls[0];
                    sendOverUDP(InetAddress.getByName(ip), port, message);
                } catch (IOException e) {
                    Log.e("SEND", "error sending " + urls[0] + e.toString());
                }
            }

            return null;
        }

        private void sendOverUDP(InetAddress IP, int PORT, String message) throws IOException
        {
            DatagramSocket socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), IP, PORT);
            socket.send(packet);
            socket.close();
        }

        protected void onPostExecute(Void feed)
        {

        }
    }

    class UDPReceiver extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            byte[] lMsg = new byte[4096];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
            DatagramSocket ds = null;

            try
            {
                ds = new DatagramSocket(isServer?4445:4444);

                while(listening)
                {
                    ds.receive(dp);
                    String message =  new String(lMsg, 0, dp.getLength());
                    if(messageDelegate != null)
                    {
                        //TODO should be on main thread?
                        messageDelegate.onHandHeldHandheldMessageReceived(message);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (ds != null)
                {
                    ds.close();
                }
            }

            return null;
        }
    }

}


