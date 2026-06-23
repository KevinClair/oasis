package com.github.kevin.oasis.services.impl.timewheel;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTimeWheelTest {

    @Test
    void addAndPollTasksInOrder() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        long now = 100000L;

        wheel.addTask(task(1L, now + 50L, 1L), now);
        wheel.addTask(task(2L, now + 150L, 1L), now);
        wheel.addTask(task(3L, now + 250L, 1L), now);

        List<ScheduleWheelTask> due = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            due.addAll(wheel.pollDueTasks(now + 100L));
        }
        assertTrue(due.stream().anyMatch(t -> Long.valueOf(1L).equals(t.getJobId())));

        for (int i = 0; i < 20; i++) {
            due.addAll(wheel.pollDueTasks(now + 300L));
        }
        assertTrue(due.stream().anyMatch(t -> Long.valueOf(2L).equals(t.getJobId())));
        assertTrue(due.stream().anyMatch(t -> Long.valueOf(3L).equals(t.getJobId())));
    }

    @Test
    void duplicateTaskIsIgnored() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        long now = 100000L;
        wheel.addTask(task(100L, now + 200L, 1L), now);
        wheel.addTask(task(100L, now + 200L, 1L), now);
        assertEquals(1, wheel.getDedupSize());
    }

    @Test
    void nullTaskIsIgnored() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        wheel.addTask(null, 0L);
        assertEquals(0, wheel.getDedupSize());
    }

    @Test
    void taskWithNullFieldsIsIgnored() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        wheel.addTask(task(null, 100L, 1L), 0L);
        wheel.addTask(task(1L, null, 1L), 0L);
        wheel.addTask(task(1L, 100L, null), 0L);
        assertEquals(0, wheel.getDedupSize());
    }

    @Test
    void pastTaskIsPolled() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        long now = 100000L;
        wheel.addTask(task(10L, now, 1L), now);

        List<ScheduleWheelTask> due = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            due.addAll(wheel.pollDueTasks(now + 10));
        }
        assertTrue(due.stream().anyMatch(t -> Long.valueOf(10L).equals(t.getJobId())));
    }

    @Test
    void futureTaskNotPolledTooEarly() {
        ScheduleTimeWheel wheel = new ScheduleTimeWheel(100L, 10);
        long now = 100000L;
        wheel.addTask(task(50L, now + 5000L, 1L), now);

        List<ScheduleWheelTask> due = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            due.addAll(wheel.pollDueTasks(now + 200));
        }
        assertTrue(due.stream().noneMatch(t -> Long.valueOf(50L).equals(t.getJobId())));
    }

    private ScheduleWheelTask task(Long jobId, Long triggerTime, Long version) {
        return ScheduleWheelTask.builder()
                .jobId(jobId)
                .expectedNextTriggerTime(triggerTime)
                .expectedVersion(version)
                .build();
    }
}
