package run.prizm.core.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.file.entity.File;

public interface FileRepository extends JpaRepository<File, Long> {
}