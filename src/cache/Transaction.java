package cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class Transaction {
    private Map<String, VersionedValue> readSet;
    private Map<String, String> writeSet;
    private Set<String> deleteSet;
    public Transaction() {
        this.readSet = new HashMap<>();
        this.writeSet = new HashMap<>();
        this.deleteSet = new HashSet<>();
    }

    public Map<String, VersionedValue> getReadSet() {
        return readSet;
    }

    public void setReadSet(Map<String, VersionedValue> readSet) {
        this.readSet = readSet;
    }

    public Map<String, String> getWriteSet() {
        return writeSet;
    }

    public void setWriteSet(Map<String, String> writeSet) {
        this.writeSet = writeSet;
    }

    public Set<String> getDeleteSet() {
        return deleteSet;
    }

    public void setDeleteSet(Set<String> deleteSet) {
        this.deleteSet = deleteSet;
    }
}
