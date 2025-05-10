package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class QuizAttemptException extends StoreException {
    public QuizAttemptException(String message) {
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage(message);
    }
}
