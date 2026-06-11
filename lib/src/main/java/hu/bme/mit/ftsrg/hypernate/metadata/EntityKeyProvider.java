package hu.bme.mit.ftsrg.hypernate.metadata;

@FunctionalInterface
public interface EntityKeyProvider {
    String getKey(Object entity);
}
