
import cache.KeyValueStore;

public class Main {
    public static void main(String[] args) {
        KeyValueStore kv = new KeyValueStore();
        kv.begin();
        kv.set("key1","value1");
        kv.commit();
        String val = kv.get("key1");

        System.out.println(kv.get("key1"));
    }
}