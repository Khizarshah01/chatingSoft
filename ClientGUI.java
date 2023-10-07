package clientClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.DataOutputStream;



public class ClientGUI {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private DataOutputStream out;

    public ClientGUI(DataOutputStream out) {
        this.out = out;

        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(messageArea);
        inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.add(sendButton, BorderLayout.EAST);

        frame.setSize(400, 300);
        frame.setVisible(true);
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

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use this main method for testing the GUI independently
            ClientGUI gui = new ClientGUI(null);
        });
    }
}

