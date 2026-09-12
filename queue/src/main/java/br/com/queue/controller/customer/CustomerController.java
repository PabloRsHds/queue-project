package br.com.queue.controller.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.create.CreateCustomerDto;
import br.com.queue.dtos.customer.create.ResponseCustomerDto;
import br.com.queue.dtos.customer.getCustomer.ResponseCustomerById;
import br.com.queue.dtos.customer.getCustomer.ResponseGetCustomerIdsAndNames;
import br.com.queue.dtos.customer.statistics.ResponseCustomerDashBoardDto;
import br.com.queue.dtos.customer.update.UpdateCustomerDto;
import br.com.queue.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ResponseCustomerDto> createCustomer(
            JwtAuthenticationToken token,
            @RequestBody CreateCustomerDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.customerService.registerCustomer(token, dto));
    }

    @PatchMapping
    public ResponseEntity<ResponseCustomerDto> updateCustomer(@RequestBody UpdateCustomerDto dto) {

        return ResponseEntity.ok()
                .body(this.customerService.updateCustomer(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAllCustomersDto>> getAllCustomers(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {

        return ResponseEntity.ok()
                .body(this.customerService.getAllCustomers(token, page, size, search));
    }

    @GetMapping("/ids-and-names")
    public ResponseEntity<List<ResponseGetCustomerIdsAndNames>> getCustomerIdsAndNames() {

        return ResponseEntity.ok()
                .body(this.customerService.getCustomerIdsAndNames());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseCustomerById> getCustomerById(@PathVariable String customerId) {

        return ResponseEntity.ok()
                .body(this.customerService.getCustomerById(customerId));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ResponseCustomerDto> deleteCustomer(@PathVariable String customerId) {

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(this.customerService.deleteCustomer(customerId));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseCustomerDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {

        return ResponseEntity.ok()
                .body(this.customerService.getStatistics(token));
    }
}
