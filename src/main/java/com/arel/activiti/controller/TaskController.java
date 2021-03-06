package com.arel.activiti.controller;


import com.arel.activiti.model.model.CommentDto;
import com.arel.activiti.model.model.CommentRequest;
import com.arel.activiti.model.model.TaskDto;
import com.arel.activiti.model.model.TaskRequestDto;
import com.arel.activiti.service.ActivitiService;
import org.activiti.engine.task.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("task")
public class TaskController {

    @Autowired
    private ActivitiService activitiService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> findAllMyTask() {
        return ResponseEntity.ok(activitiService.findAllMyTask());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> findAllMyTask(@PathVariable String id) {
        return ResponseEntity.ok(activitiService.findTaskById(id));
    }

    @PostMapping("/comment")
    public ResponseEntity<Boolean> addComment(@RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(activitiService.addCommentByTaskId(commentRequest.getId(), commentRequest.getComment()));
    }

    @PostMapping("/comment/{id}")
    public ResponseEntity<List<CommentDto>> addComment(@PathVariable String id) {
        return ResponseEntity.ok(activitiService.findAllComment(id));
    }

    @PostMapping("/attachment/{id}")
    public ResponseEntity<Boolean> addAttachment(@PathVariable String id, @RequestParam("attachment") MultipartFile file) {
        return ResponseEntity.ok(activitiService.addAttachment(id, file));
    }

    @GetMapping("/attachment/{id}")
    public ResponseEntity<Attachment> getAttachment(@PathVariable String id) {
        return ResponseEntity.ok(activitiService.findAttachment(id));
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<Boolean> taskComplete(@RequestBody TaskRequestDto taskRequestDto, @PathVariable String id) {
        return ResponseEntity.ok(activitiService.taskComplete(id, taskRequestDto.getVariables()));
    }


}
