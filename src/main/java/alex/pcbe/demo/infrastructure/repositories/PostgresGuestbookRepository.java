package alex.pcbe.demo.infrastructure.repositories;

import alex.pcbe.demo.application.repositories.GuestbookRepository;
import alex.pcbe.demo.domain.entities.GuestbookEntry;
import alex.pcbe.demo.infrastructure.mappers.GuestbookEntryMapper;
import alex.pcbe.demo.infrastructure.db.GuestbookEntryEntity;
import alex.pcbe.demo.infrastructure.db.JpaGuestbookRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Primary  // This makes it the default implementation, overriding InMemoryGuestbookRepository
public class PostgresGuestbookRepository implements GuestbookRepository {

    private final JpaGuestbookRepository jpaRepository;
    private final GuestbookEntryMapper mapper;

    public PostgresGuestbookRepository(JpaGuestbookRepository jpaRepository,
                                      GuestbookEntryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public GuestbookEntry save(GuestbookEntry entry) {
        GuestbookEntryEntity entity = mapper.toEntity(entry);
        GuestbookEntryEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<GuestbookEntry> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GuestbookEntry> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}

