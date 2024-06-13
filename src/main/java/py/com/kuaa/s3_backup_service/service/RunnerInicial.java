package py.com.kuaa.s3_backup_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import py.com.kuaa.s3_backup_service.s3.S3OperationInterface;

@Slf4j
@Component
public class RunnerInicial implements CommandLineRunner {

    @Autowired
    ProcesadorArchivoService procesadorArchivo;

    @Autowired
    S3OperationInterface bucketOperation;

    // @Autowired
    public RunnerInicial() {
        log.info("\n##########\nRunnerInicial init\n##########");
    }

    @Override
    public void run(String... args) throws Exception {


        final String USAGE = """

        Usage:
            <archivoDefinicion>

        Where:
            archivoDefinicion: archivo donde está definido los respaldos a realizar.

        """;

        if (args.length < 1) {
            System.out.println(USAGE);
            //salir
            System.exit(1);
        }        

        String filePath = args[0];
        //String directorioDestino = args[1];


        procesadorArchivo.leer(filePath);
        procesadorArchivo.procesar();


        //servicioInicial.miMetodo(argumento1, argumento2);
        
    }
}
