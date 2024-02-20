import java.io.File;
import java.util.UUID;
import java.util.Timer;
// import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.io.IOException;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class App {
    public static void main(String[] args) {
        try {
            int pId = Integer.parseInt(args[0]);
            System.out.println(
                    "\n DISTRIBUTED FILE SYSTEM (JAVA RMI)\n\n Node: " + NodeInterface.services[pId]);

            Node node = new Node(pId);
            initServer(node);
            initClient(node, pId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initServer(Node node) throws RemoteException {
        Registry reg = LocateRegistry.createRegistry(node.getPort());
        reg.rebind(node.getService(), node);
    }

    @SuppressWarnings("all")
    private static void initClient(Node node, int pId) throws RemoteException, NotBoundException {
        byte[] file;
        String fileName, filePath;
        Scanner scan = new Scanner(System.in);
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    node.fetchNewRequest();
                } catch (RemoteException ex) {
                    System.out.println(ex.getMessage());
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 100);

        // timer.scheduleAtFixedRate(new TimerTask() {
        // @Override
        // public void run() {
        // Random random = new Random();
        // int randTime = random.nextInt(1500, 3000); // delay time for testing
        // int randTime2 = random.nextInt(3001, 3002); // delay time for testing
        // try {
        // if (node.getLeader()) {
        // node.sendHeartBeat();
        // Thread.sleep(randTime2); // delay for testing
        // } else {
        // Thread.sleep(randTime); // delay for testing
        // if (!node.getHb() && !node.checkLeaders(0)) {
        // System.out.println("\n\nTimeout: No heart beat received, starting
        // election...");
        // node.resetLeaders();
        // node.setLeader(true);
        // } else {
        // node.setHb(false);
        // }
        // }
        // } catch (RemoteException ex) {
        // ex.printStackTrace();
        // } catch (NotBoundException ex) {
        // ex.printStackTrace();
        // } catch (InterruptedException ex) {
        // ex.printStackTrace();
        // }
        // }
        // }, 0, 2000);

        while (true) {
            System.out.print("\n OPERATIONS:\n1 - Search\n2 - Download\n3 - Upload\n4 - Delete\n\n --> ");
            int operation = scan.nextInt();
            if (operation < 1 || operation > 4)
                continue;

            fileName = "";
            filePath = "";
            file = null;

            System.out.print("\nEnter file name: ");
            fileName = scan.next();

            if (operation == Request.UPLOAD) {
                System.out.print("\nEnter file path: ");
                filePath = scan.next();
                try {
                    file = fileToByteArray(new File(filePath));

                } catch (Exception e) {
                    System.out.println("\nFile Path doesn't exist.");
                    continue;
                }
            }

            node.leaderElection();
            node.updateLogicalClock();

            Request r = null;

            if (operation == Request.UPLOAD)
                r = new Request(UUID.randomUUID().toString(), pId, node.getService(), operation, fileName, file,
                        node.getLogicalClock());
            else
                r = new Request(UUID.randomUUID().toString(), pId, node.getService(), operation, fileName,
                        node.getLogicalClock());

            node.multicastRequest(r);
            try {
                Thread.sleep(1250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] fileToByteArray(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}