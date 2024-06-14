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

    @Value("${s3.bucket.region}")
    private String bucketRegion;

    S3Client s3Client;

    public S3Operation() {
        s3Client = new S3Client();
    }

    @Override
    public BucketObject createDirectory(boolean reemplazar, String directorioDestino) {
        String objectKey = directorioDestino + "/";

        if (objectKey.startsWith("/"))
            objectKey = objectKey.substring(1);
        if (objectKey.startsWith("/"))
            objectKey = objectKey.substring(1);

        System.out.println("- Preparando directorio: " + objectKey);
        boolean existeObjeto = checkIfObjectExists(objectKey);
        System.out.println("  Existe en bucket '" + objectKey + "': " + (existeObjeto ? "Si" : "No"));

        if (!existeObjeto) {
            System.out.println("  Creando directorio: " + objectKey);
            s3Client.getClientAWS(accessKeyId, accessSecKey, bucketRegion).putObject(
                    bucketName,
                    objectKey,
                    "");

            System.out.println("  Creado exitosamente: " + objectKey);
        } else {
            if (reemplazar) {

                System.out.println("  Reemplazando directorio: " + objectKey);
                s3Client.getClientAWS(accessKeyId, accessSecKey, bucketRegion).putObject(
                        bucketName,
                        objectKey,
                        "");

                System.out.println("  Reemplazado exitosamente: " + objectKey);

            } else {

                System.out.println("  Omitido: " + objectKey);

            }

        }

        System.out.println("");

        return new BucketObject(objectKey, bucketName);
    }

    @Override
    public BucketObject uploadFile(boolean reemplazar, String destinoForzado, String nombreArchivo, String directorioDestino, File archivo) {
        System.out.println("- Preparando archivo: " + archivo.getAbsolutePath());
        String objectKey = directorioDestino + "/" + nombreArchivo;

        if(!destinoForzado.equalsIgnoreCase("")){
            objectKey = destinoForzado + "/" + nombreArchivo;
        }

        if (objectKey.startsWith("/"))
            objectKey = objectKey.substring(1);
        if (objectKey.startsWith("/"))
            objectKey = objectKey.substring(1);

        boolean existeObjeto = checkIfObjectExists(objectKey);
        System.out.println("  Existe en bucket '" + objectKey + "': " + (existeObjeto ? "Si" : "No"));

        if (!existeObjeto) {
            System.out.println("  Subiendo archivo: " + objectKey);
            s3Client.getClientAWS(accessKeyId, accessSecKey, bucketRegion).putObject(
                    bucketName,
                    objectKey,
                    archivo);

            System.out.println("  Subido exitosamente: " + objectKey);
        } else {
            if (reemplazar) {

                System.out.println("  Reemplazando archivo: " + objectKey);
                s3Client.getClientAWS(accessKeyId, accessSecKey, bucketRegion).putObject(
                        bucketName,
                        objectKey,
                        archivo);

                System.out.println("  Reemplazado exitosamente: " + objectKey);

            } else {

                System.out.println("  Omitido: " + objectKey);

            }

        }

        System.out.println("");
        return new BucketObject(objectKey, bucketName);
    }

    @Override
    public boolean checkIfObjectExists(String objectKey) {

        boolean exists = s3Client.getClientAWS(accessKeyId, accessSecKey, bucketRegion)
                .doesObjectExist(bucketName, objectKey);

        return exists;

    }

}
