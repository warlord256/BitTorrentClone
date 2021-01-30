import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class MessageSender extends Thread{
    private Queue<Message> messagesToSend;
    private ObjectOutputStream out;
    private boolean running = true;
    private RemotePeer peer;

    MessageSender(RemotePeer peer) throws IOException {
        this.messagesToSend=new LinkedList<>();
        this.peer = peer;
        this.out=new ObjectOutputStream(peer.connection.getOutputStream());
    }
    public void send(Message message){
        synchronized (messagesToSend){
            messagesToSend.offer(message);
            if(messagesToSend.size()==1) {
                messagesToSend.notify();
            }
        }

    }

    public void stopThread() {
        this.running = false;
        // Might have to notify to continue execution and end.
        synchronized (messagesToSend) {
            this.messagesToSend.notify();
        }
    }

    private boolean shouldDropMessage() {
        return (messagesToSend.peek().getMessageType()==MessageType.PIECE && peer.getState()==RemotePeerState.CHOKED);
    }

    @Override
    public void run() {
        try{
            while(this.running){
                synchronized (messagesToSend) {
                    if (!messagesToSend.isEmpty()) {
                        if(!shouldDropMessage()) {
                            out.writeObject(messagesToSend.poll());
                        } else {
                            messagesToSend.poll();
                        }
                    } else {
                        messagesToSend.wait();
                    }
                }
            }
//            out.close();
        } catch (SocketException e) {
//            System.out.println("Oops, the other party disconnected");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("Quitting the Sender");
        }
    }
}
