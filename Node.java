import java.rmi.*;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.PriorityQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

@SuppressWarnings("all")
public class Node extends UnicastRemoteObject implements RaftNode {
    private static final long serialVersionUID = 1L;

    static int counter = 0;
    private boolean isLeader;
    private String ip, service;
    private List<LogEntry> log;
    private int logicalClock, port, currentTerm, votedFor;
    private boolean hb = false;

    private PriorityQueue<Request> requestQueue;
    private HashMap<String, Integer> requestAcks;
    private HashMap<String, byte[]> localStorage;

    public Node(int n) throws RemoteException {
        isLeader = false;
        ip = ipAddr[n];
        port = ports[n];
        service = services[n];
        logicalClock = 0;
        requestQueue = new PriorityQueue<>();
        requestAcks = new HashMap<String, Integer>();
        localStorage = ls;
        log = new ArrayList<LogEntry>();
    }

    public boolean getLeader() {
        return this.isLeader;
    }

    public int getPort() {
        return this.port;
    }

    public String getIP() {
        return this.ip;
    }

    public String getService() {
        return this.service;
    }

    public int getLogicalClock() {
        return this.logicalClock;
    }

    public void updateLogicalClock() {
        this.logicalClock++;
    }

    @Override
    public byte[] download(String filename) throws RemoteException, NotBoundException {

        byte[] x = new byte[0];

        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface otherNodes = (NodeInterface) reg.lookup(services[i]);

            if (otherNodes.getFiles().containsKey(filename)) {
                return otherNodes.getFiles().get(filename);
            }
        }

        return x;
    }

    @Override
    public void upload(String filename, byte[] fileContent) throws RemoteException, NotBoundException {
        localStorage.put(filename, fileContent);
        System.out.println("\nRequest Status: File uploaded sucessfully.");
    }

    @Override
    public void delete(String filename) throws RemoteException, NotBoundException {
        if (localStorage.containsKey(filename)) {
            localStorage.remove(filename);
            System.out.println("\nRequest Status: File Deleted.");
            return;
        }
        System.out.println("\nRequest Status: File Doesn't Exist.");
    }

    @Override
    public String search(String filename) throws RemoteException, NotBoundException {

        boolean status = false;
        String node = "";

        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface otherNodes = (NodeInterface) reg.lookup(services[i]);

            if (otherNodes.getFiles().containsKey(filename)) {
                status = true;
                node = services[i];
                break;
            }
        }

        return "\nRequest Status: " + (status ? "File Found (Node " + node + ")." : "File Not Found.");
    }

    @Override
    public void multicastRequest(Request r) throws RemoteException, NotBoundException {
        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface e = (NodeInterface) reg.lookup(services[i]);
            e.performRequest(r);
        }
    }

    @Override
    public void performRequest(Request r) throws RemoteException, NotBoundException {
        requestQueue.add(r);
        if (!r.getSender().equalsIgnoreCase(service)) {
            logicalClock = Math.max(logicalClock, r.getLogicalClock()) + 1;
        }
        multicastAck(r);
    }

    @Override
    public void ack(Request request) throws RemoteException {

        if (requestAcks.containsKey(request.getRequestId())) {
            requestAcks.put(request.getRequestId(), requestAcks.get(request.getRequestId()) + 1);
        } else
            requestAcks.put(request.getRequestId(), 1);
    }

    private void multicastAck(Request r) throws RemoteException, NotBoundException {
        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface e = (NodeInterface) reg.lookup(services[i]);
            e.ack(r);
        }
    }

    public void fetchNewRequest() throws RemoteException, NotBoundException {
        if (requestQueue.size() > 0 && requestAcks.containsKey(requestQueue.peek().getRequestId())
                && requestAcks.get(requestQueue.peek().getRequestId()) == ipAddr.length) {

            Request r = requestQueue.poll();
            requestAcks.remove(r.getRequestId());

            if (r.getOperation() == Request.SEARCH && isLeader) {
                System.out.println("\nPerforming Request: " + r.getRequestId() + "\nSearching file...");
                sendStatus(search(r.getFileName()), r.getPID());
                System.out.println("\nCompleted Request: " + r);
            } else if (r.getOperation() == Request.DOWNLOAD && isLeader) {
                System.out.println("\nPerforming Request: " + r.getRequestId() + "\nSending file...");
                sendDownload(download(r.getFileName()), r.getFileName(), r.getPID());
                System.out.println("\nCompleted Request: " + r);
            } else if (r.getOperation() == Request.UPLOAD) {
                System.out.println("\nPerforming Request: " + r.getRequestId() + "\nUploading file...");
                upload(r.getFileName(), r.getFile());
                System.out.println("\nCompleted Request: " + r.getRequestId());
            } else if (r.getOperation() == Request.DELETE) {
                System.out.println("\nPerforming Request: " + r.getRequestId() + "\nDeleting file...");
                delete(r.getFileName());
                System.out.println("\nCompleted Request: " + r.getRequestId());
            }
        }

    }

    public void sendDownload(byte[] fileContent, String filename, int pID) throws RemoteException, NotBoundException {

        Registry reg = LocateRegistry.getRegistry(ports[pID]);
        NodeInterface e = (NodeInterface) reg.lookup(services[pID]);

        e.downloadLocally(fileContent, filename);
        System.out.println("\nRequest Status: File Sent To Sender.");
    }

    @Override
    public void downloadLocally(byte[] fileContent, String filename) {
        if (fileContent.length == 0) {
            System.out.println("\nFile Doesn't Exist.");
            return;
        }
        try {

            File file = new File("/Users/youssifsamir/Desktop/    /Distrubuted/Project/" + service, filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContent);
            System.out.println("\nFile Downloaded Sucessfully.");

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void leaderElection() throws RemoteException, NotBoundException {
        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface e = (NodeInterface) reg.lookup(services[i]);
            e.setLeader(false);
        }

        Random random = new Random();
        int i = random.nextInt(ipAddr.length);
        Registry reg = LocateRegistry.getRegistry(ports[i]);
        NodeInterface e = (NodeInterface) reg.lookup(services[i]);
        e.setLeader(true);
    }

    @Override
    public HashMap<String, byte[]> getFiles() throws RemoteException {
        return localStorage;
    }

    @Override
    public void setLeader(boolean status) {
        this.isLeader = status;
        if (status)
            System.out.println("\n I'm the leader.\n");

    }

    @Override
    public void resetLeaders() throws RemoteException, NotBoundException {
        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface e = (NodeInterface) reg.lookup(services[i]);
            e.setLeader(false);
        }
    }

    @Override
    public void receiveStatus(String status) {
        System.out.println(status);
    }

    @Override
    public void sendStatus(String status, int pID) throws RemoteException, NotBoundException {
        Registry reg = LocateRegistry.getRegistry(ports[pID]);
        NodeInterface e = (NodeInterface) reg.lookup(services[pID]);
        e.receiveStatus(status);
    }

    @Override
    public void startElection() throws RemoteException {
    }

    @Override
    public void sendHeartBeat() throws RemoteException, NotBoundException {
        for (int i = 0; i < ipAddr.length; i++) {
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeInterface e = (NodeInterface) reg.lookup(services[i]);
            e.receiveHeartBeat(getService());
        }
    }

    @Override
    public void receiveHeartBeat(String node) throws RemoteException {
        this.hb = true;
        System.out.println("\n Heart Beat Received From Node " + node + ".");
    }

    static int index;

    @Override
    public boolean checkLeaders(int n) {
        if (index >= ipAddr.length)
            return false;
        try {
            for (int i = 0; i < ipAddr.length; i++) {
                Registry reg = LocateRegistry.getRegistry(ports[i]);
                NodeInterface e = (NodeInterface) reg.lookup(services[i]);
                if (e.isLeader)
                    return true;
                index = i;
            }
            return false;

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getHb() {
        return this.hb;
    }

    public void setHb(boolean x) {
        this.hb = x;
    }
}
