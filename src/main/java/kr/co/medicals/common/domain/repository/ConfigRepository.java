package kr.co.medicals.common.domain.repository;

import kr.co.medicals.common.domain.entity.Config;
import kr.co.medicals.common.domain.entity.ConfigIds;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, ConfigIds> {

//    Optional<Config> findByDomainAndAndConfigKey(String domain, String configKey);

    Optional<Config> findByConfigIds(ConfigIds configIds);
}
