package com.lookingdev.stackoverflow.Domain.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperDTOModel {

    private String platform;
    private String username;
    private String profileUrl;
    private Integer reputation;
    private List<String> skills;
    private String location;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastActivityDate;
}
