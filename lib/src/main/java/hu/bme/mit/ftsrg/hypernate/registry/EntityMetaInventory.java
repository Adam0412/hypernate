package hu.bme.mit.ftsrg.hypernate.registry;

import java.lang.reflect.Field;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import hu.bme.mit.ftsrg.hypernate.annotations.AttributeInfo;
import hu.bme.mit.ftsrg.hypernate.annotations.KeyClass;
import hu.bme.mit.ftsrg.hypernate.annotations.Mapperinfo;
import hu.bme.mit.ftsrg.hypernate.annotations.PrimaryKey;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class EntityMetaInventory {
    private Set<EntityMeta> data;
    private static final Logger logger = LoggerFactory.getLogger(EntityMetaInventory.class);

    public EntityMetaInventory() {
        initialize();
    }

    public void add(EntityMeta meta) {
        data.add(meta);
    }

    public EntityMeta getForClass(Class<?> clazz) {
        for (EntityMeta em : data) {
            if (em.getClassName().equals(clazz.getName())) {
                return em;
            }
        }
        return null;
    }

    public void initialize() {
        try (ScanResult result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            ClassInfoList primaryKeyInfo = result.getClassesWithAnnotation(PrimaryKey.class);
            ClassInfoList keyClassInfo = result.getClassesWithAnnotation(KeyClass.class);
            if (primaryKeyInfo.isEmpty()) {
                logger.info("There are no classes with PrimaryKey annotation.");
            } else {
                primaryKeyInfo.forEach(info -> {
                    generateMetadatafromPrimaryKey(info);
                });
            }
            if (keyClassInfo.isEmpty()) {
                logger.info("There are no classes with KeyClass annotation.");
            } else {
                keyClassInfo.forEach(info -> {
                    generateMetadatafromKeyClass(info);
                });
            }
        } catch (Exception e) {
            logger.error("Failed to load classpaths", e);
        }
    }

    public void generateMetadatafromPrimaryKey(ClassInfo info) {
        Class<?> classes = info.loadClass();
        PrimaryKey pk = classes.getAnnotation(PrimaryKey.class);
        if (pk == null) {
            return;
        }
        EntityMeta meta = new EntityMeta(classes.getName(), null);
        PrimaryKeyDescriptor pKeyDescriptor = new PrimaryKeyDescriptor(meta);
        AttributeInfo[] attrinfos = pk.value();
        for (AttributeInfo attrinfo : attrinfos) {
            String name = attrinfo.name();
            String mappername = attrinfo.mapper().getName();
            AttributeDescriptor attributeDescriptor = new AttributeDescriptor(pKeyDescriptor, name, null);
            AttributeMapperDescriptor mapperDescriptor = new AttributeMapperDescriptor(attributeDescriptor, mappername);
            attributeDescriptor.setAttributeMapperDescriptor(mapperDescriptor);
            pKeyDescriptor.add(attributeDescriptor);
        }
        meta.setPrimaryKeyDescriptor(pKeyDescriptor);
        this.add(meta);
    }

    public void generateMetadatafromKeyClass(ClassInfo info) {
        Class<?> classes = info.loadClass();
        KeyClass key = classes.getAnnotation(KeyClass.class);
        if (key == null) {
            return;
        }
        Class<?> pointed = key.value();
        EntityMeta meta = new EntityMeta(pointed.getName(), null);
        PrimaryKeyDescriptor pKeyDescriptor = new PrimaryKeyDescriptor(meta);
        Field[] fields = classes.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            AttributeDescriptor attributeDescriptor = new AttributeDescriptor(pKeyDescriptor, name, null);
            if (field.isAnnotationPresent(Mapperinfo.class)) {
                Mapperinfo mapperinfo = field.getAnnotation(Mapperinfo.class);
                if (mapperinfo != null) {
                    String mapperName = mapperinfo.value().getName();
                    AttributeMapperDescriptor mapperDescriptor = new AttributeMapperDescriptor(attributeDescriptor,
                            mapperName);
                    attributeDescriptor.setAttributeMapperDescriptor(mapperDescriptor);
                }

            }
            pKeyDescriptor.add(attributeDescriptor);
        }
        meta.setPrimaryKeyDescriptor(pKeyDescriptor);
        this.add(meta);
    }

}
