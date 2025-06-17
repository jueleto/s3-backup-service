package py.com.kuaa.s3_backup_service.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import py.com.kuaa.s3_backup_service.definition.BackupDefinitionDto;
import py.com.kuaa.s3_backup_service.s3.BucketObject;
import py.com.kuaa.s3_backup_service.s3.S3OperationInterface;

@Slf4j
@Component
public class ProcesadorArchivoService {

    private List<BackupDefinitionDto> definicionList;

    @Autowired
    S3OperationInterface bucketOperation;

    @Autowired
    ZipFileService zipFile;

    public ProcesadorArchivoService() {
        log.info("\n##########\nProcesadorArchivoService init\n##########");
    }

    public void leer(String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            String mensajeError = "Archivo de definición no existe [" + filePath + "] ";
            log.error(mensajeError);
            System.exit(1);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Leer el archivo JSON y convertirlo a un objeto BackupDefinitionDto
            this.definicionList = objectMapper.readValue(file,
                    new TypeReference<List<BackupDefinitionDto>>() {
                    });
            

            log.info(filePath + " leído exitosamente");
            this.verificar();
            log.info(filePath + " verificado exitosamente");

        } catch (Exception e) {
            String mensajeError = "Error al leer archivo de definiciones json [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }

    }

    private void verificar() {

        for (BackupDefinitionDto definicion : this.definicionList) {
            // System.out.println(definicion);
            // verificar tipo
            if (!(",original,zipdirectory,zipfile,").contains("," + definicion.getTipo() + ",")) {
                String mensajeError = "Archivo de definición incorrecto, tipo [" + definicion.getTipo() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }

            // verificar directorio
            if (definicion.getDirectorio().endsWith("/")) {
                String mensajeError = "Archivo de definición incorrecto, directorio no puede terminar en / [" + definicion.getDirectorio() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }

            // verificar destinoBase
            if (definicion.getDestinoBase().endsWith("/")) {
                String mensajeError = "Archivo de definición incorrecto, destinoBase no puede terminar en / [" + definicion.getDestinoBase() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }

            // verificar destinoForzado
            if (definicion.getDestinoForzado().endsWith("/")) {
                String mensajeError = "Archivo de definición incorrecto, destinoForzado no puede terminar en / [" + definicion.getDestinoForzado() + "] ";
                log.error(mensajeError);
                System.exit(1);
            }
        }

    }

    
    public void procesar() {

        for (BackupDefinitionDto definicionActual : this.definicionList) {

            recorrerFile(definicionActual);

        }

    }

    private void subirArchivoAws(String destinoForzado, String directorioDestino, boolean reemplazar, String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            String mensajeError = "Archivo o directorio no existe [" + filePath + "] ";
            log.error(mensajeError);
            System.exit(1);
        }
        String nombreArchivo = "";

        if (file.isFile()) {
            nombreArchivo = file.getName();
            //extraemos el nombre del archivo del path
            filePath = filePath.substring(0, filePath.length() - nombreArchivo.length()-1);
        }

        try {
            BucketObject bucketObject = bucketOperation.uploadFile(
                    reemplazar,
                    destinoForzado,
                    nombreArchivo,
                    directorioDestino+filePath,
                    file);
            
        } catch (Exception e) {
            String mensajeError = "Error al subir a aws [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }
    }

    private void subirArchivoZipAws(String destinoForzado, String directorioDestino, boolean reemplazar,
            String filePath, File fileZip) {

        if (!fileZip.exists()) {
            String mensajeError = "Archivo zip no existe [" + fileZip.getAbsolutePath() + "] ";
            log.error(mensajeError);
            System.exit(1);
        }

        String nombreArchivoZip = fileZip.getName();

        File file = new File(filePath);
        String nombreArchivo = "";

        if (file.isFile()) {
            nombreArchivo = file.getName();
            //extraemos el nombre del archivo del path
            filePath = filePath.substring(0, filePath.length() - nombreArchivo.length()-1);
        }

        try {
            BucketObject bucketObject = bucketOperation.uploadFile(
                    reemplazar,
                    destinoForzado,
                    nombreArchivoZip,
                    directorioDestino+filePath,
                    fileZip);
            
        } catch (Exception e) {
            String mensajeError = "Error al subir a aws [" + filePath + "] ";
            log.error(mensajeError, e);
            System.exit(1);
        }
    }

    private void recorrerFile(BackupDefinitionDto definicion) {

        File file = new File(definicion.getDirectorio());
        

        if (!file.exists()) {
            String mensajeError = "Archivo o directorio no existe [" + definicion.getDirectorio() + "] ";
            log.error(mensajeError);
            System.exit(1);
        }


        if (definicion.getTipo().equalsIgnoreCase("original")) {
            if (file.isFile()) {
                // subir directamente
                System.out.println("");
                System.out.println("- original file: " + file);
                subirArchivoAws( definicion.getDestinoForzado(), definicion.getDestinoBase(), definicion.isReemplazar(),
                        definicion.getDirectorio());
            }

            if (file.isDirectory()) {
                subirDirectorioOriginal(definicion);
            }
        }


        if (definicion.getTipo().equalsIgnoreCase("zipfile")) {
            if (file.isFile()) {
                // subir directamente
                System.out.println("");

                String fileZipString = file.getAbsolutePath()+".zip";
                boolean existeArchivo = bucketOperation.checkIfObjectExists(fileZipString);
                File fileComprimido = null;

                if(definicion.isReemplazar() || !existeArchivo){
                    // comprime el archivo y lo sube
                    System.out.println("- zipfile file: " + file);
                    fileComprimido = zipFile.compressFile(file);
                }

                if(!definicion.isReemplazar() && existeArchivo){
                    System.out.println("  Omitido antes zip: " + fileZipString);
                }

                if(fileComprimido != null){
                  subirArchivoZipAws(definicion.getDestinoForzado(),definicion.getDestinoBase(), definicion.isReemplazar(),
                            definicion.getDirectorio(), fileComprimido);
                    // eliminar archivo del tmp
                    System.out.println("- Eliminando archivo tmp: " + fileComprimido.getAbsolutePath() +" result: "+fileComprimido.delete());
                } 
            }

            if (file.isDirectory()) {
                System.out.println("");
                subirDirectorioZipFile(definicion);
            }

        
        }
        if (definicion.getTipo().equalsIgnoreCase("zipdirectory")) {
            if (file.isFile()) {
                String mensajeError = "Tipo zipdirectory debe ser un directorio y no un archivo ["
                        + definicion.getDirectorio() + "] ";
                log.error(mensajeError);
                System.exit(1);

            }

            if (file.isDirectory()) {
                // comprime el directorio entero y lo sube como zip
                System.out.println("");
                System.out.println("- zipdirectory directory: " + definicion.getDirectorio());
                File fileComprimido = zipFile.compressFile(new File(file.toString()));
                subirArchivoZipAws(definicion.getDestinoForzado(), definicion.getDestinoBase(), definicion.isReemplazar(),
                        file.toString(), fileComprimido);
                
                
            }
        }



    }

    private void subirDirectorioOriginal(BackupDefinitionDto definicion){
            // Define la ruta inicial del directorio a recorrer
            Path startPath = Paths.get(definicion.getDirectorio());

            try {
                Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                // Procesa cada archivo encontrado
                                System.out.println("- original file: " + file);
                                // Aquí puedes agregar código para leer y procesar el archivo si es necesario
                                subirArchivoAws(definicion.getDestinoForzado(), definicion.getDestinoBase(), definicion.isReemplazar(),
                                        file.toString());
                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                    throws IOException {
                                // Procesa cada directorio encontrado
                                System.out.println("- original directory: " + dir);

                                //crea el directorio
                                bucketOperation
                                        .createDirectory(definicion.isReemplazar(), definicion.getDestinoBase() + "/" + definicion.getDirectorio());

                                return FileVisitResult.CONTINUE;
                            }
    
                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                System.err.println("- original Error visitando el archivo: " + file + " (" + exc.getMessage() + ")");
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        

    }




    private void subirDirectorioZipFile(BackupDefinitionDto definicion){
        // Define la ruta inicial del directorio a recorrer
        Path startPath = Paths.get(definicion.getDirectorio());

        try {
            Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                            // Procesa cada archivo encontrado
                            System.out.println("");
                            System.out.println("- zipfile file: " + filePath);

                            File file = new File(filePath.toString());

                            //ignorar algunos archivos
                            if(file.getName().equalsIgnoreCase(".DS_Store")){
                                System.out.println("  IGNORAR file: " + filePath);
                                return FileVisitResult.CONTINUE;
                            }

                            // Aquí puedes agregar código para leer y procesar el archivo si es necesario
                            File fileComprimido = zipFile.compressFile(file);
                            subirArchivoZipAws(definicion.getDestinoForzado(), definicion.getDestinoBase(), definicion.isReemplazar(),
                            filePath.toString(), fileComprimido);

                            System.out.println("- Eliminando archivo tmp: " + fileComprimido.getAbsolutePath() +" result: "+fileComprimido.delete());
                            

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            // Procesa cada directorio encontrado
                            System.out.println("- zipfile directory: " + dir);

                            
                            //crea el directorio
                            bucketOperation
                                    .createDirectory(definicion.isReemplazar(), definicion.getDestinoBase() + "/" + definicion.getDirectorio());

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            System.err.println("- zipfile Error visitando el archivo: " + file + " (" + exc.getMessage() + ")");
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    

}


}
