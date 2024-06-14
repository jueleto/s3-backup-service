package py.com.kuaa.s3_backup_service.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BackupDefinitionDto {

    private String tipo;
    private String directorio;
    private String destinoBase;
    private boolean reemplazar;
    private String nota;

}


