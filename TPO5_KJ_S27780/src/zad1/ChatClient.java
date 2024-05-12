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
    private volatile ChatReader chatReader;

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("localhost", 9999, "adas");
        chatClient.connect();

    }

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public String getChatView() {
        StringBuilder chatViewBuilder = new StringBuilder();
        for (String s : chatReader.getLog()) {
            chatViewBuilder.append(s).append('\n');
        }
        return chatViewBuilder.toString();
    }

    public void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.bind(null);
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
            chatReader= new ChatReader(socketChannel,id);
            chatReader.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String request) {
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


    public String getId() {
        return this.id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public ChatReader getChatReader() {
        return chatReader;
    }

    public void setChatReader(ChatReader chatReader) {
        this.chatReader = chatReader;
    }
}
