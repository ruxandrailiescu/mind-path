package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException  extends StoreException {
    public UserNotFoundException() {
        this.setHttpStatus(HttpStatus.NOT_FOUND);
        this.setMessage("USER_NOT_FOUND");
    }
}
