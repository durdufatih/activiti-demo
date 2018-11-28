package com.arel.activiti.model.model;

import java.util.HashMap;

public class TaskRequestDto {

    private HashMap<String, Object> variables;

    public HashMap<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, Object> variables) {
        this.variables = variables;
    }
}
