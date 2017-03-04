import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.swing.*;

public class Client extends javax.swing.JFrame implements Runnable {

    String line;
    String gotLine, remoteIP, remotePort;
    Thread th = null;
    InetAddress IPAddress;
    DatagramSocket welcomeSocket = null;
    ArrayList<String> peer = new ArrayList<>();
    Hashtable<String, String> peerAddress = new Hashtable<>();
    String user, friend = "friend";
    String serverSocket;
    StringTokenizer strTok;
    /**
     * Creates new form Client
     * @throws java.lang.Exception
     */
    	public Client() throws Exception
    	{

        	int clientPort = (int)(Math.random()*9000)+1000;
        	welcomeSocket = new DatagramSocket(clientPort);
        	String myIP = null;// = InetAddress.getLocalHost().getHostAddress();//toString().split("/")[1];
        	JTextField serverA = new JTextField();
        	JTextField username = new JTextField();
        	JTextField password = new JPasswordField();

        	Object[] message =
        	{
            		"Server Address:", serverA,
            		"Username:", username,
            		"Password:", password
        	};


        	int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        	if (option == JOptionPane.OK_OPTION)
        	{
            		//System.out.println(serverA.toString());
            		if(!Pattern.matches("^\\s*(.*?):(\\d+)\\s*$", serverA.getText()))
            		{
                		System.out.println("Invalid Server!");
                		System.exit(-2);
            		}
            		System.out.println("Server Ok...");
            		// authenticate from server
            		serverSocket = serverA.getText();
            		user = username.getText();

            		byte[] sendData = new byte[1024];
            		String sendTextData = "AUTH " + user + " " + password.getText() + "\n";
            		sendData = sendTextData.getBytes();
                	DatagramPacket sendPacket = null;
            		try
            		{
                		sendPacket = new DatagramPacket(sendData, sendData.length,
                			InetAddress.getByName(serverSocket.split(":")[0]),
                			Integer.parseInt(serverSocket.split(":")[1]));
            		}
            		catch (UnknownHostException ex)
            		{
                		System.out.println("1"+ex);
            		}
            		try
            		{
                		welcomeSocket.send(sendPacket);
            		}
            		catch (Exception ex)
            		{
                    		//Exception
                    		System.out.println("2"+ex);
            		}

        		System.out.println("trying auth ...");
        		int count = 0;
        		while(count < 2)
        		{
        	    		DatagramPacket receivePacket;
            			do
            			{
                			byte[] reciveData = new byte[1024];
                			receivePacket = new DatagramPacket(reciveData, reciveData.length);
                			welcomeSocket.receive(receivePacket);
                			gotLine = new String(receivePacket.getData());
            			}while(gotLine == null);

            			strTok = new StringTokenizer(gotLine);
            			String type = strTok.nextToken();

            			if(type.equals("YIP"))
            			{
                			myIP = strTok.nextToken();
                			System.out.println(myIP);
                			count ++;
            			}

            		// get peerList
	            		else if(type.equals("PLIST"))
	            		{
	                		System.out.println("getting peerlist ...");
	                		String peerUname, peerSocket;
	                		while(strTok.hasMoreTokens() && strTok.countTokens() > 1)
	                		{
	                    			//System.out.println("+++"  + gotLine);
	                   			peerUname = strTok.nextToken();
	                   			peerSocket = strTok.nextToken();
	                   			peerAddress.put(peerUname, peerSocket);
	                   			//System.out.println("*"+peerUname);
	               			}
	                		count++;
	            		}
	            		else if(type.equals("BAD"))
	            		{
	                		System.out.println("Authentication Error");
	                		System.exit(-10);
	            		}
        		}
        	}
        		else
        		{
            			System.out.println("Login canceled");
            			System.exit(-1);
        		}

        		initComponents();
        		System.out.println("loading ui ...");
        		label2.setText("User: " + user + " connected on socket: " + myIP + ":" + clientPort);

        		int i = 0;
        		for(String s: peerAddress.keySet()){
            		peer.add(s);
        	}
        	friendList.setModel(new javax.swing.AbstractListModel<String>()
        	{
            		public int getSize() { return peer.size(); }
            		public String getElementAt(int i) { return peer.get(i); }
        	});
        		jScrollPane3.setViewportView(friendList);
        	System.out.println("creating friend list ...");

        	//System.out.print("abcdef");
        	th = new Thread(this);
        	th.setDaemon(true);
        	th.start();
    	}


    @Override
    public void run(){
        DatagramPacket receivePacket;
        while(true){
            do{
                byte[] reciveData = new byte[1024];
                receivePacket = new DatagramPacket(reciveData, reciveData.length);
                try {
                    welcomeSocket.receive(receivePacket);
                } catch (IOException ex) {
                   System.out.println("3"+ex);
                }
                gotLine = new String(receivePacket.getData());
            }while(gotLine == null);

        String matchSocket = receivePacket.getSocketAddress().toString();
        strTok = new StringTokenizer(gotLine);
        String type = strTok.nextToken();

        if(type.equals("ADD")){
            System.out.println("New peer added to netork ...");
            String uname = strTok.nextToken();
            String sock = strTok.nextToken();
            peer.add(uname);
            peerAddress.put(uname, sock);
            friendList.setModel(new javax.swing.AbstractListModel<String>() {
                public int getSize() { return peer.size(); }
                public String getElementAt(int i) { return peer.get(i); }
            });
            jScrollPane3.setViewportView(friendList);
        }

        else if(type.equals("MSG")) {
            display.append(gotLine.substring(4));
        }
        else if(type.equals("DEL")){
            System.out.println("Deleting a peer ...");
            String uname = strTok.nextToken();
            String sockt = strTok.nextToken();
            peer.remove(uname);
            friendList.setModel(new javax.swing.AbstractListModel<String>() {
                public int getSize() { return peer.size(); }
                public String getElementAt(int i) { return peer.get(i); }
            });
            jScrollPane3.setViewportView(friendList);
        }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        message = new javax.swing.JTextField();
        sendB = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        display = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        friendList = new javax.swing.JList<>();
        label1 = new javax.swing.JLabel();
        label2 = new javax.swing.JLabel();
        socket = new javax.swing.JTextField();
        connect = new javax.swing.JButton();
        label3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        message.setText("Start Typing ...");
        message.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                messageMouseClicked(evt);
            }
        });
        message.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageActionPerformed(evt);
            }
        });

        sendB.setText("Send");
        sendB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sendBMouseClicked(evt);
            }
        });

        display.setEditable(false);
        display.setColumns(20);
        display.setRows(5);
        jScrollPane1.setViewportView(display);

        friendList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        friendList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                friendListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(friendList);

        label1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label1.setText("List of Peers");

        label2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label2.setText("Connected on Socket: ");

        socket.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                socketMouseClicked(evt);
            }
        });

        connect.setText("Connect");
        connect.setToolTipText("");
        connect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                connectMouseClicked(evt);
            }
        });

        label3.setText("Connect to Socket:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(message)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sendB))
                    .addComponent(label2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(socket, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connect, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label1)
                    .addComponent(label2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(socket, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(connect)
                            .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(message))
                            .addComponent(sendB, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void messageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messageMouseClicked
        // TODO add your handling code here:
        message.setText("");
    }//GEN-LAST:event_messageMouseClicked

    private void sendBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sendBMouseClicked
        // TODO add your handling code here:
        String sendText = "MSG " + user + ": " + message.getText() + "\n";
        if(sendText == null){
            message.setText("Enter a message to send ....");
        }else{
            try {
                display.append(sendText.substring(4));
                byte[] sendData = new byte[1024];
                sendData = sendText.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
                try {
                    welcomeSocket.send(sendPacket);
                } catch (Exception ex) {
                   System.out.println("4"+ex);
                }
            } catch (Exception ex) {
               System.out.println("5"+ex);
            }
        }

    }//GEN-LAST:event_sendBMouseClicked

    private void messageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageActionPerformed
        // TODO add your handling code here:
        String sendText = "MSG " + user + ": " + message.getText() + "\n";
        if(sendText == null){
            message.setText("Enter a message to send ....");
        }else{
            try {
                display.append(sendText.substring(4));
                message.setText("");
                byte[] sendData = new byte[1024];
                sendData = sendText.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
                try {
                    welcomeSocket.send(sendPacket);
                } catch (Exception ex) {
                   System.out.println("6"+ex);
                }
            } catch (Exception ex) {
               System.out.println("7"+ex);
            }
        }
    }//GEN-LAST:event_messageActionPerformed

    private void connectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectMouseClicked
        // TODO add your handling code here:
        String Rsocket = socket.getText();
        if(!Pattern.matches("^\\s*(.*?):(\\d+)\\s*$", Rsocket)){
            socket.setText("Enter a valid socket or select a peer");
        }else{
            remoteIP = Rsocket.split(":")[0];
            remotePort = Rsocket.split(":")[1];
        }
    }//GEN-LAST:event_connectMouseClicked

    private void socketMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_socketMouseClicked
        // TODO add your handling code here:
        socket.setText("");
    }//GEN-LAST:event_socketMouseClicked

    private void friendListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_friendListMouseClicked
        // TODO add your handling code here:
        int index = friendList.getSelectedIndex();
        String SocketVal = peerAddress.get(peer.get(index));
        socket.setText(SocketVal);
    }//GEN-LAST:event_friendListMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        byte[] sendData = new byte[1024];
        String sendTextData = "CLOSE " + user + " " + "\n";
        sendData = sendTextData.getBytes();
                DatagramPacket sendPacket = null;
        try{
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(serverSocket.split(":")[0]), Integer.parseInt(serverSocket.split(":")[1]));
        } catch (UnknownHostException ex) {
            System.out.println(ex);
        }
                try {
                    welcomeSocket.send(sendPacket);
                } catch (Exception ex) {
                    //Exception
                    System.out.println(ex);
                }
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]){
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Client().setVisible(true);
                } catch (Exception ex) {
                    System.out.println("8"+ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connect;
    private javax.swing.JTextArea display;
    private javax.swing.JList<String> friendList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JTextField message;
    private javax.swing.JButton sendB;
    private javax.swing.JTextField socket;
    // End of variables declaration//GEN-END:variables
}
