package com.medapp.backend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;


public interface UserRepository extends MongoRepository<User , String>{


    Optional<User> findByEmail(String email);
    List<User> findByRoleAndActifTrue(Role role);
    
}
