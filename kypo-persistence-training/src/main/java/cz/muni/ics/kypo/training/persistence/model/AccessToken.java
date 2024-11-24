package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import lombok.*;

/**
 * Class representing access token needed by trainee to start a Training run.
 * Access tokens are associated with Training instances.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "access_token")
@NamedQueries({
        @NamedQuery(
                name = "AccessToken.findOneByAccessToken",
                query = "SELECT at FROM AccessToken at WHERE at.accessToken = :accessToken"
        ),
})
public class AccessToken extends AbstractEntity<Long> {

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    /**
     * Instantiates a new Access token
     *
     * @param id          unique identification number of access token
     * @param accessToken string representing token that trainee needs to know to access Training run
     */
    public AccessToken(Long id, String accessToken) {
        this.accessToken = accessToken;
        super.setId(id);
    }
}
