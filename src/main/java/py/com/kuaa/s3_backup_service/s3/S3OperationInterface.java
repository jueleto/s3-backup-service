package py.com.kuaa.s3_backup_service.s3;

import java.io.File;

public interface S3OperationInterface {

     BucketObject uploadFile(String nombreArchivo, String directorioDestino, File archivo);
     boolean checkIfObjectExists(String objectKey);
     
}
