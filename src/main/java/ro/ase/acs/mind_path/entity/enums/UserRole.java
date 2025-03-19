package ro.ase.acs.mind_path.entity.enums;

public enum UserRole {
    STUDENT, TEACHER, ADMIN;

    public static UserRole fromString(String role) {
        return UserRole.valueOf(role.toUpperCase());
    }
}
