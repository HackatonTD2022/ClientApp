import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.github.shashankn.qrterminal.QRCode;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.UUID;

public class Loop implements Runnable {

    private UUID uuid;
    private boolean ready = false;

    /** Constructor */
    public Loop() {
        uuid = UUID.randomUUID();
    }

    private static void generateQRCode(String text, int width, int height, String filePath) throws Exception {
        QRCodeWriter qcwobj = new QRCodeWriter();
        BitMatrix bmobj = qcwobj.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path pobj = FileSystems.getDefault().getPath(filePath);
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

            local.setDiscoverable(DiscoveryAgent.GIAC);

            StringBuilder str = new StringBuilder();

            str.append("UUID: ").append(uuid.toString()).append("\n");

            StringBuilder sbAddress = new StringBuilder();
            char[] arr = local.getBluetoothAddress().toCharArray();
            for(int i = 0; i < arr.length; i++) {
                sbAddress.append(arr[i]);
                if(i % 2 == 1)
                    sbAddress.append(':');
            }
            String address = sbAddress.substring(0, sbAddress.length() - 1);

            str.append("Address: ").append(address).append("\n");
            str.append("Name: ").append(local.getFriendlyName());

            System.out.println(QRCode.from(str.toString())
                    .withSize(5,5)
                    .withMargin(1)
                    .withErrorCorrection(ErrorCorrectionLevel.H)
                    .generate());

            generateQRCode(str.toString(),1250, 1250, "qrcode.png");

            System.out.println("Address: " + local.getBluetoothAddress());
            System.out.println("Name: " + local.getFriendlyName());
            System.out.println("UUID: " + uuid.toString());

            String uuidStr = uuid.toString().replaceAll("-", "");

            String url = "btspp://localhost:" + uuidStr + ";name=RemoteBluetooth";
            System.out.println("URL: " + url);
            notifier = (StreamConnectionNotifier)Connector.open(url);

            ready = true;
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
