package ro.ase.acs.mind_path.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.ase.acs.mind_path.entity.UserResponse;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
}
