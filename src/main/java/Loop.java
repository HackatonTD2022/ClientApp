import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Loop implements Runnable {

    private String uuidStr = "d0c722b07e1511e1b0c40800200c9a66";
    private boolean ready = false;

    /** Constructor */
    public Loop() {
    }

    private static void generateQRCode(String text, int width, int height, String filePath) throws Exception {
        QRCodeWriter qcwobj = new QRCodeWriter();
        BitMatrix bmobj = qcwobj.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path pobj = FileSystems.getDefault().getPath("src/main/resources/" + filePath);
        MatrixToImageWriter.writeToPath(bmobj, "PNG", pobj);
    }

    public boolean IsReady() {
        return ready;
    }

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */
    private void waitForConnection() {
        // retrieve the local Bluetooth device object
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            System.out.println("Address: " + local.getBluetoothAddress());
            System.out.println("Name: " + local.getFriendlyName());
            local.setDiscoverable(DiscoveryAgent.GIAC);

            StringBuilder str = new StringBuilder();
            str.append("UUID: ").append(uuidStr).append("\n");
            str.append("Address: ").append(local.getBluetoothAddress()).append("\n");
            str.append("Name: ").append(local.getFriendlyName());

            generateQRCode(str.toString(),1250, 1250, "qrcode.png");
            ready = true;

            UUID uuid = new UUID(uuidStr, false);
            System.out.println(uuid.toString());

            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // waiting for connection
        while(true) {
            try {
                System.out.println("waiting for connection...");
                connection = notifier.acceptAndOpen();
                System.out.println("After AcceptAndOpen...");

                Thread processThread = new Thread(new ProcessConnectionThread(connection));
                processThread.start();

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
