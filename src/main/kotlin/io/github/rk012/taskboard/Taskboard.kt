package io.github.rk012.taskboard

import io.github.rk012.taskboard.exceptions.NoSuchLabelException
import io.github.rk012.taskboard.items.Goal
import io.github.rk012.taskboard.items.Task
import io.github.rk012.taskboard.items.TaskObject
import java.util.UUID

class Taskboard(var name: String) {
    private val taskObjects = mutableMapOf<String, TaskObject>()
    private val labels = mutableListOf<String>()

    enum class SortOptions(val comparable: (TaskObject) -> Comparable<*>) {
        DEPENDENTS({ -1 * it.getDependentSet().size }), // negated for descending order
        NAME({ it.name })
    }

    private fun <T> Collection<T>.containsAny(other: Collection<T>): Boolean {
        other.forEach {
            if (contains(it)) return true
        }

        return false
    }

    operator fun get(id: String) = taskObjects[id]

    fun createTask(name: String): Task {
        var id = UUID.randomUUID().toString().split('-').joinToString("")

        if (!taskObjects.containsKey(id.substring(0..7))) {
            id = id.substring(0..7)
        }

        val task = Task(name, id)
        taskObjects[task.id] = task
        return task
    }

    fun createGoal(name: String): Goal {
        var id = UUID.randomUUID().toString().split('-').joinToString("")

        if (!taskObjects.containsKey(id.substring(0..7))) {
            id = id.substring(0..7)
        }

        val goal = Goal(name, id)
        taskObjects[goal.id] = goal
        return goal
    }

    fun removeObject(obj: TaskObject): Boolean {
        if (taskObjects.remove(obj.id) == null) return false

        obj.delink()
        return true
    }

    fun createLabel(name: String): Boolean = if (!hasLabel(name)) {
        labels.add(name)
        true
    } else false

    fun hasLabel(name: String) = labels.contains(name)

    fun addLabel(obj: TaskObject, name: String, createNew: Boolean = false): Boolean {
        if (createNew) createLabel(name)

        return if (hasLabel(name)) {
            obj.labels.add(name)
            true
        } else false
    }

    fun removeLabel(obj: TaskObject, name: String): Boolean = if (!hasLabel(name)) false else obj.labels.remove(name)

    fun deleteLabel(name: String): Boolean = if (!hasLabel(name)) false else {
        taskObjects.values.filter { it.labels.contains(name) }.forEach { it.labels.remove(name) }
        labels.remove(name)
        true
    }

    fun query(
        sortOptions: List<SortOptions> = emptyList(),
        includeLabels: List<String> = emptyList(),
        excludeLabels: List<String> = emptyList()
    ): List<TaskObject> {
        val sortComparables = mutableListOf<(TaskObject) -> Comparable<*>>()

        sortOptions.forEach {
            sortComparables.add(it.comparable)
        }

        SortOptions.values().forEach {
            if (!sortComparables.contains(it.comparable)) sortComparables.add(it.comparable)
        }

        includeLabels.forEach {
            if (!hasLabel(it)) throw NoSuchLabelException(it)
        }

        excludeLabels.forEach {
            if (!hasLabel(it)) throw NoSuchLabelException(it)
        }

        return taskObjects.values.filter {
            (includeLabels.isEmpty() || it.labels.containsAny(includeLabels)) && !it.labels.containsAny(excludeLabels)
        }.sortedWith(compareBy(*sortComparables.toTypedArray()))
    }
}