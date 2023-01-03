package kr.co.medicals.board.notice.domain.repository;

import kr.co.medicals.board.notice.domain.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
