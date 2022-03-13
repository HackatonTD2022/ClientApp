/*
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.Objects;


public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Button btn;
    private ImageView imageView;

    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("fxml/windowtest.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();

        btn = (Button) scene.lookup("#BluetoothButton");
        imageView = (ImageView) scene.lookup("#ImageView");

        if(Objects.nonNull(btn)) {
            btn.setOnMouseClicked(mouseEvent -> {
                testBluetooth();
            });
        }
    }

    public void testBluetooth() {
        Loop lp = new Loop();

        Thread thread = new Thread(lp);
        thread.start();

        while (!lp.IsReady()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String path = "qrcode.png";
        Image image = new Image(path);
        imageView.setImage(image);

    }
}
*/
