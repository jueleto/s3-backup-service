package py.com.kuaa.s3_backup_service.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import py.com.kuaa.s3_backup_service.s3.BucketObject;
import py.com.kuaa.s3_backup_service.s3.S3OperationInterface;

@Slf4j
@Component
public class RunnerInicial implements CommandLineRunner {

    @Autowired
    S3OperationInterface bucketOperation;

    @Autowired
    public RunnerInicial() {
        log.info("\n##########\nRunnerInicial init\n##########");
    }

    @Override
    public void run(String... args) throws Exception {


        final String USAGE = """

        Usage:
            <archivo> <directorioDestino>

        Where:
            archivo - the Amazon S3 object to upload.
            directorioDestino - the directorioDestino where to uload.
        """;

        if (args.length < 2) {
            System.out.println(USAGE);
            //salir
            System.exit(1);
        }

        System.out.println("\n\nLECTURA EXITOSA DE ARGUMENTOS\n\n");

        

        String filePath = args[0];
        String directorioDestino = args[1];

        //servicioInicial.miMetodo(argumento1, argumento2);
        // File file = new File("/Users/emilio/repo/awsSss/docs/archiiiivo.txt");
        File file = new File(filePath);

        if(!file.exists()){
            System.out.println("ARCHIVO NO EXISTE : "+filePath);
            System.exit(1);
        }

        String nombreArchivo = file.getName();

        try {
            BucketObject bucketObject =  bucketOperation.uploadFile(nombreArchivo, directorioDestino , file);
            System.out.println("ARCHIVO SUBIDO CON EXITO");
            System.out.println("  objectKey: "+ bucketObject.getObjectKey());
            System.out.println("");
            
        } catch (Exception e) {
            System.out.println("ERROR AL SUBIR ARCHIVO A AWS");
            e.printStackTrace();
        }
    }
}
