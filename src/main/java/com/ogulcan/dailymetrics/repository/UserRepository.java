package com.ogulcan.dailymetrics.repository;


import com.ogulcan.dailymetrics.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByLoginCode(String loginCode);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER (CONCAT('%', :searchTerm, '%'))")
    List<User> findByUsernameContaining(@Param("searchTerm") String searchTerm);
}