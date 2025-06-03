package com.example.weighing.utils;

import com.example.weighing.service.MwicLibrary;

public class RFIDReader {
    public static void main(String[] args) {
        int port = 100; // 根据实际情况设置端口号
        int baud = 9600; // 波特率
        int icdev = MwicLibrary.INSTANCE.rf_init(port, baud);
        if (icdev >= 0) {
            System.out.println("初始化成功，设备号：" + icdev);
            // 进行读写操作
            MwicLibrary.INSTANCE.rf_exit(icdev);
        } else {
            System.out.println("初始化失败");
        }
    }
}
