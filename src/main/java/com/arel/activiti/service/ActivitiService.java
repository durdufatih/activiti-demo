package com.arel.activiti.service;

import com.arel.activiti.model.ProcessDefinitionDto;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
public class ActivitiService {

    private final RepositoryService repositoryService;
    private final IdentityService identityService;
    private Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    public ActivitiService(RepositoryService repositoryService, IdentityService identityService) {
        this.repositoryService = repositoryService;
        this.identityService = identityService;
    }

    public List<ProcessDefinitionDto> getProcessIdList() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion().active().list().stream().map(item -> convertTo(item)).collect(Collectors.toList());
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
