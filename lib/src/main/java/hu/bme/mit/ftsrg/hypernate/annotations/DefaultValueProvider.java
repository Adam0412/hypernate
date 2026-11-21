/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.annotations;

/**
 * Strategy interface for providing programmatic default values during entity upcasting.
 *
 * <p>Implementations must provide a public no-argument constructor. Because chaincode execution
 * must be deterministic across all validating peers, implementations must produce consistent,
 * reproducible values for identical entity states.
 *
 * @param <T>the type of the default value produced
 */
@FunctionalInterface
public interface DefaultValueProvider<T> {
  T get();
}
