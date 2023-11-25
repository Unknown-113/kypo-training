package cz.muni.ics.kypo.training.persistence.model.detection;

import cz.muni.ics.kypo.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "location_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
        @NamedQuery(
                name = "LocationSimilarityDetectionEvent.findLocationSimilarityEventById",
                query = "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.id = :eventId"
        ),
        @NamedQuery(
                name = "LocationSimilarityDetectionEvent.findAllByCheatingDetectionId",
                query = "SELECT lsde FROM LocationSimilarityDetectionEvent lsde WHERE lsde.cheatingDetectionId = :cheatingDetectionId"
        )
})
public class LocationSimilarityDetectionEvent extends AbstractDetectionEvent {

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "dns")
    private String dns;

    @Column(name = "is_address_deploy")
    private boolean isAddressDeploy;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public boolean getIsAddressDeploy() {
        return isAddressDeploy;
    }

    public void setIsAddressDeploy(boolean isAddressDeploy) {
        this.isAddressDeploy = isAddressDeploy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationSimilarityDetectionEvent)) return false;
        if (!super.equals(o)) return false;
        LocationSimilarityDetectionEvent other = (LocationSimilarityDetectionEvent) o;
        return Objects.equals(getIpAddress(), other.getIpAddress()) &&
                Objects.equals(getDns(), other.getDns()) &&
                Objects.equals(getIsAddressDeploy(), other.getIsAddressDeploy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getIpAddress(), getDns(), getIsAddressDeploy());
    }

    @Override
    public String toString() {
        return "LocationSimilarityDetectionEvent{" +
                "ipAddress='" + ipAddress + '\'' +
                ", dns='" + dns + '\'' +
                ", isAddressDeploy='" + isAddressDeploy + '}';
    }
}
