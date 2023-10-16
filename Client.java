import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;


public class Client {
    private Socket socket = null;
    private DataInputStream inputServer = null;
    private DataInputStream inputConsole = null;
    private DataOutputStream out = null;

    private JTextArea messageArea;
    private JTextField inputField;

    public Client(String address, int port, String name) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected!");

            inputServer = new DataInputStream(socket.getInputStream());
            inputConsole = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());

            name = "/Connected: " + name;
            out.writeUTF(name);

            JFrame frame = new JFrame("Chat Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            messageArea = new JTextArea();
            messageArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(messageArea);
            inputField = new JTextField();
            JButton sendButton = new JButton("Send");

            sendButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });

            inputField.addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent e){
                    if(e.getKeyCode() == KeyEvent.VK_ENTER){
                        sendMessage();
                    }
                }
            });

            Panel p = new Panel();
            p.setLayout(new BorderLayout());
            p.add(inputField,BorderLayout.CENTER);
            p.add(sendButton,BorderLayout.EAST);
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(p, BorderLayout.SOUTH);

            frame.setSize(400, 300);
            frame.setVisible(true);

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = inputServer.readUTF();
                        System.out.println(serverMessage);

                        if ("/quit".equals(serverMessage)) {
                            System.out.println("Disconnected from server");
                            break;
                        } else {
                            appendMessage(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            String clientMessage;
            while (true) {
                try {
                    clientMessage = inputConsole.readLine();
                    out.writeUTF(clientMessage);
                     appendMessage("You: " + clientMessage);

                    if ("/quit".equals(clientMessage)) {
                        System.out.println("Disconnected from server");
                        break;
                    } else if (clientMessage.startsWith("/rename")) {
                        String[] parts = clientMessage.split(" ");
                        if (parts.length >= 2) {
                            name = parts[1];
                            System.out.println("You have been renamed to: " + name);
                        } else {
                            System.out.println("Invalid /rename command. Usage: /rename <newName>");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        } finally {
            try {
                inputServer.close();
                inputConsole.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        String clientMessage = inputField.getText();
        try {
            out.writeUTF(clientMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputField.setText("");
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
        });
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String name = sc.nextLine();
        Client client = new Client("localhost", 5000, name);
    }
}

