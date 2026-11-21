/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an entity attribute was renamed during a schema version transition.
 *
 * <p>This annotation is optional. In it's absence the change is considered as a DELETE and an ADD
 * so the data will be lost.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @SchemaVersion(2)
 * @PrimaryKey(@AttributeInfo(name = Asset.Fields.id))
 * public record Asset(
 *     String id,
 *     @RenamedFrom(value = "size", since = 2) int dimensions
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RenamedFrom {
  String value();

  int since();
}
