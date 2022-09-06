package rindong.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        new ClientFrame();
    }

    public static class ClientFrame extends JFrame
    {
        //在聊天室中展示的名字
        public String userName="";
        //服务器套接字
        private Socket server;
        //客户端输出流
        private PrintWriter serverOutputStream;
        //客户端输入流
        private BufferedReader serverInputStream;
        //显示信息的地方
        private JTextArea conMessage=new JTextArea();
        private JScrollPane panel=new JScrollPane(conMessage);
        private JTextField inputArea = new JTextField();

        public ClientFrame()
        {
            setTitle("socket连接端");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setBounds(500,400,800,600);

            addbutton();

            setVisible(true);

            new ConnectDialog(this);
        }

        @Override
        public void dispose() {
            if (server != null)
            {
                serverOutputStream.println("exit");
                try {
                    server.close();
                    serverInputStream.close();
                    serverOutputStream.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }

            super.dispose();
        }

        private void addbutton()
        {
            conMessage.setEditable(false);
            conMessage.setFont(new Font(null,Font.PLAIN,30));
            getContentPane().add(panel,BorderLayout.CENTER);
            inputArea.setFont(new Font(null,Font.PLAIN,30));
            //添加文本输入框监听事件
            inputArea.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //如果没连接上服务器
                    if (server == null)
                    {
                        int value = JOptionPane.showConfirmDialog(ClientFrame.this,"服务器未能连接成功，是否重新连接？","提示",JOptionPane.YES_NO_OPTION);
                        if (value == 0)
                        {
                            new ConnectDialog(ClientFrame.this);
                        }
                    }
                    //否则向服务器发送信息
                    else
                    {
                        serverOutputStream.println(userName + "：" + inputArea.getText());
                        conMessage.append("我：" +  inputArea.getText() + "\n");
                        inputArea.setText("");
                    }

                }
            });
            getContentPane().add(inputArea,"South");
        }

        //调用此方法向服务器发起链接
        public void connect(String name, String ip,String port)
        {
            this.userName = name;
            try {
                server = new Socket(ip,Integer.parseInt(port));
                serverOutputStream = new PrintWriter(server.getOutputStream(),true);
            }
            catch (UnknownHostException ex1)
            {
                JOptionPane.showMessageDialog(this,"连接错误！" + ex1.getMessage());
                ex1.printStackTrace();
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this,"连接错误！" + ex.getMessage());
                ex.printStackTrace();
            }

            if (server!=null)
            {
                try {
                    serverInputStream = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    new Thread()
                    {
                        @Override
                        public void run() {
                            //持续监听服务端消息
                            while (true)
                            {
                                try {
                                    String message = serverInputStream.readLine();
                                    conMessage.append(message + "\n");
                                }
                                catch (IOException ex)
                                {
                                    JOptionPane.showMessageDialog(ClientFrame.this,"读取信息错误！" + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }.start();
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(this,"创建输入流错误！" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    //链接服务器窗口
    private static class ConnectDialog extends JDialog
    {
        //通过这个对象调用链接方法
        private ClientFrame main;
        private JLabel nameLabel=new JLabel("昵称:",SwingConstants.CENTER);
        private JTextField nameTextField=new JTextField();
        private JLabel serveripAddress=new JLabel("主机:",SwingConstants.CENTER);
        private JTextField AddressTextfield=new JTextField();
        private JLabel AddressPort=new JLabel("端口:",SwingConstants.CENTER);
        private JTextField portTextfield=new JTextField();

        private JButton JbuttonConnect = new JButton("连接");
        private JButton jbuttonCancel = new JButton("取消");

        public ConnectDialog(JFrame frame)
        {
            super(frame,"连接窗口",true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.main = (ClientFrame) frame;
            setBounds(700,600,400,350);
            setLayout(null);
            addbutton();
            addListener();
            setVisible(true);
        }

        private void addbutton()
        {
            nameLabel.setBounds(20,30,100,40);
            nameLabel.setFont(new Font(null,Font.PLAIN,30));
            add(nameLabel);
            nameTextField.setBounds(110,30,230,50);
            nameTextField.setFont(new Font(null,Font.PLAIN,27));
            add(nameTextField);
            serveripAddress.setBounds(20,100,100,40);
            serveripAddress.setFont(new Font(null,Font.PLAIN,30));
            add(serveripAddress);
            AddressTextfield.setBounds(110,100,230,50);
            AddressTextfield.setFont(new Font(null,Font.PLAIN,27));
            add(AddressTextfield);
            AddressPort.setBounds(20,170,100,40);
            AddressPort.setFont(new Font(null,Font.PLAIN,30));
            add(AddressPort);
            portTextfield.setBounds(110,170,230,50);
            portTextfield.setFont(new Font(null,Font.PLAIN,27));
            add(portTextfield);
            JbuttonConnect.setBounds(80,240,100,40);
            JbuttonConnect.setFont(new Font(null,Font.PLAIN,30));
            add(JbuttonConnect);

            jbuttonCancel.setBounds(215,240,100,40);
            jbuttonCancel.setFont(new Font(null,Font.PLAIN,30));
            add(jbuttonCancel);
        }

        private void addListener()
        {
            //简单的进行一些数据检验
            JbuttonConnect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nameTextField.getText().length() > 8)
                    {
                        JOptionPane.showMessageDialog(ConnectDialog.this,"你的名字太长了！");
                        return;
                    }
                    if (AddressTextfield.getText().length() > 15)
                    {
                        JOptionPane.showMessageDialog(ConnectDialog.this,"ip地址不合法！");
                        return;
                    }
                    if (portTextfield.getText().length() > 5)
                    {
                        JOptionPane.showMessageDialog(ConnectDialog.this,"端口数值不合法！");
                        return;
                    }
                    main.connect(nameTextField.getText(),AddressTextfield.getText(),portTextfield.getText());
                    dispose();
                }
            });

            jbuttonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }

        @Override
        public void dispose() {

            super.dispose();
        }
    }
}
