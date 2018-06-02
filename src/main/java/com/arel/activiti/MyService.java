package com.arel.activiti;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MyService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private FormService formService;

    public String startProcess(String id, String assignee) {
        Person person = personRepository.findByName(assignee);

        Map<String, Object> variables = new HashMap<>();
        variables.put("person", person);
        runtimeService.startProcessInstanceById(id, variables);

        return processInfo();
    }

    public List<FormProperty> getProcessVariables(String id) {
        TaskFormData taskFormData = formService.getTaskFormData(id);
        return taskFormData.getFormProperties();

    }

    public List<String> processList() {
        List<String> idList = new ArrayList<>();
        for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().list()) {
            idList.add(processDefinition.getId());
        }
        return idList;
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    public void createPersons() {
        if (personRepository.findAll().size() == 0) {

            personRepository.save(new Person("John", new Date()));
            personRepository.save(new Person("David", new Date()));
            personRepository.save(new Person("Katherin", new Date()));
        }
    }

    private String processInfo() {
        List<Task> tasks = taskService.createTaskQuery().orderByTaskCreateTime().asc().list();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Number of processes definitions : "
                + repositoryService.createProcessDefinitionQuery().count() + "--> Tasks >> ");

        for (Task task : tasks) {
            stringBuilder
                    .append(task + " | Assignee: " + task.getAssignee() + " | Description: " + task.getDescription());
        }

        return stringBuilder.toString();
    }
}