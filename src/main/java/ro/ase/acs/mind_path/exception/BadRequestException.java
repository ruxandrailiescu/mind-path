package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends StoreException {
    public BadRequestException(String message) {
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage(message);
    }
}
