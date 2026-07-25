/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.metadata;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.hypernate.mappers.AttributeMapper;
import hu.bme.mit.ftsrg.hypernate.registry.MissingKeysException;
import hu.bme.mit.ftsrg.hypernate.util.JSON;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(Loggable.DEBUG)
public class EntityMetadataProvider {
  private static final Logger logger = LoggerFactory.getLogger(EntityMetadataProvider.class);
  private EntityMetadataInventory metaInventory = new EntityMetadataInventory();
  private Map<Class<?>, EntityKeyProvider> keyProviders = new HashMap<>();

  <T> String getType(final T entity) {
    return getType(entity.getClass());
  }

  <T> String getType(final Class<T> clazz) {
    return clazz.getName().toUpperCase();
  }

  <T> String[] mapKeyPartsToString(final T entity, final Object... keyParts) {
    return mapKeyPartsToString(entity.getClass(), keyParts);
  }

  <T> String[] mapKeyPartsToString(final Class<T> clazz, final Object... keyParts) {
    EntityMeta em = metaInventory.getForClass(clazz);

    if (em == null) {
      throw new MissingKeysException("Entity metadata not found for class: " + clazz.getName());
    }

    List<Field> fields = new ArrayList<>();
    List<AttributeMapper> mappers = new ArrayList<>();
    PrimaryKeyDescriptor pk = em.getPrimaryKeyDescriptor();
    if (pk == null || pk.getAttributeDescriptiors() == null || pk.getAttributeDescriptiors().isEmpty()) {
      throw new MissingKeysException("No primary key descriptors found for class: " + clazz.getName());
    }
    List<AttributeDescriptor> pkAttributeDescriptors = pk.getAttributeDescriptiors();
    for (AttributeDescriptor descriptor : pkAttributeDescriptors) {
      try {
        Field field = clazz.getDeclaredField(descriptor.getAttrFieldName());
        field.setAccessible(true);
        fields.add(field);
      } catch (Exception e) {
        throw new MissingKeysException("Error accessing fields for class: " + clazz.getName(), e);
      }
      if (descriptor.getAttributeMapperDescriptor() == null) {
        mappers.add(null);
        continue;
      }
      String mapperName = descriptor.getAttributeMapperDescriptor().getMapperName();
      try {
        Class<?> mapperClass = Class.forName(mapperName);
        AttributeMapper mapper = (AttributeMapper) mapperClass.getDeclaredConstructor().newInstance();
        mappers.add(mapper);
      } catch (ReflectiveOperationException e) {
        logger.error("Failed to instantiate mapper: {}", mapperName, e);
        throw new MissingKeysException("Error instantiating mapper: " + mapperName, e);
      }
    }
    List<String> stringKeyParts = new ArrayList<>();
    for (int i = 0; i < fields.size(); i++) {
      String value = keyParts[i].toString();
      if (mappers.get(i) != null) {
        stringKeyParts.add(mappers.get(i).apply(value));
      } else {
        stringKeyParts.add(value);
      }
    }
    return stringKeyParts.toArray(String[]::new);
  }

  public <T> byte[] toBuffer(final T entity) {
    return toJson(entity).getBytes(StandardCharsets.UTF_8);
  }

  public <T> T fromBuffer(final byte[] buffer, final Class<T> clazz) {
    final String json = new String(buffer, StandardCharsets.UTF_8);
    logger.debug("Parsing entity from JSON: {}", json);
    return JSON.deserialize(json, clazz);
  }

  <T> String toJson(final T entity) {
    return JSON.serialize(entity);
  }

  public EntityKeyProvider getKeyProviderForClass(Class<?> clazz) {
    if (!keyProviders.containsKey(clazz)) {
      EntityKeyProvider provider = createEntityKeyProvider(clazz);
      keyProviders.put(clazz, provider);
    }
    return keyProviders.get(clazz);
  }

  /**
   * Generates a lambda which builds a CompositeKey for a given class instance
   *
   * <p>
   * Using reflection we access the Field values which are given as primary keys,
   * and with our
   * mappers instances we map the values and with these we build the Composite
   * Key.
   *
   * @param clazz the class of the entity
   * @return a lambda that creates a CompositeKey for an object instance
   */
  private EntityKeyProvider createEntityKeyProvider(Class<?> clazz) {
    EntityMeta em = metaInventory.getForClass(clazz);

    if (em == null) {
      throw new MissingKeysException("Entity metadata not found for class: " + clazz.getName());
    }

    List<Field> fields = new ArrayList<>();
    List<AttributeMapper> mappers = new ArrayList<>();
    PrimaryKeyDescriptor pk = em.getPrimaryKeyDescriptor();
    if (pk == null || pk.getAttributeDescriptiors() == null || pk.getAttributeDescriptiors().isEmpty()) {
      throw new MissingKeysException("No primary key descriptors found for class: " + clazz.getName());
    }
    List<AttributeDescriptor> pkAttributeDescriptors = pk.getAttributeDescriptiors();
    for (AttributeDescriptor descriptor : pkAttributeDescriptors) {
      try {
        Field field = clazz.getDeclaredField(descriptor.getAttrFieldName());
        field.setAccessible(true);
        fields.add(field);
      } catch (Exception e) {
        throw new MissingKeysException("Error accessing fields for class: " + clazz.getName(), e);
      }
      if (descriptor.getAttributeMapperDescriptor() == null) {
        mappers.add(null);
        continue;
      }
      String mapperName = descriptor.getAttributeMapperDescriptor().getMapperName();
      try {
        Class<?> mapperClass = Class.forName(mapperName);
        AttributeMapper mapper = (AttributeMapper) mapperClass.getDeclaredConstructor().newInstance();
        mappers.add(mapper);
      } catch (ReflectiveOperationException e) {
        logger.error("Failed to instantiate mapper: {}", mapperName, e);
        throw new MissingKeysException("Error instantiating mapper: " + mapperName, e);
      }
    }

    return (Object entity) -> {
      List<String> keyParts = new ArrayList<>();
      for (int i = 0; i < fields.size(); i++) {
        try {
          String value = fields.get(i).get(entity).toString();
          if (mappers.get(i) != null) {
            keyParts.add(mappers.get(i).apply(value));
          } else {
            keyParts.add(value);
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Could not access field value on entity", e);
        }
      }
      return new CompositeKey(clazz.getName(), keyParts).toString();
    };
  }

  public String createCompositeKey(final Class<?> clazz) {
    return new CompositeKey(getType(clazz)).toString();
  }

  public String createCompositeKey(Class<?> clazz, Object... keyParts) {
    return new CompositeKey(getType(clazz), mapKeyPartsToString(clazz, keyParts)).toString();
  }

  public EntityMetadataInventory getMetaDataInventory() {
    return metaInventory;
  }
}
