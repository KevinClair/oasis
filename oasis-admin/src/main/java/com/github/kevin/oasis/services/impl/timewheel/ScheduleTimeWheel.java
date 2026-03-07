package com.github.kevin.oasis.services.impl.timewheel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简化版哈希时间轮。
 * 作用：将未来一段时间内的任务按 tick 放入槽位，tick 线程只处理当前槽，降低每秒全表扫描压力。
 */
public class ScheduleTimeWheel {

    private final long tickMs;
    private final int slotCount;
    private final List<ConcurrentLinkedQueue<ScheduleWheelTask>> slots;
    private final AtomicLong cursor = new AtomicLong(0);

    /**
     * 任务去重（同一个 jobId + expectedNextTriggerTime + expectedVersion 仅入轮一次）。
     */
    private final ConcurrentHashMap<String, Boolean> dedupe = new ConcurrentHashMap<>();

    public ScheduleTimeWheel(long tickMs, int slotCount) {
        this.tickMs = Math.max(100L, tickMs);
        this.slotCount = Math.max(8, slotCount);
        this.slots = new ArrayList<>(this.slotCount);
        for (int i = 0; i < this.slotCount; i++) {
            slots.add(new ConcurrentLinkedQueue<>());
        }
    }

    public void addTask(ScheduleWheelTask task, long now) {
        if (task == null || task.getJobId() == null || task.getExpectedNextTriggerTime() == null || task.getExpectedVersion() == null) {
            return;
        }
        String key = keyOf(task);
        if (dedupe.putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }

        long delayMs = Math.max(0L, task.getExpectedNextTriggerTime() - now);
        long ticks = delayMs == 0 ? 0 : ((delayMs + tickMs - 1) / tickMs);
        long scheduledTick = cursor.get() + ticks;
        int slotIdx = (int) Math.floorMod(scheduledTick, slotCount);
        int rounds = (int) (ticks / slotCount);

        task.setRounds(rounds);
        slots.get(slotIdx).offer(task);
    }

    public List<ScheduleWheelTask> pollDueTasks(long now) {
        int slotIdx = (int) Math.floorMod(cursor.getAndIncrement(), slotCount);
        ConcurrentLinkedQueue<ScheduleWheelTask> queue = slots.get(slotIdx);

        List<ScheduleWheelTask> due = new ArrayList<>();
        int loop = queue.size();
        for (int i = 0; i < loop; i++) {
            ScheduleWheelTask task = queue.poll();
            if (task == null) {
                continue;
            }

            if (task.getRounds() > 0) {
                task.setRounds(task.getRounds() - 1);
                queue.offer(task);
                continue;
            }

            if (task.getExpectedNextTriggerTime() > now) {
                // 兜底：若由于时间漂移导致任务未到期，重新回到时间轮，避免提前触发。
                requeue(task, now);
                continue;
            }

            dedupe.remove(keyOf(task));
            due.add(task);
        }
        return due;
    }

    public int getDedupSize() {
        return dedupe.size();
    }

    private void requeue(ScheduleWheelTask task, long now) {
        long delayMs = Math.max(0L, task.getExpectedNextTriggerTime() - now);
        long ticks = delayMs == 0 ? 0 : ((delayMs + tickMs - 1) / tickMs);
        long scheduledTick = cursor.get() + ticks;
        int slotIdx = (int) Math.floorMod(scheduledTick, slotCount);
        int rounds = (int) (ticks / slotCount);
        task.setRounds(rounds);
        slots.get(slotIdx).offer(task);
    }

    private String keyOf(ScheduleWheelTask task) {
        return task.getJobId() + ":" + task.getExpectedNextTriggerTime() + ":" + task.getExpectedVersion();
    }
}
