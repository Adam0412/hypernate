package hu.bme.mit.ftsrg.hypernate.registry;

import java.util.Set;

import hu.bme.mit.ftsrg.hypernate.annotations.PrimaryKey;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class EntityMetaInventory {
    private Set<EntityMeta> data;

    public EntityMetaInventory() {
        initialize();
    }

    public void add(EntityMeta meta) {
        data.add(meta);
    }

    public EntityMeta getForClass(Class<?> clazz) {
        for (EntityMeta em : data) {
            if (em.getClassName().equals(clazz.getSimpleName())) {
                return em;
            }
        }
        return null;
    }

    public static void initialize() {
        try (ScanResult result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            ClassInfoList classinfo = result.getClassesWithAnnotation(PrimaryKey.class);
        } catch (Exception e) {

        }
    }
}
