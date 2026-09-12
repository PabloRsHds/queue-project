package br.com.queue.entities.serviceManagement;

import br.com.queue.entities.department.Department;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_service_management")
public class ServiceManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_management_id")
    private String serviceManagementId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String description = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "serviceManagement", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    private Long lastTicketNumber = 0L;

    @OneToMany(mappedBy = "serviceManagement", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    @ManyToMany(mappedBy = "services")
    private Set<User> users = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;
}
