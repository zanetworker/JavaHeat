package com.javaheat.client.models.stacks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StackData {

    private Stack stack;

    public Stack getStack() {
        return stack;
    }

    public void setStack (Stack stack) {
        this.stack = stack;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Stack {

        private String id;
        private String stack_status;

        public String getId() {
            return id;
        }

        public String getStack_status() {
            return stack_status;
        }

        public void setStack_status(String stack_status) {
            this.stack_status = stack_status;
        }
    }

}

