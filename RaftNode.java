
// import java.util.List;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface RaftNode extends NodeInterface {
    // void startElection() throws RemoteException, NotBoundException;

    // void receiveVoteRequest(int candidateId, int term) throws RemoteException,
    // NotBoundException;

    // void receiveVoteResponse(int voterId, boolean voteGranted, int term) throws
    // RemoteException, NotBoundException;

    // void receiveAppendEntries(int leaderId, int term, List<LogEntry> entries)
    // throws RemoteException, NotBoundException;

    // void respondAppendEntries(int followerId, boolean success, int term) throws
    // RemoteException, NotBoundException;

    void startElection() throws RemoteException;

    void sendHeartBeat() throws RemoteException, NotBoundException;

    void receiveHeartBeat(String node) throws RemoteException;
}