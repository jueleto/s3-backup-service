
## Version 0.0.8
## Conf pom.xml
- Archivo [pom.xml](../pom.xml)
- Descomentar el <resources> que esta dentro del <build>

- Cambiar a v0.0.8<version> en pom.xml, el que esta debajo de <artifactId>s3-backup-service</artifactId> 
- [pom.xml](../pom.xml)

## Crear jar
- Desde la raiz del proyecto
> chmod +x mvnw
> ./mvnw package -Dmaven.test.skip -Djar.finalName=s3-backup-service

- el archivo se genera en /target segun el finalName colocado
- Copiar a alguna lado
> cp ./target/s3-backup-service.jar ~/opt/s3-backup-service/




### Commit
- Ver cambios realizados 
- Commitear cambios

- El main debe estar pulleado
> git checkout main
> git pull origin main
> git checkout develop

- MAC TEU
> git checkout main && git pull origin main && git checkout develop
- WIN TEU
> git checkout main ; git pull origin main ; git checkout develop


- Versionar en git



Windows
> git flow release start v0.0.8; git flow release finish -m 'v0.0.8-liberada' 'v0.0.8' ; git push origin --all ; git push origin --tags

Mac
> git flow release start v0.0.8 && git flow release finish -m 'v0.0.8-liberada' 'v0.0.8' && git push origin --all && git push origin --tags

- Checkout a develop
> git checkout develop



## Como ejecutar?
> ${javaHome}/bin/java -jar /opt/${jarFinalName}.jar "/opt/backup-definition.json" --spring.config.location="/opt/application.properties"


