package com.arel.activiti.service;

import com.arel.activiti.model.model.CommentDto;
import com.arel.activiti.model.model.ProcessDefinitionDto;
import com.arel.activiti.model.model.ProcessInstanceDto;
import com.arel.activiti.model.model.TaskDto;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    private Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    public ActivitiService(RepositoryService repositoryService, RuntimeService runtimeService, TaskService taskService, IdentityService identityService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.identityService = identityService;
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
                            .anyMatch(role -> role.getAuthority().contains(item.getCategory())))
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
        Map<String, Object> variables = new HashMap<>();
        variables.put("initialPerson", accountCredentials.getPrincipal().toString());
        return convertToProcessInstance(runtimeService.startProcessInstanceById(id, variables));

    }

    private ProcessInstanceDto convertToProcessInstance(ProcessInstance processInstance) {
        ProcessInstanceDto processInstanceDto = new ProcessInstanceDto();
        copyProperties(processInstance, processInstanceDto);
        return processInstanceDto;
    }

    private TaskDto convertToTask(Task task) {
        TaskDto taskDto = new TaskDto();
        copyProperties(task, taskDto);

        taskDto.setCommentList(this.findAllComment(task.getId()));
        taskDto.setAttachmentList(this.findTaskAttachment(task.getId()));
        return taskDto;
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

    public List<CommentDto> findAllComment(String id) {
        return taskService.getTaskComments(id).stream().map(item -> convetToCommentDto(item)).collect(Collectors.toList());
    }

    private CommentDto convetToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        copyProperties(comment, commentDto);
        return commentDto;
    }
}
