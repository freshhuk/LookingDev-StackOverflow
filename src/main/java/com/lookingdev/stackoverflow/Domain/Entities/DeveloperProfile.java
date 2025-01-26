package com.lookingdev.stackoverflow.Domain.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "developers")
public class DeveloperProfile {

    @Id
    private String id;

    @Field
    private String platform;

    @Field
    private String username;

    @Field
    private String profileUrl;

    @Field
    private Integer reputation;

    @Field
    private String[] skills;

    @Field
    private String location;

    @Field
    private LocalDate lastActivityDate;
}
