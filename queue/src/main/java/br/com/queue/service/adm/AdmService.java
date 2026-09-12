package br.com.queue.service.adm;

import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.repositories.unit.UnitRepository;
import br.com.queue.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdmService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${adm1.email}")
    private String adm1Email;

    @Value("${adm1.password}")
    private String adm1Password;

    @Value("${adm1.username}")
    private String adm1Username;

    @Value("${adm1.name}")
    private String adm1Name;

    @Value("${adm1.surname}")
    private String adm1Surname;

    @Override
    public void run(String... args) {

        Unit unit = createUnitIfNotExists();

        createAdminIfNotExists(unit);
    }

    private Unit createUnitIfNotExists() {

        return unitRepository.findByName("Unidade 01")
                .orElseGet(() -> {

                    var unit = new Unit();

                    unit.setName("Unidade 01");
                    unit.setAddress("sem endereço físico");
                    unit.setActive(true);

                    return unitRepository.save(unit);
                });
    }

    private void createAdminIfNotExists(Unit unit) {

        userRepository.findByEmail(adm1Email)
                .ifPresentOrElse(
                        user -> System.out.println("ADM já existe!"),

                        () -> {

                            var admin = new User();

                            admin.setUsername(adm1Username);
                            admin.setName(adm1Name);
                            admin.setSurname(adm1Surname);
                            admin.setActive(true);
                            admin.setRole(Role.ADMIN);

                            admin.setEmail(adm1Email);
                            admin.setPassword(
                                    passwordEncoder.encode(adm1Password)
                            );

                            admin.setCreatedAt(LocalDateTime.now());

                            admin.setUnit(unit);

                            userRepository.save(admin);

                            System.out.println("ADM criado com sucesso!");
                        }
                );
    }
}
