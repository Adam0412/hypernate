/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a default fallback value for a newly added entity attribute during schema evolution.
 *
 * <p>When reading legacy records on the ledger that were written before this attribute existed, the
 * upcasting mechanism will inject this default value to ensure consistent deserialization.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @SchemaVersion(2)
 * @PrimaryKey(@AttributeInfo(name = Asset.Fields.id))
 * public record Asset(
 *     String id,
 *     @DefaultValue(value = "\"active\"", since = 2) String status
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {
  String value();

  Class<? extends DefaultValueProvider<?>> provider() default None.class;

  int since();

  interface None extends DefaultValueProvider<Object> {
    @Override
    default Object get() {
      return null;
    }
  }
}
