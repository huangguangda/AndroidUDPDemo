package cn.edu.gdmec.android.androidudpdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//不同之处是 ip 地址，广播的 ip 地址是 255.255.255.255，单播的 ip 地址是目标主机地址。

public class MainActivity extends AppCompatActivity {
    private EditText content;
    private static String MULTICAST_IP = "224.0.0.1";
    private static String BROADCAST_IP = "255.255.255.255";
    private static int MULTICAST_PORT = 8886;
    private static int BROADCAST_PORT = 8887;
    private static int UNICAST_PORT = 8888;
    private boolean isRuning = true;
    private String serverHost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onBroadcastReceive();
        onUnicastReceive();
        onMulticastSendReceive();
    }
    /**
     * 广播发送
     */
    public void onBroadcastSend(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(BROADCAST_IP);
                    DatagramSocket datagramSocketSend = new DatagramSocket();
                    byte[] data = content.getText().toString().getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
                    datagramSocketSend.send(datagramPacket);
                    // 发送设置为广播
                    datagramSocketSend.setBroadcast(true);
                    datagramSocketSend.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    /**
     * 广播接受
     */
    public void onBroadcastReceive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建接收数据报套接字并将其绑定到本地主机上的指定端口
                    DatagramSocket datagramSocket = new DatagramSocket(BROADCAST_PORT);
                    while (isRuning) {
                        byte[] buf = new byte[1024];
                        final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                        datagramSocket.receive(datagramPacket);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                serverHost = datagramPacket.getAddress().getHostAddress();
                                final String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength())
                                        + " from " + datagramPacket.getAddress().getHostAddress() + ":" + datagramPacket.getPort();
                                Toast.makeText(MainActivity.this, "广播接受=" + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 单播发送
     */
    public void onUnicastSend(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(serverHost);
                    byte[] message = content.getText().toString().getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length,
                            inetAddress, UNICAST_PORT);
                    DatagramSocket datagramSocket = new DatagramSocket();
                    datagramSocket.send(datagramPacket);
                    datagramSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * 单播接受
     */
    public void onUnicastReceive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket server = new DatagramSocket(UNICAST_PORT);
                    while (isRuning) {
                        byte[] buf = new byte[1024];
                        final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                        server.receive(datagramPacket);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String receiveMsg = new String(datagramPacket.getData(), 0, datagramPacket.getLength())
                                        + " from " + datagramPacket.getAddress().getHostAddress() + ":" + datagramPacket.getPort();
                                Toast.makeText(MainActivity.this, "单播接受=" + receiveMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    /**
     * 组播发送
     */
    public void onMulticastSend(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //IP组
                    InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);
                    //组播监听端口
                    MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
                    multicastSocket.setTimeToLive(1);
                    //加入该组
                    multicastSocket.joinGroup(inetAddress);
                    //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
                    byte[] data = content.getText().toString().getBytes();
                    DatagramPacket dataPacket = new DatagramPacket(data, data.length, inetAddress, MULTICAST_PORT);
                    multicastSocket.send(dataPacket);
                    multicastSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 组播接受
     */
    private void onMulticastSendReceive() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);
                    MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
                    multicastSocket.joinGroup(inetAddress);
                    byte buf[] = new byte[1024];
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, inetAddress, MULTICAST_PORT);

                    while (isRuning) {
                        multicastSocket.receive(datagramPacket);
                        final String message = new String(buf, 0, datagramPacket.getLength());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "组播接受=" + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }


        }).start();
    }
}
