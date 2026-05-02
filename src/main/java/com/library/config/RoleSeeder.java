package com.library.config;

import com.library.model.Role;
import com.library.model.RoleType;
import com.library.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleSeeder
{

    @Bean
    public CommandLineRunner seedRoles(RoleRepository roleRepository)
    {
        return args -> {
            if (roleRepository.findByRoleName(RoleType.USER).isEmpty())
            {
                roleRepository.save(new Role(RoleType.USER));
            }

            if (roleRepository.findByRoleName(RoleType.ADMIN).isEmpty())
            {
                roleRepository.save(new Role(RoleType.ADMIN));
            }
        };
    }
}