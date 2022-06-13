package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "update_attachment")
@Entity
public class UpdateAttachmentEntity {

    @Id
    public Long id;
    public Long emailId;
    public String wellNo;
    public String tgTarget;
    public String profile;
    public String dsCompany;
    public String area;
    public String team;
    public String kocTeamLeader;
    public String RIG;
    public String BIT;
    public String BITHydraulics;
    public String PRM;
    public String drillingBHA;
    public String presentActivity;
    public String formation;
    public Date date;

}
