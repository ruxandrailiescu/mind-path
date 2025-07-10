package ro.ase.acs.mind_path.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ro.ase.acs.mind_path.dto.response.WeaknessReportDto;
import ro.ase.acs.mind_path.entity.User;
import ro.ase.acs.mind_path.service.UserService;
import ro.ase.acs.mind_path.service.WeaknessDetectionService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class WeaknessReportController {
    private final WeaknessDetectionService weaknessDetectionService;
    private final UserService userService;

    @GetMapping("/students/me/weakness-report")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<WeaknessReportDto> getWeaknessReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = userService.findByEmail(userDetails.getUsername());
        WeaknessReportDto res = weaknessDetectionService.generateWeaknessReport(user.getUserId(), from, to);
        return ResponseEntity.ok(res);
    }
}
