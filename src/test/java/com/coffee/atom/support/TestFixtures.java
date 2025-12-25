package com.coffee.atom.support;

import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.Section;

import java.time.LocalDate;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static Area area(Long id) {
        return Area.builder()
                .id(id)
                .areaName("area-" + id)
                .latitude(1.0)
                .longitude(2.0)
                .build();
    }

    public static Section section(Long id, Area area, boolean approved) {
        return Section.builder()
                .id(id)
                .sectionName("section-" + id)
                .latitude(1.0)
                .longitude(2.0)
                .area(area)
                .isApproved(approved)
                .build();
    }

    public static AppUser user(Long id, Role role) {
        return AppUser.builder()
                .id(id)
                .userId("userId-" + id)
                .username("username-" + id)
                .password("pw-" + id)
                .salt("salt-" + id)
                .role(role)
                .isApproved(Boolean.TRUE)
                .build();
    }

    public static AppUser viceAdmin(Long id, Role role, Area area) {
        return AppUser.builder()
                .id(id)
                .userId("userId-" + id)
                .username("username-" + id)
                .password("pw-" + id)
                .salt("salt-" + id)
                .role(role)
                .area(area)
                .isApproved(Boolean.TRUE)
                .build();
    }

    public static AppUser villageHead(Long id, Section section, boolean approved) {
        return AppUser.builder()
                .id(id)
                .userId("vh-" + id)
                .username("village-head-" + id)
                .password("pw-" + id)
                .salt("salt-" + id)
                .role(Role.VILLAGE_HEAD)
                .section(section)
                .isApproved(approved)
                .build();
    }

    public static Farmer farmer(Long id, AppUser villageHead, boolean approved) {
        return Farmer.builder()
                .id(id)
                .name("farmer-" + id)
                .villageHead(villageHead)
                .identificationPhotoUrl("https://example.com/farmer/" + id + ".jpg")
                .isApproved(approved)
                .build();
    }

    public static Purchase purchase(Long id, AppUser manager, AppUser villageHead, boolean approved) {
        return Purchase.builder()
                .id(id)
                .manager(manager)
                .villageHead(villageHead)
                .purchaseDate(LocalDate.of(2025, 1, 1))
                .quantity(10L)
                .unitPrice(100L)
                .totalPrice(1000L)
                .deduction(0L)
                .paymentAmount(1000L)
                .remarks("memo")
                .isApproved(approved)
                .build();
    }
}


