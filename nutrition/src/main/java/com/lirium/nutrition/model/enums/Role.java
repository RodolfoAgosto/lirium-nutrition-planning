package com.lirium.nutrition.model.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public enum Role {

    PATIENT(
            List.of(
                    new SimpleGrantedAuthority("food.read"),
                    new SimpleGrantedAuthority("record.read"),
                    new SimpleGrantedAuthority("record.write"),
                    new SimpleGrantedAuthority("record.delete"),
                    new SimpleGrantedAuthority("plan.read"),
                    new SimpleGrantedAuthority("patient.read"),
                    new SimpleGrantedAuthority("patient.update")
            )
    ),

    NUTRITIONIST(
            List.of(
                    new SimpleGrantedAuthority("user.create"),
                    new SimpleGrantedAuthority("user.read"),
                    new SimpleGrantedAuthority("user.update"),
                    new SimpleGrantedAuthority("patient.read"),
                    new SimpleGrantedAuthority("patient.create"),
                    new SimpleGrantedAuthority("patient.update"),
                    new SimpleGrantedAuthority("plan.read"),
                    new SimpleGrantedAuthority("plan.write"),
                    new SimpleGrantedAuthority("plan.delete"),
                    new SimpleGrantedAuthority("template.read"),
                    new SimpleGrantedAuthority("template.create"),
                    new SimpleGrantedAuthority("template.update"),
                    new SimpleGrantedAuthority("food.read"),
                    new SimpleGrantedAuthority("restriction.read"),
                    new SimpleGrantedAuthority("restriction.create"),
                    new SimpleGrantedAuthority("restriction.update"),
                    new SimpleGrantedAuthority("record.read")
            )
    ),

    ADMIN(
            List.of(
                    new SimpleGrantedAuthority("user.create"),
                    new SimpleGrantedAuthority("user.read"),
                    new SimpleGrantedAuthority("user.update"),
                    new SimpleGrantedAuthority("user.delete"),
                    new SimpleGrantedAuthority("user.enable"),
                    new SimpleGrantedAuthority("user.validate.email"),
                    new SimpleGrantedAuthority("patient.read"),
                    new SimpleGrantedAuthority("patient.create"),
                    new SimpleGrantedAuthority("patient.update"),
                    new SimpleGrantedAuthority("plan.read"),
                    new SimpleGrantedAuthority("plan.write"),
                    new SimpleGrantedAuthority("plan.delete"),
                    new SimpleGrantedAuthority("template.read"),
                    new SimpleGrantedAuthority("template.create"),
                    new SimpleGrantedAuthority("template.update"),
                    new SimpleGrantedAuthority("template.delete"),
                    new SimpleGrantedAuthority("food.read"),
                    new SimpleGrantedAuthority("food.create"),
                    new SimpleGrantedAuthority("food.update"),
                    new SimpleGrantedAuthority("food.delete"),
                    new SimpleGrantedAuthority("restriction.read"),
                    new SimpleGrantedAuthority("restriction.create"),
                    new SimpleGrantedAuthority("restriction.update"),
                    new SimpleGrantedAuthority("record.read"),
                    new SimpleGrantedAuthority("record.write"),
                    new SimpleGrantedAuthority("record.delete")
            )
    );

    private final List<SimpleGrantedAuthority> authorities;

    Role(List<SimpleGrantedAuthority> authorities) {

        List<SimpleGrantedAuthority> allAuthorities = new ArrayList<>(authorities);

        // agrega ROLE_PATIENT, ROLE_ADMIN, etc.
        allAuthorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        this.authorities = List.copyOf(allAuthorities);
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

}