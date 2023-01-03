package kr.co.medicals.common.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfigIds implements Serializable {

    @Column(name="DOMAIN", length = 20)
    private String domain;

    @Column(name="CONFIG_KEY", length = 40)
    private String configKey;

}
