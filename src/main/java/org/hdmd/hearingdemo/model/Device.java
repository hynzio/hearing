package org.hdmd.hearingdemo.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Schema(description = "단말기 엔티티")
@Table(name="device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="device_id", nullable = false)
    @Schema(description = "단말기 아이디")
    private Long id;

    @Column(name="device_num", nullable = false)
    private String deviceNum;

    @Column(name="device_name", nullable = false)
    @Size(max=15)
    @Schema(description = "단말기 이름")
    private String deviceName;

    @Column(name="device_address", nullable = false)
    @Schema(description = "단말기 주소")
    private String address;

    @Column(name = "device_status", nullable = false)
    @Schema(description = "단말기 상태")
    private Boolean status = false; //0(false)=귀가, 1(true)=외출

    @JsonManagedReference
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recording> recordings;

    @JsonManagedReference
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<History> histories;

}
