package com.lookingdev.stackoverflow.Domain.Enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QueueAction {
    @JsonProperty("GET_GIT_DEV") GET_GIT_DEV,
    @JsonProperty("GET_STACK_USER") GET_STACK_USER,
    @JsonProperty("GET_ALL") GET_ALL,
    @JsonProperty("INIT_DB_GIT") INIT_DB_GIT,
    @JsonProperty("INIT_DB_STACK_OVERFLOW") INIT_DB_STACK_OVERFLOW,
    @JsonProperty("GET_INIT_STATUS_STACK_OVERFLOW") GET_INIT_STATUS_STACK_OVERFLOW
}
