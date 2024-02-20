import java.io.Serializable;

public class Request implements Serializable, Comparable<Request> {
    private static final long serialVersionUID = 1L;

    public static final int SEARCH = 1;
    public static final int DOWNLOAD = 2;
    public static final int UPLOAD = 3;
    public static final int DELETE = 4;

    private String requestId, sender, fileName;
    private int logicalClock, operation, pID;
    private byte[] file;

    public Request(String requestId, int pID, String sender, int operation, String fileName,
            int logicalClock) {
        this.requestId = requestId;
        this.pID = pID;
        this.sender = sender;
        this.operation = operation;
        this.fileName = fileName;
        this.logicalClock = logicalClock;
    }

    public Request(String requestId, int pID, String sender, int operation, String fileName, byte[] file,
            int logicalClock) {
        this.requestId = requestId;
        this.pID = pID;
        this.sender = sender;
        this.operation = operation;
        this.fileName = fileName;
        this.file = file;
        this.logicalClock = logicalClock;
    }

    @Override
    public int compareTo(Request r) {
        // Tie Breaker
        if (this.logicalClock == r.getLogicalClock())
            return Math.min(r.getPID(), this.pID);
        return this.logicalClock - r.getLogicalClock();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getPID() {
        return this.pID;
    }

    public void setPID(int pID) {
        this.pID = pID;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return this.sender;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFile() {
        return this.file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public int getLogicalClock() {
        return logicalClock;
    }

    public void setLogicalClock(int logicalClock) {
        this.logicalClock = logicalClock;
    }
}
