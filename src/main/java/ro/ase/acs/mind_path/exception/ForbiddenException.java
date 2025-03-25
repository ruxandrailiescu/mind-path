package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends StoreException {
    public ForbiddenException(String message) {
        this.setHttpStatus(HttpStatus.FORBIDDEN);
        this.setMessage(message);
    }
}
