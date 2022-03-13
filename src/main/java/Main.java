
public class Main {
    public static void main(String[] args) {

        Loop lp = new Loop();

        Thread thread = new Thread(lp);
        thread.start();

        //ClientApp.main(args);
    }
}
