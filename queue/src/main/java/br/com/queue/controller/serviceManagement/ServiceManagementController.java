package br.com.queue.controller.serviceManagement;

import br.com.queue.dtos.serviceManagement.create.CreateServiceManagementDto;
import br.com.queue.dtos.serviceManagement.ResponseServiceManagementDto;
import br.com.queue.dtos.serviceManagement.getServiceDto.ResponseGetServiceByIdDto;
import br.com.queue.dtos.serviceManagement.list_service.ResponseServicesForCreatedUser;
import br.com.queue.dtos.serviceManagement.statistics.ResponseServiceDashBoardDto;
import br.com.queue.dtos.serviceManagement.update.UpdateServiceManagementDto;
import br.com.queue.service.serviceManagement.ServiceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceManagementController {

    private final ServiceManagementService serviceManagementService;

    @PostMapping
    public ResponseEntity<ResponseServiceManagementDto> createServiceManagement(
            JwtAuthenticationToken token,
            @RequestBody CreateServiceManagementDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.serviceManagementService.createServiceManagement(token, dto));
    }

    @PatchMapping
    public ResponseEntity<ResponseServiceManagementDto> updateServiceManagement(
            @RequestBody UpdateServiceManagementDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.updateServiceManagement(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseServiceManagementDto>> getAllServicesManagement(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getAllServicesManagement(token, page, size, search));
    }

    @GetMapping("/{serviceManagementId}")
    public ResponseEntity<ResponseGetServiceByIdDto> getServiceManagementById(
            @PathVariable String serviceManagementId
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getServiceManagementById(serviceManagementId));
    }

    @GetMapping("/service-for-created-user")
    public ResponseEntity<List<ResponseServicesForCreatedUser>> servicesForCreatedUser() {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.servicesForCreatedUser());
    }

    @DeleteMapping("/{serviceManagementId}")
    public ResponseEntity<ResponseServiceManagementDto> deleteServiceManagement(@PathVariable String serviceManagementId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(this.serviceManagementService.deleteServiceManagement(serviceManagementId));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseServiceDashBoardDto> getStatistics(
            JwtAuthenticationToken token) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getStatistics(token));
    }
}