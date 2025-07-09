package com.example.tasks.controllers;

import com.example.tasks.domain.dto.TaskDto;
import com.example.tasks.domain.entities.Task;
import com.example.tasks.mappers.TaskMapper;
import com.example.tasks.services.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path="/api/task-lists/{task_list_id}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public List<TaskDto> listTasks(@PathVariable("task_list_id") UUID taskListId) {
        return taskService.listTasks(taskListId).stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @PostMapping
    public TaskDto createTask(@PathVariable("task_list_id") UUID taskListId, @RequestBody TaskDto taskDto){
    Task createdTask = taskService.createTask(taskListId, taskMapper.fromDto(taskDto));
    return taskMapper.toDto(createdTask);
    }
}
