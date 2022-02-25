package io.github.rk012.taskboard

import io.github.rk012.taskboard.exceptions.NoSuchLabelException
import io.github.rk012.taskboard.Taskboard.SortOptions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

class TaskboardTest {
    private lateinit var tb: Taskboard

    @BeforeEach
    fun createTaskboard() { tb = Taskboard("Test Taskboard") }

    @Test
    fun removeTest() {
        val t = tb.createTask("Test Task")

        assertTrue(tb.removeObject(t))
        assertFalse(tb.removeObject(t))
    }

    @Test
    fun filterOrderTest() {
        val t0 = tb.createTask("Task 0")
        val t1 = tb.createTask("Task 1")
        val t2 = tb.createTask("Task 2")

        t0.addDependency(t1)
        t1.addDependency(t2)

        assertEquals(
            listOf(t2, t1, t0),
            tb.query(sortOptions = listOf(SortOptions.DEPENDENTS))
        )

        assertEquals(
            listOf(t0, t1, t2),
            tb.query(sortOptions = listOf(SortOptions.NAME))
        )

        assertDoesNotThrow { tb.query(sortOptions = listOf(SortOptions.NAME, SortOptions.DEPENDENTS)) }
    }

    @Test
    fun idTest() {
        val t0 = tb.createTask("Test Task")
        assertEquals(8, t0.id.length)
        assertEquals(t0, tb[t0.id])
    }

    @Test
    fun labelTest() {
        val t0 = tb.createTask("Task 0")
        val t1 = tb.createTask("Task 1")
        val t2 = tb.createTask("Task 2")

        assertFalse(tb.hasLabel("Label 0"))

        assertTrue(tb.createLabel("Label 0"))

        assertTrue(tb.hasLabel("Label 0"))
        assertFalse(tb.createLabel("Label 0"))

        tb.createLabel("Label 1")
        tb.createLabel("Label 2")

        assertTrue(t0.labels.isEmpty())

        assertFalse(tb.addLabel(t0, "Nonexistent Label"))
        assertTrue(tb.addLabel(t0, "Label 0"))

        assertEquals(listOf("Label 0"), t0.labels)

        tb.addLabel(t1, "Label 0")
        tb.addLabel(t1, "Label 1")
        tb.addLabel(t1, "Label 2")
        tb.addLabel(t2, "Label 1")

        assertEquals(listOf("Label 0", "Label 1", "Label 2"), t1.labels)

        assertEquals(listOf(t0, t1, t2), tb.query())
        assertEquals(listOf(t0, t1, t2), tb.query(includeLabels = listOf("Label 0", "Label 1")))
        assertEquals(listOf(t0, t1), tb.query(includeLabels = listOf("Label 0")))
        assertEquals(
            listOf(t0, t2),
            tb.query(
                includeLabels = listOf("Label 0", "Label 1"),
                excludeLabels = listOf("Label 2")
            )
        )

        val t3 = tb.createTask("Task 3")

        tb.addLabel(t3, "Label 0")

        t0.addDependency(t3)
        t1.addDependency(t3)
        t2.addDependency(t1)
        t2.addDependency(t0)

        assertEquals(
            listOf(t3, t0, t1),
            tb.query(
                sortOptions = listOf(SortOptions.DEPENDENTS),
                includeLabels = listOf("Label 0")
            )
        )

        assertThrows<NoSuchLabelException> { tb.query(includeLabels = listOf("Nonexistent")) }
        assertThrows<NoSuchLabelException> { tb.query(excludeLabels = listOf("Nonexistent")) }

        assertFalse(tb.removeLabel(t1, "Nonexistent"))
        assertTrue(tb.removeLabel(t1, "Label 2"))

        assertFalse(t1.labels.contains("Label 2"))

        assertFalse(tb.deleteLabel("Nonexistent"))
        assertTrue(tb.deleteLabel("Label 0"))

        assertFalse(t0.labels.contains("Label 0"))
        assertFalse(tb.hasLabel("Label 0"))
    }
}