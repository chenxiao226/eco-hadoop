package org.hit.monitor.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @ClassName TriggerScoketClient
 * @Description 向服务器发送数据

 */
public class AlertScoketClient {

//	 private static String SERVERIP = "127.0.0.1";
//	 private static int PORT =80;

	public String sendMessage(String message) throws IOException {
		System.out.println("连接服务器: " + CONFIG.ALERT_SERVER_IP + ":" + CONFIG.ALERT_SERVER_PORT);
		Socket ClientSocket = null;
		String SERVERIP = CONFIG.ALERT_SERVER_IP;
		int PORT = Integer.valueOf(CONFIG.ALERT_SERVER_PORT);
		ClientSocket = new Socket(SERVERIP, PORT);
		PrintWriter out = new PrintWriter(ClientSocket.getOutputStream(), true);
		out.print(message);// 发送消息
		out.flush();
		// outToServer.flush();
		BufferedReader inFromServer = new BufferedReader(
				new InputStreamReader(ClientSocket.getInputStream()));
		String getFromServer = inFromServer.readLine();// 接受消息
		// System.out.println("FROM SERVER:" + getFromServer);
		ClientSocket.close();
		return getFromServer;
	}
}
