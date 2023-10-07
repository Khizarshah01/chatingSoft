import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MultiThreadedServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();

    public MultiThreadedServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void handleCommand(String command, ClientHandler clientHandler) {
        if (command.startsWith("/rename")) {
            String[] parts = command.split(" ");
            if (parts.length >= 2) {
                String newName = parts[1];
                clientHandler.rename(newName);
                broadcastMessage(clientHandler.getName() + " has been renamed to " + newName, null);
            } else {
                clientHandler.sendMessage("Invalid /rename command. Usage: /rename <newName>");
            }
        } else if (command.equals("/quit")) {
            clients.remove(clientHandler);
            broadcastMessage(clientHandler.getName() + " has quit.", null);
            clientHandler.close();
        }
        else if(command.startsWith("/Connected:")){
          String[] parts = command.split(" ");
             if (parts.length >= 2) {
          String newName = parts[1];
          clientHandler.rename(newName);
          broadcastMessage(newName + " Join the group.",null);
        } else {
                clientHandler.sendMessage("Invalid Name");
            }
        }
        // Add more commands as needed
    }

    public static void main(String[] args) {
        MultiThreadedServer server = new MultiThreadedServer(5000);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MultiThreadedServer multiThreadedServer;
    private String name;

    public ClientHandler(Socket socket, MultiThreadedServer server) {
        this.socket = socket;
        this.multiThreadedServer = server;
        try {
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            name = "Client"; // Default name
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String clientMessage;
        try {
            while (true) {
                clientMessage = in.readUTF();
                System.out.println("Client " + name + ": " + clientMessage);

                if (clientMessage.startsWith("/")) {
                    multiThreadedServer.handleCommand(clientMessage, this);
                } else {
                    multiThreadedServer.broadcastMessage(name + ": " + clientMessage, this);
                }

                if ("/quit".equals(clientMessage)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }
}

