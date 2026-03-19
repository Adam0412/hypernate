package hu.bme.mit.ftsrg.hypernate.registry;

import java.util.List;

public class PrimaryKeyDescriptor {
    private EntityMeta forEntity;
    private List<AttributeDescriptor> descriptor;

    public PrimaryKeyDescriptor(EntityMeta entity) {
        forEntity = entity;
        descriptor = null;
    }

    public void add(AttributeDescriptor desc) {
        descriptor.add(desc);
    }

    public EntityMeta getEntityMeta() {
        return forEntity;
    }
}
