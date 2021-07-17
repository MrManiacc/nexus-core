package nexus.engine.assets

import nexus.engine.resource.Name

/**
 * ResolutionStrategy is a filter used when determining what modules providing a resource with a given name to use in a particular module context.
 *
 * @author Immortius
 */
fun interface ResolutionStrategy {
    /**
     * @param modules The set of possible modules to resolve
     * @param context The module context of the resolution.
     * @return A Set of modules that are relevant given the context
     */
    fun resolve(modules: Set<Name>, context: Name): MutableSet<Name>
}

