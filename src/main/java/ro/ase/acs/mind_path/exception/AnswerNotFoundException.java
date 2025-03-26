package ro.ase.acs.mind_path.exception;

import org.springframework.http.HttpStatus;

public class AnswerNotFoundException extends StoreException {
    public AnswerNotFoundException() {
        this.setHttpStatus(HttpStatus.NOT_FOUND);
        this.setMessage("ANSWER_NOT_FOUND");
    }
}
