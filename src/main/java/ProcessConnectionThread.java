import javax.microedition.io.StreamConnection;

public class ProcessConnectionThread implements Runnable {

    private StreamConnection streamConnection;

    public ProcessConnectionThread(StreamConnection connection) {
        streamConnection = connection;
    }

    @Override
    public void run() {
        System.out.println("bluetooth connected");
    }
}
