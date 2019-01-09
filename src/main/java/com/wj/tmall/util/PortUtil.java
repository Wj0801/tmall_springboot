package com.wj.tmall.util;

import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JOptionPane;

//判断某个端口是否启动,忘记启动redis服务器导致系统无法运行
public class PortUtil {

    public static boolean testPort(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return false;
        } catch (java.net.BindException e) {
            return true;
        } catch (IOException e) {
            return true;
        }
    }

    public static void checkPort(int port, String server, boolean shutdown) {
        if(!testPort(port)) {
            if(shutdown) {
                String message =String.format("在端口 %d 未检查得到 %s 启动,请打开服务后重新运行~",port,server);
                JOptionPane.showMessageDialog(null, message);
                System.exit(1);
            }
            else {
                String message =String.format("在端口 %d 未检查得到 %s 启动%n,是否继续?",port,server);
                if(JOptionPane.OK_OPTION !=     JOptionPane.showConfirmDialog(null, message))
                    System.exit(1);

            }
        }
    }

}