package edu.fusion.common.service;

import edu.fusion.common.model.Role;

public interface AuthenticationService {

    boolean authenticate(String username, String password, Role role);
}
