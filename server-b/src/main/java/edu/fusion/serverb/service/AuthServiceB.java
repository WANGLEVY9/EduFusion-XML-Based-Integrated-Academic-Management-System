package edu.fusion.serverb.service;

import edu.fusion.common.model.Role;
import edu.fusion.common.service.AuthenticationService;

public class AuthServiceB implements AuthenticationService {

    private final CollegeBGateway gateway = new CollegeBGateway();

    @Override
    public boolean authenticate(String username, String password, Role role) {
        if (role == Role.ADMIN) {
            return gateway.authenticateAdmin(username, password);
        }
        return gateway.authenticateStudent(username, password);
    }
}
