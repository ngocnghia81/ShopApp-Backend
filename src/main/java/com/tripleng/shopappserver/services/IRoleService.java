package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.models.Role;

import java.util.List;

public interface IRoleService {
    List<Role> getAllRoles(); // <1>
}
