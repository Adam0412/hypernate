package hu.bme.mit.ftsrg.hypernate.registry;

public class EntityMeta {
    private String entityClassname;
    private PrimaryKeyDescriptor pkDescriptor;

    public EntityMeta(String entityClassName, PrimaryKeyDescriptor descriptor) {
        this.entityClassname = entityClassName;
        this.pkDescriptor = descriptor;
    }

    public String getClassName() {
        return entityClassname;
    }

    public PrimaryKeyDescriptor getPrimaryKeyDescriptor() {
        return pkDescriptor;
    }

    public void setPrimaryKeyDescriptor(PrimaryKeyDescriptor pkDescriptor) {
        this.pkDescriptor = pkDescriptor;
    }
}
