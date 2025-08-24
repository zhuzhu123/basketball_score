package com.basketball.display;

/**
 * @author zhucha
 * @Title TODO
 * @Description TODO
 * @Date 2025/8/23
 */

import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothServer {
    public static void main(String[] args) throws IOException {
        // 1. 获取本地蓝牙设备
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("PC Bluetooth Address: " + localDevice.getBluetoothAddress());
        System.out.println("PC Bluetooth Name: " + localDevice.getFriendlyName());

        // 2. 创建 UUID（唯一标识服务）
        UUID uuid = new UUID("1101", true); // 标准串口服务 UUID

        // 3. 创建服务 URL
        String url = "btspp://localhost:" + uuid + ";name=BluetoothServer";

        // 4. 启动服务端 Socket
        StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(url);
        System.out.println("Server started. Waiting for client...");

        // 5. 等待客户端连接
        StreamConnection connection = notifier.acceptAndOpen();
        System.out.println("Client connected!");

        // 6. 获取输入输出流
        InputStream input = connection.openInputStream();
        OutputStream output = connection.openOutputStream();

        // 7. 接收数据
        byte[] buffer = new byte[1024];
        int bytesRead = input.read(buffer);
        String received = new String(buffer, 0, bytesRead);
        System.out.println("Received: " + received);

        // 8. 发送响应
        output.write("Hello from PC!".getBytes());

        // 9. 关闭连接
        connection.close();
        notifier.close();
    }
}
