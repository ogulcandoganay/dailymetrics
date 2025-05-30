package com.ogulcan.dailymetrics.service;

import com.ogulcan.dailymetrics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginCode) throws UsernameNotFoundException {
        return userRepository.findByLoginCode(loginCode)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login code: " + loginCode));
    }
}