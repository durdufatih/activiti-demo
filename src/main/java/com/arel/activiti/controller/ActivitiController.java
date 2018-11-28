package com.arel.activiti.controller;

import com.arel.activiti.model.model.GeneralResponse;
import com.arel.activiti.model.model.LeftMenuData;
import com.arel.activiti.model.model.ProcessDefinitionDto;
import com.arel.activiti.model.model.ProcessInstanceDto;
import com.arel.activiti.service.ActivitiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "activiti")
public class ActivitiController {

    private final ActivitiService activitiService;
    private Logger logger = LoggerFactory.getLogger(ActivitiController.class);

    public ActivitiController(ActivitiService activitiService) {
        this.activitiService = activitiService;
    }

    @GetMapping("list")
    public ResponseEntity<List<ProcessDefinitionDto>> getProcessList() {
        List<ProcessDefinitionDto> processDefinitionList = activitiService.getProcessIdList();
        if (processDefinitionList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(processDefinitionList);
    }

    @PostMapping("upload")
    public ResponseEntity<?> uploadProcess(@RequestParam("category") String category, @RequestParam("process") MultipartFile file) {
        try {
            return ResponseEntity.ok(new GeneralResponse("Upload Success", activitiService.uploadFile(file.getOriginalFilename(), file.getInputStream())));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.ok(new GeneralResponse("Upload Fail", false));
        }
    }

    @GetMapping("category/{id}/{categoryName}")
    public ResponseEntity<?> setCategory(@PathVariable("categoryName") String categoryName, @PathVariable("id") String id) {
        if (activitiService.setCategory(id, categoryName))
            return ResponseEntity.ok(new GeneralResponse("Fail", false));

        return ResponseEntity.ok(new GeneralResponse("Success", true));
    }

    @GetMapping(value = "/process/{id}")
    public ResponseEntity<ProcessInstanceDto> startProcessInstance(@PathVariable String id) {
        return ResponseEntity.ok(activitiService.startProcess(id));
    }

    @GetMapping(value = "/left")
    public ResponseEntity<LeftMenuData> leftMenuData() {
        return ResponseEntity.ok(activitiService.leftmenuData());

    }
}
