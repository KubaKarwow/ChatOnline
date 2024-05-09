/**
 *
 *  @author Karwowski Jakub S27780
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {
    private ChatClient client;
    private List<String> messages;
    private int wait;
    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        super(ChatClientTask::handleClient);
        this.client=c;
        this.messages=msgs;
        this.wait=wait;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c,msgs,wait);
    }

    public static String handleClient(){
        return null;
    }

    public ChatClient getClient() {
        return client;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }
}
