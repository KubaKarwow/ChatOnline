/**
 * @author Karwowski Jakub S27780
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ChatServer extends Thread {
    private Map<SocketChannel, String> userMaps;
    private Map<String, StringBuilder> logs;
    private StringBuilder serverLog;
    private String host;
    private int port;

    private ServerSocketChannel channel;
    private Selector selector;
    private volatile boolean isRunning = true;

    ChatServer(String host, int port) {
        userMaps = new HashMap<>();
        logs = new HashMap<>();
        serverLog = new StringBuilder();
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        ChatServer localhost = new ChatServer("localhost", 9999);
        localhost.startServer();
    }

    public void startServer() throws IOException {
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(host, port));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started\n");
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        isRunning = false;
        System.out.println("Server stopped");
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        try {
            handleConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleConnections() throws IOException {

        while (isRunning) {
            selector.select();
            if (!selector.isOpen()) {
                return;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                    continue;
                }
                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.configureBlocking(false);
                    handleRequest(socketChannel);
                }
            }
        }
    }

    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    public void handleRequest(SocketChannel socketChannel) throws IOException {
        if (!socketChannel.isOpen()) {
            return;
        }
        List<String> requests= readRequest(socketChannel);
     //   System.out.println("------requests----");
     //  requests.forEach(System.out::println);
        for (String request : requests) {
            String response = getResponse(request, socketChannel);
          //  System.out.println("response:"+response);
            updateServerLog(response);
            writeResponseToEveryone(response);
            closeAndRemoveSocketChannelIfNecessary(response);
        }
    }
    private void updateServerLog(String response){
        LocalDateTime now = LocalDateTime.now();
        String timeNow = now.toString().substring(11, 23);
        serverLog.append(timeNow + " " + response.substring(0,response.length()-1) + '\n');
    }
    private void writeResponseToEveryone(String response) throws IOException {
        Set<SocketChannel> socketChannels = userMaps.keySet();
        for (SocketChannel socketChannel : socketChannels) {
            if (socketChannel.isOpen()) {
                ByteBuffer encode = Charset.defaultCharset().encode(response);
                while (encode.hasRemaining()) {
                    socketChannel.write(encode);
                }
            }
        }
    }

    private void closeAndRemoveSocketChannelIfNecessary(String response) throws IOException {
        if (response.endsWith("logged out$")) {
            String[] words = response.split(" ");
            String userID = words[0];
            for (SocketChannel socketChannel : userMaps.keySet()) {
                if (userMaps.get(socketChannel).equals(userID)) {
                    socketChannel.close();
                    userMaps.remove(socketChannel, userID);
                    return;
                }
            }
        }
    }

    private String getResponse(String request, SocketChannel socketChannel) {
        if (request.startsWith("login")) {
            login(request.split(" ")[1], socketChannel);
            return getLoginResponse(socketChannel);
        } else if (request.equals("logout")) {
            logout(socketChannel);
            return getLogoutResponse(socketChannel);
        } else {
            return getRequestResponse(socketChannel, request);
        }
    }

    private String getLogoutResponse(SocketChannel socketChannel) {
        String userId = userMaps.get(socketChannel);
        return userId + " logged out$";
    }

    private String getLoginResponse(SocketChannel socketChannel) {
        String userId = userMaps.get(socketChannel);
        return userId + " logged in$";
    }

    private void logout(SocketChannel socketChannel) {
        String userID = userMaps.get(socketChannel);
    }

    private void login(String id, SocketChannel socketChannel) {
        userMaps.put(socketChannel, id);
    }

    private void putNewUserInDB(String id, SocketChannel socketChannel) {
        userMaps.put(socketChannel, id);
        logs.put(id, new StringBuilder());
    }

    private List<String> readRequest(SocketChannel socketChannel) throws IOException {
        List<String> result = new ArrayList<>();
        buffer.clear();
        boolean stillReading = true;
        StringBuilder resultBuilder = new StringBuilder();
        while (stillReading) {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == 0) {
                continue;
            }
            if (bytesRead == -1) {
                break;
            }
            buffer.flip();
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            while (charBuffer.hasRemaining()) {
                char currentChar = charBuffer.get();
                if (currentChar == '$') {
                    result.add(resultBuilder.toString());
                    resultBuilder = new StringBuilder();
                } else{
                    resultBuilder.append(currentChar);
                }
            }
            stillReading = false;
        }
        return result.stream().map(this::handleDollarSigns).collect(Collectors.toList());
    }

    private String handleDollarSigns(String request) {
        //
        return request.replaceAll("%20", "$");

    }

    public String getRequestResponse(SocketChannel socketChannel, String request) {
        String userID = userMaps.get(socketChannel);
        return userID + ": " + request + "$";
    }

    private String getIdByChannel(SocketChannel socketChannel) {
        return userMaps.get(socketChannel);
    }

    private String strip(String string) {
        return string.replaceAll("\\s", "");
    }

    public Map<SocketChannel, String> getUserMaps() {
        return userMaps;
    }

    public void setUserMaps(Map<SocketChannel, String> userMaps) {
        this.userMaps = userMaps;
    }

    public Map<String, StringBuilder> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, StringBuilder> logs) {
        this.logs = logs;
    }

    public StringBuilder getServerLog() {
        return serverLog;
    }

    public void setServerLog(StringBuilder serverLog) {
        this.serverLog = serverLog;
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
}
