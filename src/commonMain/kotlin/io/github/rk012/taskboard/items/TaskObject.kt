package io.github.rk012.taskboard.items

import io.github.rk012.taskboard.TaskStatus
import io.github.rk012.taskboard.exceptions.*
import kotlinx.datetime.LocalDateTime

sealed class TaskObject(var name: String, val id: String, var time: LocalDateTime) {
    var status: TaskStatus = TaskStatus.NOT_STARTED
        protected set

    internal val labels = mutableListOf<String>()
    protected val dependencyList = mutableListOf<TaskObject>() // Other TaskObjects this depends on
    private val dependents = mutableListOf<TaskObject>() // Other TaskObjects that depend on this

    init {
        update()
    }

    fun getLabels() = labels.toList()

    fun getDependencies() = dependencyList.toList()

    fun getDependents() = dependents.toList()

    fun addDependency(other: TaskObject) {
        if (dependencyList.contains(other)) throw DependencyAlreadyExistsException()
        if (other.hasDependency(this)) throw CircularDependencyException()

        dependencyList.add(other)
        other.dependents.add(this)
        update()
    }

    fun hasDependency(other: TaskObject): Boolean {
        if (dependencyList.contains(other)) return true

        dependencyList.forEach {
            if (it.hasDependency(other)) return true
        }

        return false
    }

    fun removeDependency(other: TaskObject) {
        if (!dependencyList.remove(other)) throw NoSuchDependencyException()

        other.dependents.remove(this)
        update()
    }

    internal fun getDependentSet(): Set<TaskObject> {
        val dependentSet = dependents.toMutableSet()

        dependents.forEach {
            dependentSet.addAll(it.getDependentSet())
        }

        return dependentSet
    }

    internal fun delink() {
        // List needs to be copied since the removeDependency/removeDependent function modifies the original list
        dependencyList.toList().forEach {
            removeDependency(it)
        }

        dependents.toList().forEach {
            it.removeDependency(this)
        }

        update()
    }

    protected fun update() {
        updateSelf()

        dependents.forEach {
            it.update()
        }
    }

    protected abstract fun updateSelf()
}