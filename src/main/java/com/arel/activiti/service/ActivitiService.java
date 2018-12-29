package com.arel.activiti.service;

import com.arel.activiti.model.model.*;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
public class ActivitiService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final IdentityService identityService;
    private final FormService formService;
    private final UserService userService;
    private Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    public ActivitiService(RepositoryService repositoryService, RuntimeService runtimeService, TaskService taskService, IdentityService identityService, FormService formService, UserService userService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.identityService = identityService;
        this.formService = formService;
        this.userService = userService;
    }

    public List<ProcessDefinitionDto> getProcessIdList() {
        UsernamePasswordAuthenticationToken accountCredentials = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (accountCredentials.getAuthorities().stream().anyMatch(item -> item.getAuthority().contains("Administrator"))) {
            return repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .active().list().stream().map(item -> convertTo(item)).collect(Collectors.toList());
        } else {
            return repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .active().list().stream()
                    .filter(item -> accountCredentials.getAuthorities().stream()
                            .anyMatch(role -> role.getAuthority().contains(item.getCategory()) || item.getCategory().contains("All")))
                    .map(item -> convertTo(item)).collect(Collectors.toList());
        }
    }

    public boolean setCategory(String processId, String category) {
        try {
            repositoryService.setProcessDefinitionCategory(processId, category);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean uploadFile(String resourceName, InputStream processStream) {
        try {
            repositoryService.createDeployment().addInputStream(resourceName, processStream).enableDuplicateFiltering().category("test").deploy();
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
    }

    private ProcessDefinitionDto convertTo(ProcessDefinition processDefinition) {
        ProcessDefinitionDto processDefinitionDto = new ProcessDefinitionDto();
        copyProperties(processDefinition, processDefinitionDto);
        return processDefinitionDto;
    }

    public ProcessInstanceDto startProcess(String id) {
        UsernamePasswordAuthenticationToken accountCredentials = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        //Fix that area
        //Map<String, String> variables = new HashMap<>();
        //variables.put("name", userService.getUser(accountCredentials.getPrincipal().toString()).getName());

        Map<String, Object> variablesProcess = new HashMap<>();
        variablesProcess.put("initialPerson", accountCredentials.getPrincipal().toString());
        variablesProcess.put("leadPerson", getLeadUsername(accountCredentials));
        variablesProcess.put("userName", userService.getUser(accountCredentials.getPrincipal().toString()).getName());
        variablesProcess.put("name", userService.getUser(accountCredentials.getPrincipal().toString()).getName());

       /* Map<String, String> variablesValues = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : variablesProcess.entrySet()) {
            if (entry.getValue() instanceof String) {
                variablesValues.put(entry.getKey(), (String) entry.getValue());
            }
        }
        variablesValues.put("name", userService.getUser(accountCredentials.getPrincipal().toString()).getName());*/
        ProcessInstanceDto processInstanceDto = convertToProcessInstance(runtimeService.startProcessInstanceById(id, variablesProcess));
        //formService.submitStartFormData(processInstanceDto.getId(), variablesValues);
        return processInstanceDto;

    }

    private String getLeadUsername(UsernamePasswordAuthenticationToken accountCredentials) {
        UserDetails activeUser = userService.loadUserByUsername(accountCredentials.getPrincipal().toString());
        List<UserDetails> userDetailsList = userService.getAllUsers();
        userDetailsList = userDetailsList.stream().filter(item -> item.getUsername().equals(activeUser.getUsername())).collect(Collectors.toList());
        UserDetails leadUser = userDetailsList.get(0);
        return leadUser.getUsername();
    }

    private ProcessInstanceDto convertToProcessInstance(ProcessInstance processInstance) {
        ProcessInstanceDto processInstanceDto = new ProcessInstanceDto();
        copyProperties(processInstance, processInstanceDto);
        return processInstanceDto;
    }

    private TaskDto convertToTask(Task task) {
        TaskDto taskDto = new TaskDto();
        try {
            task.setDescription(new String(task.getDescription().getBytes(), "UTF-8"));
            copyProperties(task, taskDto);
            taskDto.setTaskLocalVariables(this.setFromData(formService.getTaskFormData(task.getId()).getFormProperties()));
            taskDto.setProcessVariables(taskService.getVariables(task.getId()));
            taskDto.setCommentList(this.findAllComment(task.getId()));
            taskDto.setAttachmentList(this.findTaskAttachment(task.getId()));
            return taskDto;
        } catch (Exception ex) {
            ex.printStackTrace();
            return taskDto;
        }
    }

    private List<FormPropertyDto> setFromData(List<FormProperty> formProperties) {

        List<FormPropertyDto> formPropertyDtoList = new ArrayList<>();
        for (FormProperty formProperty : formProperties) {
            FormPropertyDto formPropertyDto = new FormPropertyDto();
            formPropertyDto.setId(formProperty.getId());
            formPropertyDto.setName(formProperty.getName());
            formPropertyDto.setType(formProperty.getType());
            formPropertyDto.setValue(formProperty.getValue());
            formPropertyDto.setReadable(formProperty.isReadable());
            formPropertyDto.setWritable(formProperty.isWritable());
            formPropertyDto.setRequired(formProperty.isRequired());
            formPropertyDtoList.add(formPropertyDto);
        }

        return formPropertyDtoList;

    }


    public List<TaskDto> findAllMyTask() {
        UsernamePasswordAuthenticationToken accountCredentials = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return taskService.createTaskQuery()
                .active()
                .taskCandidateOrAssigned(accountCredentials.getPrincipal()
                        .toString()).list().stream().map(item -> convertToTask(item)).collect(Collectors.toList());
    }

    public TaskDto findTaskById(String id) {
        return convertToTask(taskService.createTaskQuery()
                .active().taskId(id).singleResult());
    }


    public boolean addCommentByTaskId(String id, String comment) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        UsernamePasswordAuthenticationToken accountCredentials = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        try {
            identityService.setAuthenticatedUserId(accountCredentials.getPrincipal().toString());
            taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean addAttachment(String id, MultipartFile file) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();

        try {
            taskService.createAttachment("attachment", task.getId(), task.getProcessInstanceId(), file.getContentType(), file.getOriginalFilename(), file.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<Attachment> findTaskAttachment(String id) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();

        return taskService.getTaskAttachments(task.getId());

    }

    public Attachment findAttachment(String id) {
        return taskService.getAttachment(id);

    }

    public boolean taskComplete(String id, HashMap<String, Object> taskVariables) {
        try {
            taskService.setVariable(id, "ok", true);
            taskService.complete(id, taskVariables);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<CommentDto> findAllComment(String id) {
        return taskService.getTaskComments(id).stream().map(item -> convetToCommentDto(item)).collect(Collectors.toList());
    }

    public LeftMenuData leftmenuData() {
        LeftMenuData leftMenuData = new LeftMenuData();
        leftMenuData.setJobCount(String.valueOf(this.findAllMyTask().size()));
        leftMenuData.setProcessCount(String.valueOf(this.getProcessIdList().size()));
        return leftMenuData;
    }

    private CommentDto convetToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        copyProperties(comment, commentDto);
        return commentDto;
    }
}
