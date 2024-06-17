package py.com.kuaa.s3_backup_service.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Manifest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import py.com.kuaa.s3_backup_service.S3BackupServiceApplication;
import py.com.kuaa.s3_backup_service.s3.S3OperationInterface;

@Slf4j
@Component
public class RunnerInicial implements CommandLineRunner {

    @Autowired
    ProcesadorArchivoService procesadorArchivo;

    @Autowired
    S3OperationInterface bucketOperation;

    @Value("${spring.application.name}")
	private String springApplicationName;

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

        mensajeInicio();
        procesadorArchivo.leer(filePath);
        procesadorArchivo.procesar();


        //servicioInicial.miMetodo(argumento1, argumento2);
        
    }

    public void mensajeInicio() {
		Date date = new Date();
		String DD_MM_AAAA=new SimpleDateFormat("dd/MM/yyyy").format(date);
		String HH_MM_SS=new SimpleDateFormat("HH:mm:ss").format(date);


		String implementationVersion = null;
        try (InputStream is = S3BackupServiceApplication.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			implementationVersion = new Manifest(is).getMainAttributes().getValue("Implementation-Version");
		} catch (IOException e) {
			log.warn("No se pudo obtener información del archivo MANIFEST.MF", e);
		}

		System.out.println("\n           ▄█████▄");
		System.out.println("        ▄███████████▄");
		System.out.println("     ▄██████       █████▄");
        System.out.println("    ██████   LISTO   █████");
        System.out.println("   █████               ████");
        System.out.println("  ████   "+"EJECUTANDOSE"+"   ████");
        System.out.println("   ████   "+DD_MM_AAAA+"   ████");
        System.out.println("    ████▄  "+HH_MM_SS+"  ▄████");
        System.out.println("       ███         ▄███");
        System.out.println("         ███████████"); 
		System.out.println("            █████ \n"); 

		System.out.println("\n" + springApplicationName);
		System.out.println("Versión: "+implementationVersion);
	
		System.out.println("\n\n");

	}
}
