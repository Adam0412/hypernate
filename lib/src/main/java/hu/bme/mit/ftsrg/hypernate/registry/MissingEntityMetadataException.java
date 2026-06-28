/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.registry;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import lombok.experimental.StandardException;

/** Thrown when metadata for a specific entity class cannot be found. */
@StandardException
public class MissingEntityMetadataException extends HypernateException {}
