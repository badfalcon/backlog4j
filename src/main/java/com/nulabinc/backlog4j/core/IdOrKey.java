package com.nulabinc.backlog4j.core;

/**
 * @author nulab-inc
 */
public class IdOrKey {
    private Union<Long, String> idOrKey;

    private IdOrKey(Union<Long, String> idOrKey) {
        this.idOrKey = idOrKey;
    }

    public String toString() {
        return this.idOrKey.toString();
    }

    public Optional<Long> id() {
        return idOrKey.left();
    }

    public Optional<String> key() {
        return idOrKey.right();
    }

    public static IdOrKey key(String key) {
        return new IdOrKey(Union.right(key));
    }

    public static IdOrKey id(Long id) {
        return new IdOrKey(Union.left(id));
    }

    public static IdOrKey id(Integer id) {
        return new IdOrKey(Union.left(id.longValue()));
    }
}
