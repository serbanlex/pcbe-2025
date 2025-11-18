package alex.pcbe.demo.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaGuestbookRepository extends JpaRepository<GuestbookEntryEntity, String> {
}

