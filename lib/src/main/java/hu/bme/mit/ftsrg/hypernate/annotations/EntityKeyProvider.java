package hu.bme.mit.ftsrg.hypernate.annotations;

@FunctionalInterface
public interface EntityKeyProvider {
    String getKey(Object entity);
}
