package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends StoreException {
    public UserAlreadyExistsException() {
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("USER_ALREADY_EXISTS");
    }
}
