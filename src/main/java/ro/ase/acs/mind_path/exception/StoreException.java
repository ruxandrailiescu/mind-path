package ro.ase.acs.mind_path.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;
}
