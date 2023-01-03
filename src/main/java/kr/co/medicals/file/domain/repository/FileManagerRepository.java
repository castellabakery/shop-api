package kr.co.medicals.file.domain.repository;

import kr.co.medicals.file.domain.entity.FileManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
}
