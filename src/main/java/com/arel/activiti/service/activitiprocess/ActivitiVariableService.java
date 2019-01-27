package com.arel.activiti.service.activitiprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActivitiVariableService implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) {

        Map<String, Object> variablesProcess = new HashMap<>();
        Map<String, String> nameList = new HashMap<>();
        nameList.put("1", "Sağlık Problemi");
        nameList.put("2", "Tatil");
        nameList.put("3", "Diğer Nedenler");
        variablesProcess.put("reasonList", nameList);
        execution.setVariables(variablesProcess);

        System.out.println("Hello tasks");
    }
}