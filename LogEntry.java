import java.io.Serializable;

public class LogEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private int term;

    LogEntry(int term) {
        this.term = term;
    }

    public int getTerm() {
        return this.term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

}
