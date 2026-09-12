package br.com.queue.service.unit;

import br.com.queue.entities.unit.Unit;
import br.com.queue.repositories.unit.UnitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitContext {

    private final UnitRepository unitRepository;

    public Unit getCurrentUnit(JwtAuthenticationToken token) {

        String unitId = token.getToken().getClaimAsString("UNIT_ID");

        return unitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unidade não encontrada"));
    }
}
