package com.example.yearprojectfinal;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

// This class is responsible for sending and receiving the data to and from the server
// asynchronously
public class SocketTask extends AsyncTask<String, Void, String>
{
    private final String className = this.getClass().getSimpleName();

    private final static String IP_ADDRESS = "192.168.1.218";
    private final static int PORT = 50001;
    private final static int SOCKET_CONNECT_TIMEOUT = 20000;
    private final static int SOCKET_RECEIVE_TIMEOUT = 3000;

    private Socket socket;
    private String sendingStr = "";
    private String receivingStr = "";

    // Initialize with the string to send
    public SocketTask(String str1) {
        this.sendingStr = str1;
    }

    // This function calls the send and receive data (interacts with the server)
    @Override
    protected String doInBackground(String... arrStrings)
    {
        try
        {
            this.socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);
            this.socket.connect(socketAddress, SOCKET_CONNECT_TIMEOUT); // Set timeout to 15 seconds
            send(this.sendingStr);
            receive();
            this.socket.close();
        }

        catch (IOException e)
        {
            Log.e("Exception", "Connection timeout: The server is not responding.");
            return "";
        }

        catch (Exception e)
        {
            Log.e("Exception", e.toString());
            return "";
        }

        return this.receivingStr;
    }

    // This function sends the data to the server
    private void send(String sendingStr)
    {
        try
        {
            OutputStreamWriter writer = new
                    OutputStreamWriter(this.socket.getOutputStream(),
                    StandardCharsets.UTF_8); // outputStreamWriter creating

            writer.write(sendingStr);
            writer.flush();

            Log.d("Sent to server", sendingStr);
        }
        catch (Exception e)
        {
            Log.e("Exception", e.toString());
        }
    }

    // This function receives the data from the server
    private void receive() throws IOException {
        ByteArrayOutputStream baos = null;
        InputStream inputStream = null;
        socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);

        try
        {
            inputStream = socket.getInputStream();
            baos = new ByteArrayOutputStream();

            int c;
            while ((c = inputStream.read()) != -1) {
                baos.write((char) c);
            }

            // in case socket didnt timeout.
            byte[] bytes = baos.toByteArray();
            receivingStr = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();
            Log.d(className, "Str received: " + receivingStr);

        }
        catch (SocketTimeoutException e)
        {
            // in case socket timed out. Happens occasionally.
            Log.d(className, "Normal timeout");

            byte[] bytes = baos.toByteArray();
            receivingStr = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();
            Log.d("str received", receivingStr);

        } catch (SocketException e)
        {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}