package com.lookingdev.stackoverflow.Domain.Enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QueueAction {
    @JsonProperty("GET_GIT_DEV") GET_GIT_DEV,
    @JsonProperty("GET_STACK_USER") GET_STACK_USER,
    @JsonProperty("GET_ALL") GET_ALL,
    @JsonProperty("INIT_DB") INIT_DB
}
