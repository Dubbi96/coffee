package com.coffee.atom.domain.file;

import com.coffee.atom.common.BaseTimeEntity;
import com.coffee.atom.domain.appuser.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity(name = "fileEventLog")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileEventLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id" , foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "text")
    private FileEventLogType type;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "file", columnDefinition = "text")
    private String file;

    @Column(name = "size", columnDefinition = "text")
    private String size;

    @Column(name = "is_success")
    private Boolean isSuccess = null;

    @Column(name = "number")
    private Long number;
}