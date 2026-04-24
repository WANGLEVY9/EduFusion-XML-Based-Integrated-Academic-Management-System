package edu.fusion.serverc.service;

import edu.fusion.common.model.Role;
import edu.fusion.common.service.AuthenticationService;

public class AuthServiceC implements AuthenticationService {

    private final CollegeCGateway gateway = new CollegeCGateway();

    @Override
    public boolean authenticate(String username, String password, Role role) {
        if (role == Role.ADMIN) {
            return gateway.authenticateAdmin(username, password);
        }
        return gateway.authenticateStudent(username, password);
    }
}
