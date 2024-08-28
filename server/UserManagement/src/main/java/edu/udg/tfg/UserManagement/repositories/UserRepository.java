package edu.udg.tfg.UserManagement.repositories;

import edu.udg.tfg.UserManagement.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserInfo, UUID> {

}