package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    @Query("SELECT u.id FROM Users u WHERE u.username = :username")
    Integer findIdByUsername(@Param("username") String username);

     @Query("SELECT u FROM Users u WHERE u.username = :username")
     UserDetails findByUsername(@Param("username") String username);
}
