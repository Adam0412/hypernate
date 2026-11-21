/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.schemaregistry;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import lombok.experimental.StandardException;

@StandardException
public class SchemaDowngradeException extends HypernateException {
  public SchemaDowngradeException(String className) {
    super("Schema version on the ledger is higher than the current for class:" + className);
  }
}
