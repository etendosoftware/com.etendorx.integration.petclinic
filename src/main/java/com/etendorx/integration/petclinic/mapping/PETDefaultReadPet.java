package com.etendorx.integration.petclinic.mapping;

import org.springframework.stereotype.Component;
import com.etendoerp.integration.petclinic.Pet;
import com.etendorx.entities.mapper.lib.DTOReadMapping;

/**
 * Test implementation of {@link DTOReadMapping} for the {@link Pet} entity.
 * <p>
 * This class is registered as a Spring component under the name
 * {@code PETDefaultReadPet}, and is intended only for testing and
 * experimentation with "Java mappings" within the Petclinic integration.
 * </p>
 *
 * <p>
 * The {@link #map(Pet)} method currently returns a constant value ({@code false})
 * instead of performing any real transformation. This behavior is intentional
 * for test purposes and should not be used in production.
 * </p>
 */
@Component("PETDefaultReadPet")
public class PETDefaultReadPet implements DTOReadMapping<Pet> {

  /**
   * Dummy mapping of a {@link Pet} entity.
   * <p>
   * This implementation always returns {@code false} and does not
   * perform any actual mapping. Its sole purpose is to validate
   * the mapping mechanism during tests.
   * </p>
   *
   * @param entity the {@link Pet} entity to be mapped (ignored in this implementation).
   * @return always {@code false}.
   */
  @Override
  public Object map(Pet entity) {
    return false;
  }
}
