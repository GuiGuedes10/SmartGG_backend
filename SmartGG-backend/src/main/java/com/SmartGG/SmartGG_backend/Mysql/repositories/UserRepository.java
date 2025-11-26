package com.SmartGG.SmartGG_backend.Mysql.repositories;

import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
}