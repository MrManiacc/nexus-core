package nexus.engine.module

import com.google.common.base.Preconditions
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.common.collect.Table
import nexus.engine.module.naming.Name
import nexus.engine.module.naming.Version
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Implementation of ModuleRegistry based around a com.google.common.collect.Table.
 *
 * @author Immortius
 */
class TableModuleRegistry : ModuleRegistry {
    private val modules: Table<Name, Version, Module> = HashBasedTable.create()
    private val latestModules: MutableMap<Name, Module> = Maps.newHashMap()

    override fun add(element: Module): Boolean {
        Preconditions.checkNotNull(element)
        if (!modules.contains(element.id, element.version)) {
            modules.put(element.id, element.version, element)
            val previousLatest = latestModules[element.id]
            if (previousLatest == null || previousLatest.version.compareTo(element.version) <= 0) {
                latestModules[element.id] = element
            }
            return true
        } else {
            logger.error("Duplicate module {}-{} discovered",
                element.id,
                element.version)
        }
        return false
    }

    override fun remove(o: Module): Boolean {
        val module = o
        if (modules.remove(module.id, module.version) != null) {
            val latest = latestModules[module.id]
            if (latest?.version?.compareTo(module.version) == 0) {
                updateLatestFor(module.id)
            }
            return true
        }
        return false

    }

    private fun updateLatestFor(moduleId: Name) {
        var newLatest: Module? = null
        for (remainingModule in modules.row(moduleId).values) {
            if (newLatest == null || remainingModule.version.compareTo(newLatest.version) > 0) {
                newLatest = remainingModule
            }
        }
        if (newLatest != null) {
            latestModules[moduleId] = newLatest
        } else {
            latestModules.remove(moduleId)
        }
    }

    override fun removeAll(elements: Collection<Module>): Boolean {
        var result = false
        for (o in elements) {
            result = result or remove(o)
        }
        return result
    }

    override fun addAll(elements: Collection<Module>): Boolean {
        var result = false
        for (o in elements) {
            result = result or add(o)
        }
        return result
    }

    override fun retainAll(elements: Collection<Module>): Boolean {
        val modulesToRetain: MutableSet<Module?> = Sets.newHashSet()
        for (o in elements) {
            modulesToRetain.add(o as Module?)
        }
        var changed = false
        val moduleIterator = modules.values().iterator()
        while (moduleIterator.hasNext()) {
            val next = moduleIterator.next()
            if (!modulesToRetain.contains(next)) {
                moduleIterator.remove()
                changed = true
            }
        }
        if (changed) {
            for (name in modules.rowKeySet()) {
                updateLatestFor(name)
            }
        }
        return changed
    }

    override val moduleIds: Set<Any>
        get() = Sets.newLinkedHashSet(modules.rowKeySet())

    override fun getModuleVersions(id: Name): Collection<Module> =
        Collections.unmodifiableCollection(modules.row(id).values)


    override fun getLatestModuleVersion(id: Name): Module =
        latestModules[id] ?: error("failed to find module version for module name '$id'")


    override fun getLatestModuleVersion(id: Name, minVersion: Version, maxVersion: Version): Module {
        val module = latestModules[id]
        if (module != null) {
            if (module.version.compareTo(maxVersion) < 0 && module.version.compareTo(minVersion) >= 0) {
                return module
            }
            if (module.version.compareTo(minVersion) >= 0) {
                var result: Module? = null
                for ((key, value) in modules.row(id)) {
                    if (key.compareTo(minVersion) >= 0 && key.compareTo(maxVersion) < 0 && (result == null || key.compareTo(
                            result.version) > 0)
                    ) {
                        result = value
                    }
                }
                if (result == null)
                    error("failed to get latest module version for module named '$id'")
                return result
            }
        }
        error("failed to get latest module for module named '$id' in range '$minVersion'-'$maxVersion'")
    }

    override fun getModule(moduleId: Name, version: Version): Module {
        return modules[moduleId, version] ?: error("Failed to get module '$moduleId' for version '$version'")
    }

    /**
     * Returns the size of the collection.
     */
    override val size: Int
        get() = modules.size()

    override fun isEmpty(): Boolean {
        return modules.isEmpty()
    }

    override operator fun contains(element: Module): Boolean =
        modules.contains(element.id, element.version)


    override fun iterator(): MutableIterator<Module> {
        val it = modules.values().iterator()
        return object : MutableIterator<Module> {
            private var current: Module? = null
            override fun hasNext(): Boolean {
                return it.hasNext()
            }

            override fun next(): Module {
                current = it.next()
                return current!!
            }

            override fun remove() {
                it.remove()
                val latest = latestModules[current?.id]
                if (current?.version?.let { it1 -> latest?.version?.compareTo(it1) } == 0) {
                    updateLatestFor(current!!.id)
                }
            }
        }
    }

    fun toArray(): Array<Module> {
        return modules.values().toTypedArray()
    }

    override fun containsAll(elements: Collection<Module>): Boolean {
        for (o in elements) {
            if (!contains(o)) {
                return false
            }
        }
        return true
    }

    override fun clear() {
        modules.clear()
        latestModules.clear()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TableModuleRegistry::class.java)
    }
}
