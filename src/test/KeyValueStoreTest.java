package test;
import cache.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValueStoreTest {
    private KeyValueStore store;

    @BeforeEach
    void setUp() {
        store = new KeyValueStore();
    }

    @Test
    void testBasicOperations() {
        store.set("k1","v1");
        assertEquals("v1", store.get("k1"));

        store.set("k1","updatedValue");
        assertEquals("updatedValue", store.get("k1"));

        store.delete("k1");
        assertNull(store.get("k1"));
    }

    @Test
    void testTransactionCommit() {
        store.set("k1","val1");
        store.begin();
        store.set("k1","txnVal");
        store.set("k2","newVal");
        assertTrue(store.commit());

        assertEquals("txnVal",store.get("k1"));
        assertEquals("newVal",store.get("k2"));
    }

    @Test
    void testTransactionRollback() {
        store.set("k1","val1");
        store.begin();
        store.set("k1","txnVal");
        store.set("k2","newVal");
        store.rollback();

        assertEquals("val1",store.get("k1"));
        assertNull(store.get("k2"));
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void testConcurrentTransactions() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0;i<threadCount;i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    store.begin();
                    store.set("key" +index,"value"+index);
                    assertTrue(store.commit());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        for (int i=0;i<threadCount;i++){
            assertEquals("value"+i,store.get("key"+i));
        }
    }

    @Test
    void testDelete() {
        store.set("k1","val1");
        store.begin();
        store.delete("k1");
        assertNull(store.get("k1"));
        assertTrue(store.commit());
        assertNull(store.get("k1"));
    }

    @Test
    void testReadYourOwnWrites() {
        store.begin();
        store.set("k1","val1");
        assertEquals("val1",store.get("k1"));
        assertTrue(store.commit());
    }
}
