/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.metadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import hu.bme.mit.ftsrg.hypernate.annotations.KeyClass;
import hu.bme.mit.ftsrg.hypernate.annotations.KeyOrder;
import hu.bme.mit.ftsrg.hypernate.mappers.ObjectToString;
import hu.bme.mit.ftsrg.hypernate.registry.ConflictingOrderException;
import hu.bme.mit.ftsrg.hypernate.registry.MissingOrderException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class EntityMetadataInventoryTest {

  /*
   * The key classes here deliberately carry no @KeyClass: the inventory scans the classpath in its
   * static initializer, and a malformed key class there would be logged as a failure on every test
   * run. collectKeyFieldsByOrder does not need the annotation, so these are called directly.
   */
  @Nested
  class Collecting_key_fields {

    @Test
    void given_fields_declared_out_of_order_then_returns_them_sorted_by_key_order() {
      final Map<Integer, Field> byOrder =
          EntityMetadataInventory.collectKeyFieldsByOrder(OutOfOrderKey.class);

      assertEquals(List.of(1, 2), List.copyOf(byOrder.keySet()));
      assertEquals(List.of("first", "second"), fieldNames(byOrder));
    }

    @Test
    void given_field_without_key_order_then_throws_missing_order_exception() {
      final MissingOrderException e =
          assertThrows(
              MissingOrderException.class,
              () -> EntityMetadataInventory.collectKeyFieldsByOrder(KeyWithoutOrder.class));

      assertTrue(e.getMessage().contains("unordered"), e.getMessage());
    }

    @Test
    void given_two_fields_with_same_key_order_then_throws_conflicting_order_exception() {
      final ConflictingOrderException e =
          assertThrows(
              ConflictingOrderException.class,
              () -> EntityMetadataInventory.collectKeyFieldsByOrder(KeyWithDuplicateOrder.class));

      assertTrue(e.getMessage().contains("alpha"), e.getMessage());
      assertTrue(e.getMessage().contains("beta"), e.getMessage());
    }

    @Test
    void given_static_field_then_ignores_it() {
      final Map<Integer, Field> byOrder =
          EntityMetadataInventory.collectKeyFieldsByOrder(KeyWithStaticField.class);

      assertEquals(List.of("id"), fieldNames(byOrder));
    }

    @Test
    void given_synthetic_field_then_ignores_it() {
      assumeTrue(
          Arrays.stream(KeyWithSyntheticField.class.getDeclaredFields())
              .anyMatch(Field::isSynthetic),
          "compiler emitted no synthetic field for the inner class");

      final Map<Integer, Field> byOrder =
          EntityMetadataInventory.collectKeyFieldsByOrder(KeyWithSyntheticField.class);

      assertEquals(List.of("id"), fieldNames(byOrder));
    }

    private List<String> fieldNames(final Map<Integer, Field> byOrder) {
      return byOrder.values().stream().map(Field::getName).toList();
    }

    private static class OutOfOrderKey {

      @KeyOrder(2)
      String second;

      @KeyOrder(1)
      String first;
    }

    private static class KeyWithoutOrder {

      @KeyOrder(1)
      String ordered;

      String unordered;
    }

    private static class KeyWithDuplicateOrder {

      @KeyOrder(1)
      String alpha;

      @KeyOrder(1)
      String beta;
    }

    private static class KeyWithStaticField {

      static final String CONSTANT = "not a key field";

      @KeyOrder(1)
      String id;
    }

    // Non-static on purpose: javac gives inner classes a synthetic this$0 field.
    private class KeyWithSyntheticField {
      @KeyOrder(1)
      String id;
    }
  }

  @Nested
  class Registering_key_classes {

    @Test
    void given_key_class_with_static_field_then_registers_entity_fields_in_key_order() {
      final EntityDescriptor descriptor = EntityMetadataInventory.getForClass(Warehouse.class);

      assertNotNull(descriptor, "Warehouse should have been registered via WarehouseKey");
      final List<AttributeDescriptor> attributes = descriptor.primaryKey().attributes();
      assertEquals(
          List.of("locationCode", "localId"),
          attributes.stream().map(a -> a.field().getName()).toList());
      attributes.forEach(
          a -> {
            assertEquals(Warehouse.class, a.field().getDeclaringClass());
            assertEquals(ObjectToString.class, a.mapper());
          });
    }

    private static class Warehouse {

      String locationCode;
      int localId;
      int capacity;
    }

    @KeyClass(Warehouse.class)
    private record WarehouseKey(@KeyOrder(2) int localId, @KeyOrder(1) String locationCode) {

      static final String NOT_A_KEY_FIELD = "ignored";
    }
  }
}
