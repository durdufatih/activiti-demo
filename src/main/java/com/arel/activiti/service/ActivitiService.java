package com.arel.activiti.service;

import com.arel.activiti.model.ProcessDefinitionDto;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
public class ActivitiService {

    private final RepositoryService repositoryService;
    private Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    public ActivitiService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<ProcessDefinitionDto> getProcessIdList() {
        SecurityContextHolder.getContext().getAuthentication();
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
}
