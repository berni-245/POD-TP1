# TP1 POD - Estación de Trenes

## 👋 Introducción

En este trabajo práctico de la materia de Programación de Objetos Distribuidos se buscó aplicar los conceptos de concurrencia y gRPC aprendidos a lo largo de la cursada en un proyecto de manejar múltiples clientes que funcionarán en distintos threads. El contexto del tp es de una estación de trenes. 

[Enunciado](docs/TPE1%20Estación%20de%20Tren.pdf)

### ❗ Requisitos:
- Java 21
- [Maven](https://maven.apache.org/download.cgi)
- Terminal estilo Unix

Clonar el proyecto utilizando:
```shell
git clone https://github.com/AlekDG/pod-tp1.git
```

## 🛠️ Compilación
Desde la terminal y parándose en la carpeta raíz del proyecto correr el siguiente comando:
```shell
mvn clean install
```

## 🏃 Ejecución

### 🌐 Server
Una vez se tenga el proyecto compilado, lo primero que queremos hacer es iniciar el servidor. Dirigirse a la carpeta `server/target/` y ahí veremos un archivo `tpe1-g3-server-1.0-SNAPSHOT-bin.tar.gz`. Descomprimirlo usando algún comando como:
```shell
tar -xzf tpe1-g3-server-1.0-SNAPSHOT-bin.tar.gz
```
Y ahora tendremos una carpeta `tpe1-g3-server-1.0-SNAPSHOT`. Accedemos a dicha carpeta y tendremos un archivo `run-server.sh`, le damos permisos de ejecución utilizando:
```shell 
chmod u+x run-server.sh 
```
Luego borramos los retornos de carro `\r` al final de cada línea (esto afecta a ciertas terminales de linux) se puede usar:
```shell 
sed -i 's/\r$//' run-server.sh
```
o usar el editor `vim` sobre el archivo y correr el comando:
```shell 
:set ff=unix
```

Finalmente corremos el servidor con:
```shell 
./run-server.sh <-Dport>
```
Opcionalmente se le puede agregar el puerto de localhost específico que se quiera usar, de no especificarlo tomará el puerto `50051`

### 👨🏻‍💼 Cliente 
Ya con el servidor corriendo, vamos a hacer los mismos pasos para los clientes desde otra consola.
Vamos para la carpeta `client/target/`, descomprimimos el archivo `tpe1-g3-client-1.0-SNAPSHOT-bin.tar` y accedemos a la carpeta `tpe1-g3-client-1.0-SNAPSHOT`.
Una vez ahí, corremos el comando:
```shell 
chmod u+x *.sh 
```
Para darle permiso de ejecución a todos los `.sh`. Borramos los retornos de carro utilizando alguna de las dos formas sugeridas en la sección de `server` y finalmente corremos alguno de los clientes con:
```shell 
./<client>.sh <args> 
```
Cada `<Client>` tendrá distintos `<args>`, aunque para todos siempre se deberá especificar la dirección del servidor y el puerto con el flag `-DserverAddress=xx.xx.xx.xx:yyyy`, y la acción a realizar con `-Daction=<action>`. Se puede consultar más información sobre qué comandos usar para cada cliente en el [enunciado](docs/TPE1%20Estación%20de%20Tren.pdf).

## 👥 Equipo
- [Padula Morillo, Alejo](https://github.com/AlekDG)
- [Scheffer, Tomás Guillermo](https://github.com/tomaScheffer)
- [Zapico, Bernardo](https://github.com/berni-245)
