package cache;

import javax.swing.tree.TreeNode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class KeyValueStore implements Cache {
    private Map<String, VersionedValue> data;
    private ThreadLocal<Transaction> currentTransaction;
    private AtomicLong globalVersion;
    public KeyValueStore() {
        this.data = new HashMap<>();
        this.currentTransaction = new ThreadLocal<>();
        this.globalVersion = new AtomicLong(0);
    }
    @Override
    public String get(String key) {
        Transaction transaction = currentTransaction.get();
        if (transaction != null) {
            if (transaction.getDeleteSet().contains(key)) {
                return null;
            }
            if (transaction.getWriteSet().containsKey(key)) {
                return transaction.getWriteSet().get(key);
            }
        }

        VersionedValue v = data.get(key);
//        System.out.println(v);
        if (v==null) {
            return null;
        }
        if ( transaction != null) {
            transaction.getReadSet().putIfAbsent(key, v);
        }
        return v.getValue();
    }

    @Override
    public void set(String key, String value) {
        Transaction transaction = currentTransaction.get();
        if (transaction != null) {
            transaction.getWriteSet().put(key,value);
            transaction.getDeleteSet().remove(key);
        } else {
            data.put(key, new VersionedValue(value, globalVersion.incrementAndGet()));
        }
    }

    @Override
    public void delete(String key) {
        Transaction transaction = currentTransaction.get();
        if (transaction!=null){
            transaction.getDeleteSet().add(key);
            transaction.getWriteSet().remove(key);
        } else {
            data.remove(key);
        }
    }

    @Override
    public void begin() {
        if (currentTransaction.get() != null) {
            throw new IllegalStateException("txn already in progress");
        }
        currentTransaction.set(new Transaction());
    }

    @Override
    public boolean commit() {
        Transaction transaction = currentTransaction.get();
        if (transaction == null) {
            throw new IllegalStateException("no active txn");
        }

        for(Map.Entry<String, VersionedValue> entry: transaction.getReadSet().entrySet()) {
            VersionedValue currVersionedValue = data.get(entry.getKey());
            if (currVersionedValue == null || currVersionedValue.getVersion() != entry.getValue().getVersion()){
                currentTransaction.remove();
                return false;
            }
        }

        for (String key:transaction.getDeleteSet()){
            data.remove(key);
        }
        for(Map.Entry<String,String> entry: transaction.getWriteSet().entrySet()){
            data.put(entry.getKey(), new VersionedValue(entry.getValue(), globalVersion.incrementAndGet()));
        }
        currentTransaction.remove();
        return true;
    }

    @Override
    public void rollback() {
        Transaction transaction = currentTransaction.get();
        if(transaction == null) {
            throw new IllegalStateException("No active txn");
        }
        currentTransaction.remove();
    }
}
