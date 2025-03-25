package ro.ase.acs.mind_path.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.acs.mind_path.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private String email;
    private String firstName;
    private String lastName;
    private String userType;

    public UserProfileDto(User user) {
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userType = user.getRole().name();
    }
}
