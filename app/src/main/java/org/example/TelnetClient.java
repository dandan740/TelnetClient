package org.example;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.net.Socket;

public class TelnetClient {
    private final String host;
    private final int port;

    public TelnetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException, InterruptedException {
        try (Socket socket = new Socket(host, port)) {
            Thread readerThread = new Thread(() -> {
                readFromServer(socket);
            });
            Thread writerThread = new Thread(() -> {
                writeToServer(socket);
            });

            readerThread.start();
            writerThread.start();

            writerThread.join();
        }
    }

    private static void readFromServer(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            socket.getInputStream(), StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int charactersRead;
            while ((charactersRead = reader.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, charactersRead));
                System.out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToServer(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in, StandardCharsets.UTF_8));
            Writer writer = new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8);

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write("\r\n");
                writer.flush();
                if ("/QUIT".equals(line)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
