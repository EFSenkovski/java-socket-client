package br.com.brsocketclient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class User {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public User(String host, int port, String username) {
        try {
            this.socket = new Socket(host, port);
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void awaitForSendingMessages() {
        try {
            writer.write(username);
            writer.newLine();
            writer.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                writer.write(username + ": " + messageToSend);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void awaitForReceivingMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromServer;

                while (socket.isConnected()) {
                    try {
                        messageFromServer = reader.readLine();
                        System.out.println(messageFromServer);
                    } catch (IOException e) {
                        closeEverything(socket, reader, writer);
                    }

                }
            }
        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            System.out.println("Closed everything on client: " + this.username);
        } catch (IOException e) {
            System.out.println(e.getCause().toString());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter you username:");
        String username = scanner.nextLine();
        var user = new User("localhost", 1234, username);
        user.awaitForReceivingMessages();
        user.awaitForSendingMessages();
    }
}
