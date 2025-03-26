package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class QuestionNotFoundException extends StoreException {
    public QuestionNotFoundException() {
        this.setHttpStatus(HttpStatus.NOT_FOUND);
        this.setMessage("QUESTION_NOT_FOUND");
    }
}
