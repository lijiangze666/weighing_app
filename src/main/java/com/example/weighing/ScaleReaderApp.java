package com.example.weighing;

import javax.swing.*;
import com.fazecast.jSerialComm.SerialPort;
import java.awt.*;
import java.awt.event.*;

public class ScaleReaderApp {

    private JFrame frame;
    private JTextArea textArea;
    private JLabel weightLabel;
    private SerialPort comPort;
    private static final double CONVERSION_FACTOR = 0.1;  // 假设转换因子，具体的因子需要根据实际设备校准
    private double lastWeight = -1.0;  // 用于记录上次显示的重量数据
    private boolean isUpdating = false;  // 用来标记是否正在更新UI，避免频繁更新

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScaleReaderApp().createAndShowGUI());
    }

    // 创建 GUI 窗口
    private void createAndShowGUI() {
        frame = new JFrame("地磅称重系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        // 创建布局
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        panel.setLayout(new BorderLayout());

        // 创建文本框显示串口数据
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 创建显示当前重量的标签
        weightLabel = new JLabel("当前重量: 0.0 Kg");
        weightLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        panel.add(weightLabel, BorderLayout.NORTH);

        // 创建连接和断开按钮
        JPanel controlPanel = new JPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);

        JButton connectButton = new JButton("连接串口");
        JButton disconnectButton = new JButton("断开串口");

        controlPanel.add(connectButton);
        controlPanel.add(disconnectButton);

        // 设置连接按钮动作
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToSerialPort("COM3");
            }
        });

        // 设置断开按钮动作
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectSerialPort();
            }
        });

        frame.setVisible(true);
    }

    // 连接串口并开始读取数据
    private void connectToSerialPort(String portName) {
        // 获取串口
        comPort = SerialPort.getCommPort(portName);
        comPort.setBaudRate(9600);  // 设置波特率
        comPort.setNumDataBits(8);  // 设置数据位
        comPort.setNumStopBits(1);  // 设置停止位
        comPort.setParity(SerialPort.NO_PARITY);  // 设置校验位

        if (comPort.openPort()) {
            textArea.append("串口 " + portName + " 已连接\n");

            // 开始读取串口数据
            Thread readThread = new Thread(() -> {
                while (comPort.isOpen()) {
                    byte[] readBuffer = new byte[1024];
                    int numBytes = comPort.readBytes(readBuffer, readBuffer.length);
                    if (numBytes > 0) {
                        String data = new String(readBuffer, 0, numBytes).trim();
                        textArea.append("接收到数据: " + data + "\n");  // 打印接收到的数据
                        // 只保留数字和小数点
                        data = data.replaceAll("[^0-9\\.]", "");

                        try {
                            if (!data.isEmpty()) {
                                double rawValue = Double.parseDouble(data);
                                // 将数据转换为整数，不带小数点
                                long weightInKg = Math.round(rawValue * CONVERSION_FACTOR);  // 使用 Math.round() 转换为整数

                                // 只有当数据变化时才更新界面显示
                                if (lastWeight != weightInKg && !isUpdating) {
                                    lastWeight = weightInKg;  // 更新上次显示的重量
                                    isUpdating = true;  // 标记正在更新UI
                                    SwingUtilities.invokeLater(() -> {
                                        updateWeightLabel(weightInKg);  // 更新界面上的重量
                                        isUpdating = false;  // 更新完成，重置标志
                                    });
                                }
                            }
                        } catch (NumberFormatException ex) {
                            textArea.append("读取数据格式错误: " + data + "\n");
                        }
                    }
                }
            });
            readThread.start();
        } else {
            textArea.append("无法连接串口 " + portName + "\n");
        }
    }

    // 断开串口连接
    private void disconnectSerialPort() {
        if (comPort != null && comPort.isOpen()) {
            comPort.closePort();
            textArea.append("串口已断开\n");
        }
    }

    // 更新显示的重量数据
    private void updateWeightLabel(long weightInKg) {
        weightLabel.setText("当前重量: " + weightInKg + " kg");  // 显示整数的重量
    }
}
