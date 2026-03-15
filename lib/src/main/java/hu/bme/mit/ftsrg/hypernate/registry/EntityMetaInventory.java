package hu.bme.mit.ftsrg.hypernate.registry;

import java.util.Set;

public class EntityMetaInventory {
    private Set<EntityMeta> data;

    public void add(EntityMeta meta) {
        data.add(meta);
    }

}
