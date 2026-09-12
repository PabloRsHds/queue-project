package br.com.queue.controller.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.ResponseUserDashBoardDto;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ResponseUserDto> createUser(
            JwtAuthenticationToken token,
            @RequestBody CreateUserDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.createUser(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_RECEPTION', 'SCOPE_ATTENDANT')")
    public ResponseEntity<ResponseUserDto> updateUser(
            @RequestBody UpdateUserDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.userService.updateUser(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<Page<ResponseUserDto>> getAllUsers(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getAllUsers(token, page, size, search));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ResponseUserInfoDto> getUserById(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getUserById(userId));
    }

    @GetMapping("/token")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_RECEPTION', 'SCOPE_ATTENDANT')")
    public ResponseEntity<ResponseUserInfoDto> getUserByToken(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getUserByToken(token));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ResponseUserDto> deleteUser(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(this.userService.deleteUser(userId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ResponseUserDashBoardDto> getUserStatistics(JwtAuthenticationToken token) {
        return ResponseEntity.ok()
                .body(this.userService.getUserStatistics(token));
    }
}