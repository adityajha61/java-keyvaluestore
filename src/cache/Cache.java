package cache;

public interface Cache {
    String get(String key);
    void set(String key, String value);
    void delete(String key);
    void begin();
    boolean commit();
    void rollback();
}
