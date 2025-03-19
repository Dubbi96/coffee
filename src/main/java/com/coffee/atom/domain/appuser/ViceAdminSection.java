package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vice_admin_section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViceAdminSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vice_admin_id", nullable = false)
    private ViceAdminDetail viceAdminDetail;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
}