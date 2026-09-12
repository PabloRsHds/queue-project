package br.com.queue.controller.login;

import br.com.queue.dtos.loginDto.RequestLoginDto;
import br.com.queue.dtos.tokenDto.ResponseTokens;
import br.com.queue.service.login.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public ResponseEntity<ResponseTokens> login(
            @Valid @RequestBody RequestLoginDto request,
            HttpServletResponse response) {

        var tokens = loginService.login(request, response);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        this.loginService.logout(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<ResponseTokens> refreshTokens(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        var tokens = this.loginService.refreshTokens(refreshToken, response);
        return ResponseEntity.ok().body(tokens);
    }
}
