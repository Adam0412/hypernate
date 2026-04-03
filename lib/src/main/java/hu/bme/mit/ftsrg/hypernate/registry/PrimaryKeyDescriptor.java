package hu.bme.mit.ftsrg.hypernate.registry;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyDescriptor {
    private EntityMeta forEntity;
    private List<AttributeDescriptor> descriptor;

    public PrimaryKeyDescriptor(EntityMeta entity) {
        forEntity = entity;
        descriptor = new ArrayList<>();
    }

    public void add(AttributeDescriptor desc) {
        descriptor.add(desc);
    }

    public EntityMeta getEntityMeta() {
        return forEntity;
    }

    public List<AttributeDescriptor> getAttributeDescriptiors() {
        return descriptor;
    }
}
