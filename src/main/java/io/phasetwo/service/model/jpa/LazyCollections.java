package io.phasetwo.service.model.jpa;

import jakarta.persistence.EntityManager;

/** Helpers for working with LAZY associations without accidentally initializing them. */
final class LazyCollections {

  private LazyCollections() {}

  /**
   * True when {@code attribute} on {@code owner} has already been loaded.
   *
   * <p>Lets a caller keep an in-memory collection consistent without being the thing that forces it
   * to load. These associations are mapped {@code cascade = ALL, orphanRemoval = true}, so an entity
   * removed through the EntityManager while an already-loaded collection still references it can be
   * re-cascaded on flush. When the collection was never loaded there is nothing to reconcile, and
   * touching it would read every row.
   */
  static boolean isLoaded(EntityManager em, Object owner, String attribute) {
    return em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(owner, attribute);
  }
}
