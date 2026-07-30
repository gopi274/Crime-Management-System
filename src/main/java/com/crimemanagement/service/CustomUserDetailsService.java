package com.crimemanagement.service;

import com.crimemanagement.dao.UserAccountDAO;
import com.crimemanagement.entity.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountDAO userAccountDAO;

    @Autowired
    public CustomUserDetailsService(UserAccountDAO userAccountDAO) {
        this.userAccountDAO = userAccountDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserAccount user = userAccountDAO.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return new User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Database error during authentication", e);
        }
    }
}
