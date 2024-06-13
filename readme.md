## s3-backup-service
Es una app springboot para realizar copias de seguridad de direcctorios o archivos definidos al servicio de almacenamiento de aws s3

### Definición de backup
Al programa se le pasa un archivo json donde está definido de que y como se realizarán las copias. El formato del archivo es
```json
[
    {
        "tipo": "",
        "directorio": "",
        "reemplazar": false,
        "nota": ""
    }
]
```
- tipo:
- - original: se sube el archivo o directorio tal cual como está.
- - zip : comprime todo el directorio y lo sube como archivo único, comprime el archivo.
- - zipone: recorre el dirctorio, comprime cada archivo en forma individual y lo sube.
- directorio:
- - El directorio o archivo que se subirá
- destino:
- - El directorio donde se subirá el archivo, dejar vacio para que use la raiz
- reemplazar: 
- - Si se reemplazará o no el archivo o los archivos.
- nota:
- - texto adicional para describir o algo que se quiera poner


