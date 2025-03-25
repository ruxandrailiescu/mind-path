package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class QuizNotFoundException extends StoreException {
    public QuizNotFoundException() {
        this.setHttpStatus(HttpStatus.NOT_FOUND);
        this.setMessage("QUIZ_NOT_FOUND");
    }
}
