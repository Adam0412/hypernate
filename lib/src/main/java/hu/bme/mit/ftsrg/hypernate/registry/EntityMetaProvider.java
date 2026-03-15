package hu.bme.mit.ftsrg.hypernate.registry;

import com.jcabi.aspects.Loggable;
import hu.bme.mit.ftsrg.hypernate.annotations.AttributeInfo;
import hu.bme.mit.ftsrg.hypernate.annotations.PrimaryKey;
import hu.bme.mit.ftsrg.hypernate.util.JSON;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(Loggable.DEBUG)
public class EntityMetaProvider {
  private static final Logger logger = LoggerFactory.getLogger(EntityMetaProvider.class);
  private EntityMetaInventory metaInventory = new EntityMetaInventory();

  public EntityMetaProvider() {

  }

  <T> String getType(final T entity) {
    return getType(entity.getClass());
  }

  <T> String getType(final Class<T> clazz) {
    return clazz.getName().toUpperCase();
  }

  <T> int getPrimaryKeyCount(final Class<T> clazz) {
    return clazz.getAnnotation(PrimaryKey.class) != null
        ? clazz.getAnnotation(PrimaryKey.class).value().length
        : 0;
  }

  <T> String[] getPrimaryKeys(final T entity) {
    return Arrays.stream(getPrimaryKeyAnnot(entity.getClass()).value())
        .map(
            attrInfo -> {
              logger.debug("Processing primary key attribute {}", attrInfo.name());
              final Object value = getFieldValueForAttr(entity, attrInfo);
              final String mappedKey = applyAttrMapper(attrInfo, value);
              logger.debug(
                  "Result of primary key mapping for attribute {} is {}",
                  attrInfo.name(),
                  mappedKey);
              return mappedKey;
            })
        .toArray(String[]::new);
  }

  <T> String[] mapKeyPartsToString(final T entity, final Object... keyParts) {
    return mapKeyPartsToString(entity.getClass(), keyParts);
  }

  <T> String[] mapKeyPartsToString(final Class<T> clazz, final Object... keyParts) {
    final AttributeInfo[] attrInfos = getPrimaryKeyAnnot(clazz).value();
    return IntStream.range(0, Math.min(attrInfos.length, keyParts.length))
        .mapToObj(i -> applyAttrMapper(attrInfos[i], keyParts[i]))
        .toArray(String[]::new);
  }

  <T> byte[] toBuffer(final T entity) {
    return toJson(entity).getBytes(StandardCharsets.UTF_8);
  }

  <T> T fromBuffer(final byte[] buffer, final Class<T> clazz) {
    final String json = new String(buffer, StandardCharsets.UTF_8);
    logger.debug("Parsing entity from JSON: {}", json);
    return JSON.deserialize(json, clazz);
  }

  <T> String toJson(final T entity) {
    return JSON.serialize(entity);
  }

  private <T> PrimaryKey getPrimaryKeyAnnot(final Class<T> clazz) {
    final PrimaryKey pk = clazz.getAnnotation(PrimaryKey.class);
    if (pk == null) {
      throw new MissingPrimaryKeysException(
          String.format("%s does not have a primary key annotation", clazz));
    }

    return pk;
  }

  private String applyAttrMapper(final AttributeInfo attrInfo, final Object keyPart) {
    Class<? extends Function<Object, String>> mapperClass = attrInfo.mapper();
    Constructor<? extends Function<Object, String>> mapperCtor;
    try {
      mapperCtor = mapperClass.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      logger.error("Could not find no-arg constructor for mapper {}", mapperClass.getName());
      throw new RuntimeException(e);
    }

    Function<Object, String> mapper;
    try {
      mapper = mapperCtor.newInstance();
    } catch (InstantiationException e) {
      logger.error("Failed to instantiate mapper {}", mapperClass.getName());
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      logger.error("Could not access constructor for mapper {}", mapperClass.getName());
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      logger.error(
          "An exception was thrown by the constructor of mapper {}", mapperClass.getName());
      throw new RuntimeException(e);
    }
    logger.trace(
        "Successfully instantiated mapper of type {} for primary key attribute {}",
        mapperClass.getName(),
        attrInfo.name());

    return mapper.apply(keyPart);
  }

  private <T> Object getFieldValueForAttr(final T ent, final AttributeInfo attrInfo) {
    final Field field;
    try {
      field = ent.getClass().getDeclaredField(attrInfo.name());
    } catch (NoSuchFieldException e) {
      logger.error(
          "Could not find field {} in class {}", attrInfo.name(), ent.getClass().getName());
      throw new RuntimeException(e);
    }
    field.setAccessible(true);
    logger.trace("Found field for primary key attribute {}", attrInfo.name());

    final Object value;
    try {
      value = field.get(ent);
    } catch (IllegalAccessException e) {
      logger.error(
          "Could not access field {} in class {}", field.getName(), ent.getClass().getName());
      throw new RuntimeException(e);
    }

    return value;
  }
}
