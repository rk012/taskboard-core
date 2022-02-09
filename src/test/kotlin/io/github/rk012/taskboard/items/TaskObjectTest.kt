package io.github.rk012.taskboard.items

import io.github.rk012.taskboard.TaskStatus
import io.github.rk012.taskboard.exceptions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class TaskObjectTest {
    private lateinit var t0: Task
    private lateinit var t1: Task
    private lateinit var t2: Task
    private lateinit var t3: Task
    private lateinit var g0: Goal

    private lateinit var objectList: List<TaskObject>

    private fun setupDependencies() {
        /*
         *   g0
         *  |  \
         * t3  t2
         * | \ |
         * t0 t1
         * */
        g0.addDependency(t3)
        g0.addDependency(t2)
        t3.addDependency(t0)
        t3.addDependency(t1)
        t2.addDependency(t1)
    }

    @BeforeEach
    fun createObjects() {
        t0 = Task("Task 0")
        t1 = Task("Task 1")
        t2 = Task("Task 2")
        t3 = Task("Task 3")
        g0 = Goal("Goal 0")

        objectList = listOf(t0, t1, t2, t3, g0)
    }

    @Test
    fun idTest() {
        objectList.forEachIndexed { i, taskObject0 ->
            objectList.forEachIndexed { j, taskObject1 ->
                if (i != j) assertNotEquals(taskObject0.id, taskObject1.id)
            }
        }
    }

    @Test
    fun dependencyManagementTest() {
        assertDoesNotThrow {
            setupDependencies()
        }

        assertTrue(g0.hasDependency(t3))
        assertTrue(g0.hasDependency(t2))
        assertTrue(t3.hasDependency(t0))
        assertTrue(t3.hasDependency(t1))
        assertTrue(t2.hasDependency(t1))

        assertDoesNotThrow {
            g0.removeDependency(t3)
            t2.removeDependency(t1)
            t3.removeDependency(t0)
            g0.removeDependency(t2)
            t3.removeDependency(t1)
        }

        assertFalse(g0.hasDependency(t3))
        assertFalse(g0.hasDependency(t2))
        assertFalse(t2.hasDependency(t0))
        assertFalse(t3.hasDependency(t1))
        assertFalse(t2.hasDependency(t1))
    }

    @Test
    fun completionStatusTest() {
        setupDependencies()

        objectList.forEach {
            if (it == t0 || it == t1) {
                assertEquals(TaskStatus.IN_PROGRESS, it.status)
            } else {
                assertEquals(TaskStatus.NOT_STARTED, it.status)
            }
        }
        
        assertThrows<MissingTaskReqsException> { t3.markAsComplete() }
        assertThrows<MissingTaskReqsException> { t2.markAsComplete() }
        
        assertDoesNotThrow {
            t0.markAsComplete()
        }
        
        assertEquals(TaskStatus.NOT_STARTED, t3.status)
        
        assertDoesNotThrow { 
            t1.markAsComplete()
            t3.markAsComplete()
        }
        
        assertEquals(TaskStatus.COMPLETE, t0.status)
        assertEquals(TaskStatus.COMPLETE, t1.status)
        assertEquals(TaskStatus.COMPLETE, t3.status)
        
        assertEquals(TaskStatus.IN_PROGRESS, t2.status)
        assertEquals(TaskStatus.IN_PROGRESS, g0.status)
        
        assertDoesNotThrow { t2.markAsComplete() }

        assertEquals(TaskStatus.COMPLETE, t2.status)
        assertEquals(TaskStatus.COMPLETE, g0.status)
    }

    @Test
    fun exceptionTest() {
        assertThrows<NoSuchDependencyException> { t0.removeDependency(t1) }

        t0.addDependency(t1)
        assertThrows<DependencyAlreadyExistsException> { t0.addDependency(t1) }

        assertThrows<CircularDependencyException> { t1.addDependency(t0) }

        t1.addDependency(t2)
        assertThrows<CircularDependencyException> { t2.addDependency(t0) }
    }
}