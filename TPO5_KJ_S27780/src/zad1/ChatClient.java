/**
 * @author Karwowski Jakub S27780
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private String host;
    private int port;
    private String id;
    private SocketChannel socketChannel;
    private boolean isFinished = false;
    String clientLog;

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("localhost", 9999, "adas");
        chatClient.connect();
        String login = chatClient.send("login adas");
        System.out.println(login);
        String dobry = chatClient.send("Dzień dobry");
        System.out.println(dobry);
        String aaaa = chatClient.send("aaaa");
        System.out.println(aaaa);
        String bbbb = chatClient.send("bbbb");
        System.out.println(bbbb);
        String widzenia = chatClient.send("Do widzenia");
        System.out.println(widzenia);
        String logout = chatClient.send("logout");
        System.out.println(logout);
    }

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        clientLog = "";

    }

    public String getChatView() {
        return clientLog;
    }

    public void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.bind(null);
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String send(String request) {
        if (isFinished) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            sendRequest(request + "$");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (request.equals("bye") || request.equals("bye and log transfer")) {
                isFinished = true;
            }
            return readResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

    private void sendRequest(String request) throws IOException {

        byteBuffer.clear();

        byteBuffer.put(request.getBytes(Charset.defaultCharset()));

        byteBuffer.flip();
        while (byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }


    }

    // escapujesz podwojnie $
    // kleint jak to wysyla to zamieasz na jakies %20
    // i to samo robisz na serwerze
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

    private String readResponse() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        readBuffer.clear();
        boolean stillReading = true;
        while (stillReading) {
            int read = socketChannel.read(readBuffer);
            if (read == 0) {
                continue;
            } else if (read == -1) {
                break;
            } else {
                readBuffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(readBuffer);
                while (charBuffer.hasRemaining()) {
                    char currentChar = charBuffer.get();
                    if (currentChar == '$') {
                        stillReading = false;
                        break;
                    }
                    responseBuilder.append(currentChar);
                }
            }
        }
        return responseBuilder.toString();
    }

    public String getId() {
        return this.id;
    }
}
