package py.com.kuaa.s3_backup_service.s3;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class S3Operation implements S3OperationInterface {

    @Value("${s3.bucket.accessKeyId}")
    private String accessKeyId;

    @Value("${s3.bucket.accessSecKey}")
    private String accessSecKey;

    @Value("${s3.bucket.name}")
    private String bucketName;

    S3Client s3Client;

    public S3Operation() {

    }

    @Override
    public BucketObject uploadFile(String nombreArchivo, String directorioDestino, File archivo) {
        System.out.println("- Subiendo archivo: " + archivo.getAbsolutePath());
        String objectKey = directorioDestino + "/" + nombreArchivo;

        if (objectKey.startsWith("/"))
            objectKey = objectKey.substring(1);

        boolean existeObjeto = checkIfObjectExists(objectKey);
        System.out.println("- Existe en bucket?: " + existeObjeto);

        System.out.println("");
        s3Client.getClientAWS(accessKeyId, accessSecKey).putObject(
                bucketName,
                objectKey,
                archivo);

        return new BucketObject(objectKey, bucketName);
    }

    @Override
    public boolean checkIfObjectExists(String objectKey) {

        boolean exists = s3Client.getClientAWS(accessKeyId, accessSecKey)
                .doesObjectExist(bucketName, objectKey);

        return exists;

    }

}
