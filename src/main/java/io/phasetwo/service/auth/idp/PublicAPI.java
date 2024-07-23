//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a class or method as part of the public API of this extension. This annotation
 * serves to clearly indicate which components of the extension are designed for external
 * use and are supported according to the project's compatibility and deprecation policies.
 *
 * <p>Elements marked with this annotation are considered stable and safe for use in production
 * environments, unless specified otherwise by the {@code unstable} attribute. Developers
 * using these APIs can expect them to follow semantically versioned paths for updates,
 * including deprecations and removals.</p>
 *
 * <h2>Usage Guidelines</h2>
 * <ul>
 *   <li><strong>Stable API:</strong> By default, APIs annotated with {@code @PublicAPI} without
 *   the {@code unstable} flag set to {@code true} are stable. These APIs are suitable for
 *   long-term use and should maintain backward compatibility according to the project's versioning
 *   policy.</li>
 *   <li><strong>Unstable API:</strong> APIs marked as unstable with {@code @PublicAPI(unstable = true)}
 *   are in a state of flux and may undergo significant changes including backwards incompatible
 *   modifications. They are intended for testing, experimental use, or to gain feedback before
 *   becoming part of the stable public API.</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface PublicAPI {
    /**
     * Indicates whether this API is unstable. Unstable APIs are subject to change
     * and may not maintain backward compatibility. Default is {@code false}, indicating
     * the API is stable and intended for widespread use in production environments.
     * <p>
     * Unstable APIs are intended for early
     * access to features for feedback and may change based on that feedback or
     * be removed in future versions.
     * </p>
     * <p>
     * Default value: {@code false}, meaning the API is stable.
     * </p>
     *
     * @return {@code true} if the API is unstable, {@code false} if it is stable.
     */
    boolean unstable() default false;
}
