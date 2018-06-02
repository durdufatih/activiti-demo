package com.arel.activiti;

import com.arel.common.Detail;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class MyController {

    @Autowired
    private MyService myService;

    @Autowired
    private Detail detail;


    @RequestMapping(value = "/processList")
    public List<String> processList() {
        return myService.processList();
    }

    @GetMapping("/process/variables")
    public List<FormProperty> getVaraibles(@RequestParam String id) {
        return myService.getProcessVariables(id);
    }

    @RequestMapping(value = "/process")
    public String startProcessInstance(@RequestParam String id, @RequestParam String assignee) {
        return myService.startProcess(id, assignee);
    }

    @RequestMapping(value = "/tasks/{assignee}")
    public String getTasks(@PathVariable("assignee") String assignee) {
        List<Task> tasks = myService.getTasks(assignee);
        return tasks.toString();
    }

    @RequestMapping(value = "/completetask")
    public String completeTask(@RequestParam String id) {
        myService.completeTask(id);
        return "Task with id " + id + " has been completed!";
    }

}