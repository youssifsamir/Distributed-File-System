
import java.rmi.Remote;
import java.util.HashMap;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public interface NodeInterface extends Remote {
    String[] ipAddr = { "127.0.0.1", "127.0.0.1", "127.0.0.1", "127.0.0.1" };
    Integer[] ports = { 2000, 3000, 4000, 5004 };
    String[] services = { "A", "B", "C", "D" };

    HashMap<String, byte[]> ls = new HashMap<String, byte[]>();
    boolean isLeader = false;

    void multicastRequest(Request request) throws RemoteException, NotBoundException;

    void performRequest(Request r) throws RemoteException, NotBoundException;

    void ack(Request request) throws RemoteException;

    byte[] download(String filename) throws RemoteException, NotBoundException;

    void upload(String filename, byte[] fileContent) throws RemoteException, NotBoundException;

    void delete(String filename) throws RemoteException, NotBoundException;

    String search(String filename) throws RemoteException, NotBoundException;

    HashMap<String, byte[]> getFiles() throws RemoteException;

    void setLeader(boolean isLeader) throws RemoteException;

    void sendStatus(String status, int pID) throws RemoteException, NotBoundException;

    void receiveStatus(String status) throws RemoteException, NotBoundException;

    void downloadLocally(byte[] fileContent, String filename) throws RemoteException, NotBoundException;

    void receiveHeartBeat(String node) throws RemoteException, NotBoundException;

    void resetLeaders() throws RemoteException, NotBoundException;

    boolean checkLeaders(int n) throws RemoteException, NotBoundException;

}
