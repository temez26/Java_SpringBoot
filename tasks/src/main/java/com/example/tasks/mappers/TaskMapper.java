package com.example.tasks.mappers;

import com.example.tasks.domain.dto.TaskDto;
import com.example.tasks.domain.entities.Task;

public interface TaskMapper {

    Task fromDto(TaskDto taskDto);

    TaskDto toDto(Task task);
}
