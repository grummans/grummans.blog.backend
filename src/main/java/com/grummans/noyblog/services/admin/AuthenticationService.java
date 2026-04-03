package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    public Integer authenticate(String username, String password) {
        var userDetails = usersRepository.findByUsername(username);
        if (userDetails != null && passwordEncoder.matches(password, userDetails.getPassword())) {
            return usersRepository.findIdByUsername(username);
        }
        return null;
    }
}
