package edu.fusion.servera.service;

import edu.fusion.common.model.Role;
import edu.fusion.common.service.AuthenticationService;

public class AuthServiceA implements AuthenticationService {

    private final CollegeAGateway gateway = new CollegeAGateway();

    @Override
    public boolean authenticate(String username, String password, Role role) {
        if (role == Role.ADMIN) {
            return gateway.authenticateAdmin(username, password);
        }
        return gateway.authenticateStudent(username, password);
    }
}
