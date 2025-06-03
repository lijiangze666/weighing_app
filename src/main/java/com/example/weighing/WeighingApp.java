package com.example.weighing;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WeighingApp extends JFrame {

    private JTextArea weightDisplay;
    private JComboBox<String> portList, baudRateList, dataBitsList, stopBitsList, parityList;
    private JButton connectBtn;

    private JComboBox<String> rfidPortList;
    private JButton rfidConnectBtn;
    private JTextField rfidField;

    private SerialPort serialPort;
    private SerialPort rfidPort;

    private Font digitalFont;

    public WeighingApp() {
        setTitle("地磅称重系统");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadDigitalFont();
        initUI();
    }

    private void loadDigitalFont() {
        try (InputStream is = getClass().getResourceAsStream("/fonts/digital.ttf")) {
            if (is != null) {
                digitalFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, 72f);
            } else {
                System.out.println("字体文件未找到！");
                digitalFont = new Font("Monospaced", Font.BOLD, 72);
            }
        } catch (Exception e) {
            e.printStackTrace();
            digitalFont = new Font("Monospaced", Font.BOLD, 72);
        }
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        portList = new JComboBox<>();
        baudRateList = new JComboBox<>(new String[]{"9600", "19200", "38400", "57600", "115200"});
        dataBitsList = new JComboBox<>(new String[]{"5", "6", "7", "8"});
        stopBitsList = new JComboBox<>(new String[]{"1", "1.5", "2"});
        parityList = new JComboBox<>(new String[]{"None", "Odd", "Even", "Mark", "Space"});
        connectBtn = new JButton("连接串口");

        baudRateList.setSelectedItem("9600");
        dataBitsList.setSelectedItem("8");
        stopBitsList.setSelectedItem("1");
        parityList.setSelectedItem("None");

        rfidPortList = new JComboBox<>();
        rfidConnectBtn = new JButton("连接RFID");

        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portList.addItem(port.getSystemPortName());
            rfidPortList.addItem(port.getSystemPortName());
        }

        topPanel.add(new JLabel("串口:"));
        topPanel.add(portList);
        topPanel.add(new JLabel("波特率:"));
        topPanel.add(baudRateList);
        topPanel.add(new JLabel("数据位:"));
        topPanel.add(dataBitsList);
        topPanel.add(new JLabel("停止位:"));
        topPanel.add(stopBitsList);
        topPanel.add(new JLabel("校验位:"));
        topPanel.add(parityList);
        topPanel.add(connectBtn);

        topPanel.add(new JLabel("RFID串口:"));
        topPanel.add(rfidPortList);
        topPanel.add(rfidConnectBtn);

        weightDisplay = new JTextArea();
        weightDisplay.setFont(digitalFont);
        weightDisplay.setEditable(false);
        weightDisplay.setBackground(Color.BLACK);
        weightDisplay.setForeground(Color.RED);
        weightDisplay.setLineWrap(true);
        weightDisplay.setWrapStyleWord(true);
        weightDisplay.setMargin(new Insets(30, 30, 30, 30));

        rfidField = new JTextField();
        rfidField.setEditable(false);
        rfidField.setFont(new Font("Monospaced", Font.BOLD, 24));
        rfidField.setForeground(Color.BLUE);

        JPanel rfidPanel = new JPanel(new BorderLayout());
        rfidPanel.add(new JLabel("当前卡号："), BorderLayout.WEST);
        rfidPanel.add(rfidField, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(weightDisplay), BorderLayout.CENTER);
        add(rfidPanel, BorderLayout.SOUTH);

        connectBtn.addActionListener(this::toggleWeightConnection);
        rfidConnectBtn.addActionListener(this::toggleRfidConnection);
    }

    private void toggleWeightConnection(ActionEvent e) {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            connectBtn.setText("连接串口");
            weightDisplay.setText("0");
            JOptionPane.showMessageDialog(this, "串口已断开");
            return;
        }

        String portName = (String) portList.getSelectedItem();
        int baudRate = Integer.parseInt((String) baudRateList.getSelectedItem());
        int dataBits = Integer.parseInt((String) dataBitsList.getSelectedItem());
        int stopBits = switch (stopBitsList.getSelectedItem().toString()) {
            case "1.5" -> SerialPort.ONE_POINT_FIVE_STOP_BITS;
            case "2" -> SerialPort.TWO_STOP_BITS;
            default -> SerialPort.ONE_STOP_BIT;
        };
        int parity = switch (parityList.getSelectedItem().toString()) {
            case "Odd" -> SerialPort.ODD_PARITY;
            case "Even" -> SerialPort.EVEN_PARITY;
            case "Mark" -> SerialPort.MARK_PARITY;
            case "Space" -> SerialPort.SPACE_PARITY;
            default -> SerialPort.NO_PARITY;
        };

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            connectBtn.setText("断开串口");
            JOptionPane.showMessageDialog(this, "串口已连接：" + portName);
            new Thread(this::readWeightData).start();
        } else {
            JOptionPane.showMessageDialog(this, "无法打开串口：" + portName, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void readWeightData() {
        byte[] buffer = new byte[1024];
        while (serialPort != null && serialPort.isOpen()) {
            int numRead = serialPort.readBytes(buffer, buffer.length);
            if (numRead > 0) {
                String raw = new String(buffer, 0, numRead, StandardCharsets.UTF_8).trim();
                System.out.println("原始串口数据: " + raw);

                try {
                    if (raw.matches("\\+[0-9]{8}[A-Z]?")) {
                        String numericPart = raw.substring(1, 9);
                        int rawValue = Integer.parseInt(numericPart);
                        int kg = rawValue / 100;

                        String display = String.valueOf(kg);
                        SwingUtilities.invokeLater(() -> weightDisplay.setText(display));
                    }
                } catch (Exception ex) {
                    System.err.println("解析失败: " + raw);
                }
            }
        }
    }

    private void toggleRfidConnection(ActionEvent e) {
        if (rfidPort != null && rfidPort.isOpen()) {
            rfidPort.closePort();
            rfidConnectBtn.setText("连接RFID");
            rfidField.setText("");
            JOptionPane.showMessageDialog(this, "RFID串口已断开");
            return;
        }

        String portName = (String) rfidPortList.getSelectedItem();
        rfidPort = SerialPort.getCommPort(portName);
        rfidPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        rfidPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (rfidPort.openPort()) {
            rfidConnectBtn.setText("断开RFID");
            JOptionPane.showMessageDialog(this, "RFID已连接：" + portName);
            new Thread(this::readRfidData).start();
        } else {
            JOptionPane.showMessageDialog(this, "无法打开RFID串口：" + portName, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void readRfidData() {
        byte[] buffer = new byte[64];
        StringBuilder sb = new StringBuilder();

        while (rfidPort != null && rfidPort.isOpen()) {
            int numRead = rfidPort.readBytes(buffer, buffer.length);
            if (numRead > 0) {
                String raw = new String(buffer, 0, numRead, StandardCharsets.UTF_8);
                for (char c : raw.toCharArray()) {
                    if (c == '\n' || c == '\r') {
                        String cardId = sb.toString().trim();
                        if (!cardId.isEmpty()) {
                            SwingUtilities.invokeLater(() -> rfidField.setText(cardId));
                            System.out.println("RFID卡号：" + cardId);
                        }
                        sb.setLength(0);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
