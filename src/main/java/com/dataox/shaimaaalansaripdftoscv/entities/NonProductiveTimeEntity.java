package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "non_productive_time")
@Entity
public class NonProductiveTimeEntity {

    @Id
    public Long id;
    public Long updateAttachmentId;
    public Double hours;
    public String operationalDistribution;

}