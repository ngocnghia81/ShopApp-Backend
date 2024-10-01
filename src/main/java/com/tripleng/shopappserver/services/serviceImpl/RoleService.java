package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.models.Role;
import com.tripleng.shopappserver.repositories.RoleRepository;
import com.tripleng.shopappserver.services.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
