package employees;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
@Getter @Setter
@Audited
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private String name;

    public Employee(String name) {
        this.name = name;
    }
}
